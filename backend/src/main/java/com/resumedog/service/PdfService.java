package com.resumedog.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.resumedog.common.BusinessException;
import com.resumedog.domain.Resume;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

/**
 * 简历 PDF 导出。
 *
 * <p>为什么放在后端而不是浏览器端：简历是最终要投递出去的正式文件，
 * 需要跨操作系统、跨浏览器、跨版本输出完全一致的排版，还要可靠地嵌入中文字体。
 * 浏览器端导出（html2pdf / jsPDF）在这三点上都会出问题——尤其是中文字体嵌入和分页控制。
 */
@Slf4j
@Service
public class PdfService {

    private final TemplateEngine resumeTemplateEngine;
    private final TemplateService templateService;

    public PdfService(@Qualifier("resumeTemplateEngine") TemplateEngine resumeTemplateEngine,
                      TemplateService templateService) {
        this.resumeTemplateEngine = resumeTemplateEngine;
        this.templateService = templateService;
    }

    public byte[] render(Resume resume) {
        if (resume.getContent() == null) {
            throw new BusinessException(40000, "简历还没有内容，请先生成");
        }
        TemplateService.TemplateInfo template = templateService.require(resume.getTemplateCode());

        Context context = new Context();
        context.setVariable("content", resume.getContent());
        context.setVariable("title", resume.getTitle());
        context.setVariable("templateCode", template.code());

        String html = resumeTemplateEngine.process(template.file(), context);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("PDF 渲染失败，模板={}", template.code(), e);
            throw new BusinessException(50000, "PDF 渲染失败: " + e.getMessage(), e);
        }
    }

    public String fileName(Resume resume) {
        // 去掉文件名中的非法字符，避免部分浏览器下载时截断
        String safe = resume.getTitle().replaceAll("[\\\\/:*?\"<>|\\s]", "_");
        return safe + ".pdf";
    }
}
