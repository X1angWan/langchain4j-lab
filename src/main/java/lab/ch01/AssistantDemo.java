package lab.ch01;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import lab.common.Models;

public class AssistantDemo {
    interface StudyAssistant {
        // 注解即提示词
        @SystemMessage("""
                你是 Java 工程师的 AI 应用学习助教。
                规则：回答不超过 5 句；优先给代码示例；术语第一次出现时用一句话解释。
                """)
        String chat(@UserMessage String question);
    }

    public static void main(String[] args) {
        ChatModel model = Models.chat();

        StudyAssistant assistant = AiServices.builder(StudyAssistant.class).chatModel(model).build();

        System.out.println(assistant.chat("AiServices 和直接调 chat() 有什么区别？"));
    }
}
