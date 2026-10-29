/**
 * 第01章：模型接入 / AiServices 声明式接口 / 流式输出 / 结构化输出
 */
package lab.ch01;

import dev.langchain4j.model.chat.ChatModel;
import lab.common.Models;

public class HelloLlm{
    public static void main(String[] args) {
        ChatModel model = Models.chat();

        String answer = model.chat("用一句话解释：Java 工程师为什么适合转 Agent 开发？");
        System.out.println(answer);
    }
}