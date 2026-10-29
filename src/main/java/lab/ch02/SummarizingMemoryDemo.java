package lab.ch02;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import lab.common.Models;

/**
 * 滚动摘要 demo：自定义 RollingSummaryChatMemory（见同包同类），
 * 原文窗口有界，被挤出的旧消息自动压缩成"滚动摘要"置顶回灌。
 *
 * 场景：前 3 轮说关键信息 → 中间 6 轮闲聊制造窗口压力 → 再问早期信息。
 * 预期：早期信息"忘了原文、记得要点"（靠摘要答对），最近信息靠原文答对。
 */
public class SummarizingMemoryDemo {

    interface SupportBot {
        @SystemMessage("你是人工智能课程助教，能记住学员的信息。回答不超过两句话。")
        String chat(@UserMessage String message);   // 单会话 demo：不用 @MemoryId，直接挂单个 memory
    }

    public static void main(String[] args) {
        ChatModel mainModel = Models.chat();                      // 主对话
        ChatModel cheapModel = Models.chat("glm-5.3-flash", 0.1); // 摘要专用（生产换更便宜档位）

        // 只保留最近 4 条原文，更早的自动滚入摘要
        RollingSummaryChatMemory memory = new RollingSummaryChatMemory("s1", cheapModel, 4);

        SupportBot bot = AiServices.builder(SupportBot.class)
                .chatModel(mainModel)
                .chatMemory(memory)                               // 直接挂自定义 ChatMemory
                .build();

        // 第 1-3 轮：关键信息（稍后会被滚入摘要）
        bot.chat("我叫千早爱音，在学 LangChain4j，目标是 AI 应用开发岗");
        bot.chat("我的 GitHub 是 github.com/mygo-tomori，上面有一个自研 Coding Agent");
        bot.chat("我熟悉 Java 后端，坐标武汉");

        // 第 4-9 轮：闲聊制造窗口压力（每轮 2 条消息，会触发两次压缩）
        for (int i = 1; i <= 6; i++) {
            bot.chat("闲聊" + i + "：今天天气不错，聊点轻松的，比如美食和运动。");
        }

        // 此时前 3 轮原文已进摘要——看模型是"忘了"还是"靠摘要记得"
        System.out.println("== 提问早期信息（应靠摘要答对） ==");
        System.out.println(bot.chat("我的 GitHub 是什么？"));
        System.out.println("== 提问最近信息（应靠原文答对） ==");
        System.out.println(bot.chat("我上一句话问了你什么？"));
    }
}
