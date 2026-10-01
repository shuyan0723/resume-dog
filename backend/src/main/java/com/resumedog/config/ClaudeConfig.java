package com.resumedog.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.OutputConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class ClaudeConfig {

    @Bean
    public AnthropicClient anthropicClient(ClaudeProperties properties) {
        var builder = AnthropicOkHttpClient.builder();
        // apiKey 留空则由 SDK 回落读取环境变量 ANTHROPIC_API_KEY
        if (StringUtils.hasText(properties.getApiKey())) {
            builder.apiKey(properties.getApiKey());
        }
        return builder.build();
    }

    /**
     * 系统提示词的缓存断点。简历写作规范是一段不随请求变化的长文本，
     * 放在 system 最前面并打上 ephemeral 断点，命中缓存后按约 1/10 价格计费。
     */
    public static CacheControlEphemeral systemCacheBreakpoint(ClaudeProperties properties) {
        var ttl = "1h".equalsIgnoreCase(properties.getCacheTtl())
                ? CacheControlEphemeral.Ttl.TTL_1H
                : CacheControlEphemeral.Ttl.TTL_5M;
        return CacheControlEphemeral.builder().ttl(ttl).build();
    }

    public static OutputConfig.Effort effortOf(ClaudeProperties properties) {
        return switch (properties.getEffort().toUpperCase()) {
            case "LOW" -> OutputConfig.Effort.LOW;
            case "MEDIUM" -> OutputConfig.Effort.MEDIUM;
            case "XHIGH" -> OutputConfig.Effort.XHIGH;
            case "MAX" -> OutputConfig.Effort.MAX;
            default -> OutputConfig.Effort.HIGH;
        };
    }
}
