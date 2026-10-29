package lab.common;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.time.Duration;
import java.util.Arrays;

/**
 * 统一模型入口：GLM Coding Plan 套餐。
 *
 * 关键点（踩过坑的）：
 * 1. 套餐 key 必须走 Coding 专用端点 /api/coding/paas/v4，
 *    走普通按量端点 /api/paas/v4 会报 1309「Coding Plan 套餐已到期」；
 * 2. 套餐内可用模型以套餐为准；当前用 glm-5.3-flash（速度优先，适合教学 demo）；
 *    若报模型不存在，到 bigmodel.cn/claude-code 页面确认套餐支持的模型编码。
 * 3. 超时显式放宽到 180s：框架默认 60s，旗舰档模型深度思考时可能超；
 *    超时治理属"LLM 网关"问题，与业务代码正确性无关。
 * 供应商/端点/模型只改这一个文件——教材各章 demo 一律从这里取模型。
 */
public final class Models {

    private static final String BASE_URL = "https://open.bigmodel.cn/api/coding/paas/v4/";
    private static final String DEFAULT_MODEL = "glm-5.3-flash"; // 速度优先
    private static final Duration TIMEOUT = Duration.ofSeconds(180);

    private Models() {
    }

    public static ChatModel chat() {
        return chat(DEFAULT_MODEL, 0.2);
    }

    public static ChatModel chat(String modelName, double temperature) {
        return OpenAiChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(Keys.zhipu())
                .modelName(modelName)
                .temperature(temperature)
                .timeout(TIMEOUT)
                .build();
    }

    /** 可挂 ChatModelListener（07 章埋点用）：token/延迟/成本指标从这里进来 */
    public static ChatModel chat(String modelName, double temperature,
                                 ChatModelListener... listeners) {
        return OpenAiChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(Keys.zhipu())
                .modelName(modelName)
                .temperature(temperature)
                .timeout(TIMEOUT)
                .listeners(Arrays.asList(listeners))
                .build();
    }

    public static StreamingChatModel streaming() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(Keys.zhipu())
                .modelName(DEFAULT_MODEL)
                .timeout(TIMEOUT)
                .build();
    }
}
