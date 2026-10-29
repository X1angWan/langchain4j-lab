package lab.common;

/**
 * 集中读取 API Key：密钥只存在于系统环境变量，代码与 Git 里永不出现。
 * 设置方法见《LangChain4j教程》00 章第 3.2 节。
 */
public final class Keys {

    private Keys() {
    }

    public static String zhipu() {
        String k = System.getenv("ZHIPU_API_KEY");
        if (k == null || k.isBlank()) {
            throw new IllegalStateException(
                    "未读到环境变量 ZHIPU_API_KEY；setx 后需新开终端窗口，IDEA 需完全重启后再运行。");
        }
        return k;
    }
}
