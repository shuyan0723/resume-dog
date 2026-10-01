package com.resumedog.domain;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * 简历的完整结构化内容。
 *
 * <p>这个 record 有双重身份：
 * <ol>
 *   <li><b>Claude 结构化输出的 schema</b>：作为 {@code outputConfig(ResumeContent.class)} 的目标类型，
 *       SDK 会用 Jackson 从它自动推导 JSON Schema，模型返回的内容天然就是可直接入库的形状。</li>
 *   <li><b>持久化单元</b>：整体以 JSON 存入 {@code resumes.content} 列。</li>
 * </ol>
 *
 * <p>字段上的 {@link JsonPropertyDescription} 会直接进入 schema 的 description，
 * 是影响模型输出质量的地方——写清楚约束，比在 prompt 里反复强调更有效。
 */
public record ResumeContent(

        @JsonPropertyDescription("个人基本信息。summary 为 2-3 句的职业定位概述，突出与目标岗位的匹配度")
        BasicInfo basicInfo,

        @JsonPropertyDescription("教育经历，按时间倒序（最近的在前）。没有则返回空数组")
        List<EducationItem> education,

        @JsonPropertyDescription("工作经历，按时间倒序（最近的在前）。每段的 highlights 需为量化过的工作成果")
        List<WorkItem> work,

        @JsonPropertyDescription("项目经历，按重要性倒序。每条的 highlights 需描述技术方案与业务收益")
        List<ProjectItem> projects,

        @JsonPropertyDescription("技能清单，按类别分组，例如「后端」「前端」「工程效率」")
        List<SkillGroup> skills,

        @JsonPropertyDescription("荣誉奖项、竞赛名次、专利、公开文章等。没有则返回空数组")
        List<String> honors
) {

    public record BasicInfo(
            @JsonPropertyDescription("姓名") String name,
            @JsonPropertyDescription("求职意向岗位，例如「Java 后端工程师」") String targetPosition,
            @JsonPropertyDescription("手机号") String phone,
            @JsonPropertyDescription("邮箱") String email,
            @JsonPropertyDescription("所在城市") String city,
            @JsonPropertyDescription("个人主页或 GitHub 链接，没有则留空字符串") String homepage,
            @JsonPropertyDescription("2-3 句职业定位概述，面向目标岗位，突出核心优势与年限") String summary
    ) {
    }

    public record EducationItem(
            @JsonPropertyDescription("学校名称") String school,
            @JsonPropertyDescription("专业名称") String major,
            @JsonPropertyDescription("学历：专科 / 本科 / 硕士 / 博士") String degree,
            @JsonPropertyDescription("起止时间，格式 2021.09 - 2025.06") String period,
            @JsonPropertyDescription("GPA 或专业排名，没有则留空字符串") String gpa,
            @JsonPropertyDescription("与目标岗位相关的课程、奖项或 noteworthy 经历，没有则返回空数组") List<String> highlights
    ) {
    }

    public record WorkItem(
            @JsonPropertyDescription("公司名称") String company,
            @JsonPropertyDescription("职位名称") String position,
            @JsonPropertyDescription("在职起止时间，格式 2023.07 - 至今") String period,
            @JsonPropertyDescription("工作内容亮点，每条以动词开头，包含具体动作、技术手段与可量化结果（数字/百分比/规模）") List<String> highlights
    ) {
    }

    public record ProjectItem(
            @JsonPropertyDescription("项目名称") String name,
            @JsonPropertyDescription("担任角色，例如「后端负责人」") String role,
            @JsonPropertyDescription("项目时间，格式 2024.03 - 2024.08") String period,
            @JsonPropertyDescription("项目背景，一句话说明解决的问题") String description,
            @JsonPropertyDescription("技术栈，逗号分隔，例如「Spring Boot, Redis, MySQL」") String techStack,
            @JsonPropertyDescription("个人职责与成果，每条含量化结果") List<String> highlights
    ) {
    }

    public record SkillGroup(
            @JsonPropertyDescription("技能分类，例如「后端」「工程效率」") String category,
            @JsonPropertyDescription("该分类下的具体技能") List<String> items
    ) {
    }
}
