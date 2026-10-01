package com.resumedog.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredOutputConfig;
import com.anthropic.models.messages.TextBlockParam;
import com.resumedog.config.ClaudeConfig;
import com.resumedog.config.ClaudeProperties;
import com.resumedog.domain.ResumeContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 简历生成的核心 AI 能力：经历碎片 -> 结构化简历。
 *
 * <p>用结构化输出（structured outputs）而不是让模型「输出 JSON 字符串再解析」：
 * 前者由 API 层保证返回值符合 schema，不会出现半截 JSON、围栏代码块或多余解释文字，
 * 后者每次都要写容错解析。这条差别在生产环境里决定了要不要重试。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeResumeService {

    private static final String SYSTEM_PROMPT_RESOURCE = "prompts/resume-writer-system.md";

    private final AnthropicClient anthropicClient;
    private final ClaudeProperties properties;

    /**
     * 读取并缓存系统提示词。这段文本在所有请求中完全一致，是 prompt caching 的最佳位置。
     */
    private volatile String cachedSystemPrompt;

    private String systemPrompt() {
        if (cachedSystemPrompt == null) {
            synchronized (this) {
                if (cachedSystemPrompt == null) {
                    cachedSystemPrompt = loadSystemPrompt();
                }
            }
        }
        return cachedSystemPrompt;
    }

    private String loadSystemPrompt() {
        try (InputStream in = new ClassPathResource(SYSTEM_PROMPT_RESOURCE).getInputStream()) {
            return new String(FileCopyUtils.copyToByteArray(in), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("无法读取系统提示词资源: " + SYSTEM_PROMPT_RESOURCE, e);
        }
    }

    /**
     * 把经历碎片改写成结构化简历。
     *
     * @param fragments      用户原始的经历碎片
     * @param targetPosition 目标岗位，模型据此决定详略取舍；为空则按通用技术岗处理
     */
    public ResumeContent generate(String fragments, String targetPosition) {
        if (properties.isMock()) {
            log.warn("claude.mock=true，跳过真实 API 调用，返回示例数据");
            return MockResumeContent.of(targetPosition);
        }

        // 结构化输出：SDK 从 ResumeContent 这个 record 自动推导 JSON Schema，
        // 返回值直接就是强类型的 ResumeContent，不需要手工解析。
        // effort 和 format 必须放在同一个 StructuredOutputConfig 上设置——
        // 连续调用两次 outputConfig(...) 会互相覆盖，先设的 format 会被抹掉。
        StructuredOutputConfig<ResumeContent> outputConfig = StructuredOutputConfig
                .<ResumeContent>builder()
                .format(ResumeContent.class)
                .effort(ClaudeConfig.effortOf(properties))
                .build();

        // 系统提示词打上缓存断点：简历写作规范是一段固定长文本，
        // 命中缓存后按约 1/10 的价格计费，是这个产品最有效的一处成本优化。
        CacheControlEphemeral cacheBreakpoint = ClaudeConfig.systemCacheBreakpoint(properties);

        StructuredMessageCreateParams<ResumeContent> params = MessageCreateParams.builder()
                .model(properties.getModel())
                .maxTokens(properties.getMaxTokens())
                .systemOfTextBlockParams(List.of(
                        TextBlockParam.builder()
                                .text(systemPrompt())
                                .cacheControl(cacheBreakpoint)
                                .build()))
                .outputConfig(outputConfig)
                .addUserMessage(buildUserPrompt(fragments, targetPosition))
                .build();

        StructuredMessage<ResumeContent> response = anthropicClient.messages().create(params);

        Optional<StopReason> stopReason = response.stopReason();
        if (stopReason.orElse(null) == StopReason.REFUSAL) {
            String category = response.stopDetails()
                    .flatMap(details -> details.category())
                    .map(com.anthropic.models.messages.RefusalStopDetails.Category::asString)
                    .orElse("UNKNOWN");
            throw new IllegalStateException("模型拒绝了本次请求，类别: " + category);
        }
        if (stopReason.orElse(null) == StopReason.MAX_TOKENS) {
            throw new IllegalStateException("简历内容过长，模型输出被 max_tokens 截断，请精简经历碎片");
        }

        logUsage(response);

        return response.content().stream()
                .map(block -> block.text().orElse(null))
                .filter(Objects::nonNull)
                .map(com.anthropic.models.messages.StructuredTextBlock::text)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("模型未返回结构化内容"));
    }

    private void logUsage(StructuredMessage<ResumeContent> response) {
        var usage = response.usage();
        if (usage == null) {
            return;
        }
        log.info("Claude 调用完成: model={}, input={} tokens, output={} tokens, cacheRead={}, cacheWrite={}",
                response.model(),
                usage.inputTokens(), usage.outputTokens(),
                usage.cacheReadInputTokens(), usage.cacheCreationInputTokens());
    }

    private String buildUserPrompt(String fragments, String targetPosition) {
        StringBuilder sb = new StringBuilder();
        if (targetPosition != null && !targetPosition.isBlank()) {
            sb.append("目标岗位：").append(targetPosition.trim()).append("\n\n");
        }
        sb.append("以下是我的经历碎片，请改写成一份专业的中文简历：\n\n<fragments>\n")
                .append(fragments == null ? "" : fragments.trim())
                .append("\n</fragments>");
        return sb.toString();
    }
}
