package lab.ch01;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import lab.common.Models;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * 01 章作业：TokenStream + SseEmitter 的流式问答接口。
 *
 * 线程模型（面试重点）：
 *   Tomcat 线程：接收请求 → 注册回调 → 返回 SseEmitter 后立即释放（Servlet 异步）
 *   LangChain4j 回调线程：逐 token 到达 → emitter.send() 推给客户端
 *
 * 设计要点：
 *   - 事件命名（token/done/error）：前端按 event 类型分流，元信息与正文不混
 *   - onError 走具名事件而非 completeWithError：后者只会断连，前端拿不到错误内容
 *   - send 包 try-catch：客户端断开后 send 抛异常，吞掉并记日志，避免污染回调线程
 */
@SpringBootApplication
@RestController
public class SseChatServer {

    /** 无记忆流式助手：无状态、线程安全，全局单例共享 */
    interface StreamAssistant {
        TokenStream chat(String question);   // 单参数接口可省 @UserMessage
    }

    private final StreamAssistant assistant;

    public SseChatServer() {
        StreamingChatModel model = Models.streaming();
        this.assistant = AiServices.builder(StreamAssistant.class)
                .streamingChatModel(model)
                .build();
    }

    /** curl -N -G "http://localhost:8080/chat/stream" --data-urlencode "q=用一句话介绍SSE" **/
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestParam String q) {
        SseEmitter emitter = new SseEmitter(120_000L);   // 2 分钟超时兜底

        emitter.onCompletion(() -> System.out.println("[sse] 连接正常完成"));
        emitter.onTimeout(() -> System.out.println("[sse] 连接超时关闭"));

        TokenStream stream = assistant.chat(q);
        stream.onPartialResponse(token -> sendSafely(emitter, "token", token))
              .onCompleteResponse(r -> {
                  sendSafely(emitter, "done", String.valueOf(r.tokenUsage()));
                  emitter.complete();                     // 主动收尾：立即释放连接
              })
              .onError(e -> {
                  sendSafely(emitter, "error",
                          e.getMessage() == null ? e.toString() : e.getMessage());
                  emitter.complete();
              })
              .start();
        return emitter;   // Servlet 线程到此为止，后续全由回调线程驱动
    }

    private void sendSafely(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data, MediaType.TEXT_PLAIN));
        } catch (IOException | IllegalStateException e) {
            // 客户端已断开（刷新页面/关标签页）：到这里清理即可，不必再 complete
            System.out.println("[sse] 客户端断开: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SseChatServer.class, args);
    }
}
