package lab.ch02;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 滚动摘要记忆：原文窗口有界，被挤出的旧消息压缩成"滚动摘要"置顶回灌。
 * 机制：add() 时原文超过 keepRecent+2 条 → 把最旧的超出部分滚入摘要；
 *      messages() 返回 = [摘要SystemMessage(若有)] + 最近 keepRecent 条原文。
 * 取舍（面试可讲）：摘要也耗 token，省的是"每轮全量重发历史"的大头；
 *      摘要是有损压缩——细节数字可能丢，所以压缩提示词强制保留人名/数字/决定。
 */
public class RollingSummaryChatMemory implements ChatMemory {

    private final Object id;
    private final ChatModel cheapModel;   // 摘要专用（便宜档、低温度）
    private final int keepRecent;         // 原文保留条数
    private final List<ChatMessage> recent = new ArrayList<>();
    private String rollingSummary;        // 滚动累积的摘要（null = 尚无）

    public RollingSummaryChatMemory(Object id, ChatModel cheapModel, int keepRecent) {
        this.id = id;
        this.cheapModel = cheapModel;
        this.keepRecent = keepRecent;
    }

    @Override
    public Object id() { return id; }

    @Override
    public synchronized void add(ChatMessage message) {
        recent.add(message);
        if (recent.size() > keepRecent + 2) {   // +2 是缓冲，避免每条都触发压缩
            compress();
        }
    }

    @Override
    public synchronized List<ChatMessage> messages() {
        List<ChatMessage> result = new ArrayList<>();
        if (rollingSummary != null) {
            result.add(SystemMessage.from("【此前对话的滚动摘要】" + rollingSummary
                    + "\n（早期对话细节若与本段冲突，以最近原文为准）"));
        }
        result.addAll(recent);
        return result;
    }

    @Override
    public synchronized void clear() {
        recent.clear();
        rollingSummary = null;
    }

    /** 把最旧的 (size-keepRecent) 条压入滚动摘要（摘要的摘要 → "滚动"的含义） */
    private void compress() {
        int overflow = recent.size() - keepRecent;
        List<ChatMessage> oldest = new ArrayList<>(recent.subList(0, overflow));
        recent.subList(0, overflow).clear();

        StringBuilder dialogue = new StringBuilder();
        for (ChatMessage m : oldest) dialogue.append(textOf(m)).append('\n');

        String prompt = """
                你是对话压缩器。请把"新增对话"压缩为不超过 120 字的要点，
                必须保留：人名、关键事实、数字、已做出的决定；丢弃寒暄与重复内容。
                %s新增对话：
                %s
                直接输出摘要本身，不要任何前缀。
                """.formatted(rollingSummary == null ? ""
                        : "已有摘要（请合并更新）：\n" + rollingSummary + "\n", dialogue);

        rollingSummary = cheapModel.chat(prompt).trim();
        System.out.println("[滚动摘要已更新] " + rollingSummary);   // 教学用：亲眼看压缩发生
    }

    private static String textOf(ChatMessage m) {
        if (m instanceof UserMessage um) return "用户: " + um.singleText();
        if (m instanceof AiMessage am) return "助手: " + am.text();
        return m.type().name();
    }
}
