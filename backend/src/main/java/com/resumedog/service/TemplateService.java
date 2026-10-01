package com.resumedog.service;

import com.resumedog.common.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 简历模板注册表。
 *
 * <p>模板用「编码 + 资源文件」的形式声明，新增模板就是往 {@code resources/resume-templates/}
 * 里放一个 HTML 并在这里加一行，不需要改任何渲染逻辑。
 */
@Service
public class TemplateService {

    public record TemplateInfo(String code, String name, String description, String file) {
    }

    private static final List<TemplateInfo> TEMPLATES = List.of(
            new TemplateInfo("classic", "经典黑白",
                    "稳重克制，不抢内容风头，适合投递国企、外企和传统行业。", "resume-classic"),
            new TemplateInfo("modern", "现代强调色",
                    "带一处主色做层级区分，适合互联网和技术岗，扫一眼就能抓到重点。", "resume-modern")
    );

    public List<TemplateInfo> list() {
        return TEMPLATES;
    }

    public TemplateInfo require(String code) {
        return TEMPLATES.stream()
                .filter(t -> t.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new BusinessException(40000, "未知模板: " + code));
    }
}
