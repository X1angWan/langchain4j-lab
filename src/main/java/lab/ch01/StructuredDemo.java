package lab.ch01;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lab.common.Models;

public class StructuredDemo {

    record TaskPlan(String goal, java.util.List<String> steps, String risk) {}

    interface Planner {
        @UserMessage("为这个开发任务制定执行计划：{{task}}")
        TaskPlan plan(@V("task") String task);   // @V 显式绑定模板变量，不依赖编译器保留参数名
    }

    public static void main(String[] args) {
        ChatModel model = Models.chat();

        Planner planner = AiServices.builder(Planner.class).chatModel(model).build();
        TaskPlan plan = planner.plan("给用户模块增加分页查询接口");
        System.out.println("目标: " + plan.goal());
        System.out.println("步骤: " + plan.steps());
        System.out.println("风险: " + plan.risk());
    }
}