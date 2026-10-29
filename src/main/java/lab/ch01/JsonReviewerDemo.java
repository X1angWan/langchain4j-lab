package lab.ch01;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lab.common.Models;

import java.util.List;

public class JsonReviewerDemo {
    record Review(boolean valid, List<String> problems){}

    interface JsonReviewer {
        @SystemMessage("""
                你是 JSON 校验器。判断用户提供的文本是否为语法合法的 JSON，
                重点检查：尾逗号、单引号代替双引号、键没用双引号、括号未闭合。
                problems 中每条用一句话说明问题及大致位置；完全合法时返回空数组。
                只输出校验结论，不要输出其他解释。
                """)
        @UserMessage("校验这段文本：\n{{json}}")
        Review review(@V("json") String json);
    }

    public static void main(String[] args) {
        ChatModel model = Models.chat("glm-5.3", 0.1);

        JsonReviewer reviewer = AiServices.builder(JsonReviewer.class)
                .chatModel(model)
                .build();

        // 三个用例：合法 / 尾逗号 / 单引号（经典的"看起来像JSON但不是"）
        System.out.println(reviewer.review("{\"name\":\"zero\",\"age\":26}"));
        System.out.println(reviewer.review("{\"name\":\"zero\",,}"));
        System.out.println(reviewer.review("{'name':'zero'}"));
    }
}
