package com.resumedog.service;

import com.resumedog.config.PdfConfig;
import com.resumedog.domain.Resume;
import com.resumedog.domain.ResumeContent;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PDF 渲染冒烟测试。
 *
 * <p>这个测试存在的意义：简历 PDF 是整个产品唯一的交付物，
 * 而 OpenHTMLtoPDF 只实现 CSS 2.1 子集（不支持 flex/grid），
 * 中文字体还要靠系统回退链——这些都是「编译通过但运行炸掉」的地方。
 * 这个测试不碰数据库、不需要 API Key，纯本地就能守住这条链路。
 */
class PdfRenderSmokeTest {

    private final TemplateEngine engine = new PdfConfig().resumeTemplateEngine();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("经典模板能渲染出合法 PDF")
    void rendersClassicTemplate() throws Exception {
        byte[] pdf = renderTo("resume-classic", "classic");
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
        assertThat(extractText(pdf)).contains("张三").contains("订单中心");
    }

    @Test
    @DisplayName("现代模板能渲染出合法 PDF")
    void rendersModernTemplate() throws Exception {
        byte[] pdf = renderTo("resume-modern", "modern");
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
        assertThat(extractText(pdf)).contains("张三").contains("订单中心");
    }

    /**
     * 把 PDF 里的文字抽出来。
     *
     * <p>断言体积是没用的：OpenHTMLtoPDF 找不到中文字体时会静默丢字，
     * 照样能产出一个语法合法、却没有任何内容的「成功」PDF。
     * 只有把文字抽出来断言，才知道中文真的落到纸面上了。
     */
    private String extractText(byte[] pdf) throws IOException {
        // openhtmltopdf 1.0.10 带的 PDFBox 是 2.x，没有 3.x 的 Loader 入口
        try (PDDocument document = PDDocument.load(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }

    /**
     * 未知模板必须被拒绝，而不是渲染出一份空白 PDF 让用户以为导出成功了。
     */
    @Test
    @DisplayName("未知模板直接报错")
    void rejectsUnknownTemplate() {
        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> new TemplateService().require("no-such-template"))
                .isInstanceOf(com.resumedog.common.BusinessException.class)
                .hasMessageContaining("no-such-template");
    }

    /**
     * 简历内容要能原样存进 MySQL 的 JSON 列再读回来。
     * 字段一旦改名，这个往返就会失败——早发现比上线后查数据强。
     */
    @Test
    @DisplayName("ResumeContent 能无损 JSON 往返")
    void roundTripsThroughJson() throws Exception {
        ResumeContent original = sampleContent();
        String json = mapper.writeValueAsString(original);
        ResumeContent back = mapper.readValue(json, ResumeContent.class);

        assertThat(back).isEqualTo(original);
        assertThat(json).contains("\"targetPosition\"").contains("\"highlights\"");
    }

    /**
     * 把渲染结果写到 target/ 下，方便人工检查中文和分页效果。
     */
    @Test
    @DisplayName("导出样例 PDF 供人工检查")
    void writesSamplePdfs() throws Exception {
        Path dir = Path.of("target", "sample-resume");
        Files.createDirectories(dir);
        Files.write(dir.resolve("classic.pdf"), renderTo("resume-classic", "classic"));
        Files.write(dir.resolve("modern.pdf"), renderTo("resume-modern", "modern"));
        System.out.println("样例 PDF 已生成: " + dir.toAbsolutePath());
    }

    private byte[] renderTo(String templateFile, String templateCode) throws Exception {
        Context context = new Context();
        context.setVariable("content", sampleContent());
        context.setVariable("title", "样例简历");
        context.setVariable("templateCode", templateCode);

        String html = engine.process(templateFile, context);
        // 模板如果因为片段路径写错而渲染出空壳，这里能第一时间发现
        assertThat(html).contains("张三").contains("订单中心");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        }
    }

    private ResumeContent sampleContent() {
        // 走和 MockResumeContent 一样的数据形状，但独立一份，
        // 避免改了 mock 就悄悄让这个测试失去意义
        Resume resume = new Resume();
        resume.setTitle("样例简历");
        return new ResumeContent(
                new ResumeContent.BasicInfo("张三", "Java 后端工程师", "13800000000",
                        "zhangsan@example.com", "杭州", "", "3 年 Java 后端经验。"),
                List.of(new ResumeContent.EducationItem("某某大学", "计算机科学与技术", "本科",
                        "2018.09 - 2022.06", "3.6 / 4.0", List.of("国家奖学金"))),
                List.of(new ResumeContent.WorkItem("某某科技", "后端工程师", "2023.03 - 至今",
                        List.of("主导订单中心单元化改造，将单体应用拆分为 12 个独立部署单元，故障回滚时间从 30 分钟降至 3 分钟"))),
                List.of(),
                List.of(new ResumeContent.SkillGroup("后端", List.of("Java", "Spring Boot", "MySQL"))),
                List.of("2023 年度技术突破奖"));
    }
}
