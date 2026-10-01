package com.resumedog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Claude API 相关配置，对应 application.yml 中的 {@code claude.*}。
 */
@ConfigurationProperties(prefix = "claude")
public class ClaudeProperties {

    /**
     * Anthropic API Key。留空时回落到环境变量 ANTHROPIC_API_KEY。
     */
    private String apiKey;

    /**
     * 生成简历使用的模型 ID。
     */
    private String model = "claude-opus-5-5";

    /**
     * 单次请求的 max_tokens。简历生成属长输出场景，默认给足。
     */
    private long maxTokens = 16000L;

    /**
     * 推理投入档位：LOW / MEDIUM / HIGH / XHIGH / MAX。
     * Claude Opus 5.5 的默认值是 MEDIUM，简历措辞质量对 HIGH 更友好。
     */
    private String effort = "HIGH";

    /**
     * 系统提示词的缓存有效期：5m / 1h。
     * 简历写作规范是一段固定的长文本，开启 prompt caching 后可显著降低单次成本。
     */
    private String cacheTtl = "1h";

    /**
     * 开发期开关：为 true 时不真正调用 API，返回内置示例数据，方便前端联调。
     */
    private boolean mock = false;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public long getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(long maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getEffort() {
        return effort;
    }

    public void setEffort(String effort) {
        this.effort = effort;
    }

    public String getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(String cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public boolean isMock() {
        return mock;
    }

    public void setMock(boolean mock) {
        this.mock = mock;
    }
}
