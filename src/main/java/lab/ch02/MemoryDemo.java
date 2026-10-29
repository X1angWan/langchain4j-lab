package lab.ch02;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import lab.common.Models;

public class MemoryDemo {
    interface  SupportBot {
        @SystemMessage("你是耐心的人工智能课程助教，记住学员说过的话,回答在30个字以内")
        String chat(@MemoryId String sessionId, @UserMessage String userMessage);
    }

    public static void main(String[] args) {
        ChatModel model = Models.chat();

        // 实验1：条数窗口，5 条上限
        SupportBot byMessages = AiServices.builder(SupportBot.class)
                .chatModel(model)
                .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(3))
                .build();

        // 实验2：token 窗口，粗略 800 token
        // 1.x 起必须显式传 TokenCountEstimator（各模型 tokenizer 不同，计数器已可插拔）；
        // OpenAI 计数器对中文是近似值，窗口本就是粗略预算，精确用量看 API 返回的 tokenUsage
        SupportBot byTokens = AiServices.builder(SupportBot.class)
                .chatModel(model)
                .chatMemoryProvider(id -> TokenWindowChatMemory.withMaxTokens(
                        10, new OpenAiTokenCountEstimator("gpt-4o-mini")))
                .build();


        String s = "s1";
        int k = 0;
        byMessages.chat(s, "我叫千早爱音，是一个吉他手,最熟的栈是 Java 后端，正在学 LangChain4j");
        for (int i = 1; i <= 2; i++) {
            byMessages.chat(s, "第" + i + "条闲聊消息：今天天气不错，聊点别的，比如饮食和运动。");
            k = i;
        }
        // 第 5 条已把自我介绍挤出 5 条窗口 → 模型答不出名字
        System.out.println("第" + k +"条数窗口还记得我叫什么吗？→ " + byMessages.chat(s, "我叫什么名字？"));

        String s2 = "s2";
        byTokens.chat(s2, "我是中专生仁菜，最熟的栈是 Java 后端，正在学 LangChain4j");
        System.out.println("token窗口（还记得）→ " + byTokens.chat(s2, "我叫什么名字？"));

        //多会话隔离验证
        SupportBot bot = AiServices.builder(SupportBot.class)
                .chatModel(model)
                .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(3))
                .build();

        System.out.println("多会话隔离验证→ " + bot.chat("alice", "我叫Alice"));
        System.out.println("多会话隔离验证→ " + bot.chat("bob", "我叫Bob"));
        System.out.println("多会话隔离验证→ " + bot.chat("alice", "我叫什么？"));
        System.out.println("多会话隔离验证→ " + bot.chat("bob", "我叫什么？"));


    }
}
