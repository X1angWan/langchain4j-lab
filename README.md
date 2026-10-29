# langchain4j-lab

《LangChain4j教程》配套实操工程：每章一个包（lab.ch01 ~ lab.ch08），公共工具在 lab.common。

## 环境要求

- JDK 21（17+ 即可），Maven 3.8+
- 环境变量 `ZHIPU_API_KEY`（智谱开放平台申请；`setx` 后需新开终端，IDEA 需完全重启）

## 运行示例

```bash
mvn compile
mvn exec:java -Dexec.mainClass="lab.ch01.HelloLlm"   # 各章 demo 以教程对应章节为准
```

## 学习路线

见《LangChain4教程》00 章路线图；面试答题层见《Agent面试P0资料》。

## 版本说明

依赖版本由 **langchain4j-bom 1.22.0** 统一管理（core/open-ai 稳定线，mcp/embeddings beta 线），升级只改 pom 中 BOM 的版本号。模型接入走智谱官方 OpenAI 兼容端点（`lab/common/Models.java`）。注意：`langchain4j-zhipu-ai` 专用模块停更于 0.36.x，与 1.x core 不兼容，勿引入。
