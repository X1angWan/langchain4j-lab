package lab.ch01;

import dev.langchain4j.model.chat.StreamingChatModel;
import lab.common.Models;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class StreamDemo {

    interface StreamAssistant {
        TokenStream chat(String question);   // 返回类型换成 TokenStream 即流式
    }

    public static void main(String[] args) throws Exception {
        StreamingChatModel model = Models.streaming();

        StreamAssistant assistant = AiServices.builder(StreamAssistant.class)
                .streamingChatModel(model)
                .build();

        // 流式是异步的：主线程必须等"完成/出错"放行，否则 JVM 提前退场，后台回调直接被杀
        CountDownLatch done = new CountDownLatch(1);

        TokenStream stream = assistant.chat("给我列出 Spring AI 和 LangChain4j 各三个差异点");
        stream.onPartialResponse(token -> System.out.print(token))   // 每个 token 到达时
                .onCompleteResponse(r -> {
                    System.out.println("\n[完成] tokens=" + r.tokenUsage());
                    done.countDown();                    // 完成事件 → 放行
                })
                .onError(e -> {
                    System.err.println("\n[出错] " + e.getMessage());
                    done.countDown();                    // 出错也放行
                })
                .start();

        // 最多等 120 秒；超时会打出明确提示，不再"静默无输出"
        if (!done.await(120, TimeUnit.SECONDS)) {
            System.err.println("[超时] 120 秒内未收到任何事件——检查端点/网络/模型名");
        }
    }
}