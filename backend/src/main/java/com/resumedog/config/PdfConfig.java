package com.resumedog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * 简历 PDF 用的独立 Thymeleaf 引擎。
 *
 * <p>不复用 Spring MVC 的引擎：Web 模板在 {@code templates/} 下由 @Controller 渲染成 HTML 响应，
 * 而简历模板需要走文件系统/类路径解析后直接交给 OpenHTMLtoPDF 渲染成二进制，
 * 两者解析策略和用途都不同，分开配置比在同一引擎上加条件清晰。
 */
@Configuration
public class PdfConfig {

    /**
     * 简历 HTML 模板的目录，与 Web 端模板隔离。
     *
     * <p>注意这里不能写成 {@code classpath:/resume-templates/}：
     * ClassLoaderTemplateResolver 是把 prefix + name + suffix 拼起来直接丢给
     * {@code ClassLoader.getResourceAsStream} 的，前缀里再带一层 {@code classpath:/}
     * 会让拼出来的名字多一层路径，运行时直接 FileNotFoundException。
     */
    public static final String RESUME_TEMPLATE_PREFIX = "resume-templates/";
    public static final String RESUME_TEMPLATE_SUFFIX = ".html";

    @Bean("resumeTemplateEngine")
    public TemplateEngine resumeTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(RESUME_TEMPLATE_PREFIX);
        resolver.setSuffix(RESUME_TEMPLATE_SUFFIX);
        resolver.setTemplateMode(org.thymeleaf.templatemode.TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
