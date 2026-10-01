package com.resumedog.service;

import com.resumedog.domain.ResumeContent;

import java.util.List;

/**
 * 联调用的示例数据。仅在 {@code claude.mock=true} 时返回，
 * 让前端在没有 API Key 的情况下也能把「填碎片 → 生成 → 预览 → 导出」的链路跑通。
 */
final class MockResumeContent {

    private MockResumeContent() {
    }

    static ResumeContent of(String targetPosition) {
        return new ResumeContent(basicInfo(targetPosition), education(), work(), projects(), skills(), honors());
    }

    private static ResumeContent.BasicInfo basicInfo(String targetPosition) {
        return new ResumeContent.BasicInfo(
                "张三",
                targetPosition == null || targetPosition.isBlank() ? "Java 后端工程师" : targetPosition,
                "13800000000",
                "zhangsan@example.com",
                "杭州",
                "https://github.com/zhangsan",
                "3 年 Java 后端经验，主要方向是高并发交易系统与云原生基础设施。"
                        + "主导过日均千万级请求的服务治理与单元化改造，熟悉 Spring Boot / Redis / Kafka 技术栈，"
                        + "有从 0 到 1 搭建中台服务的完整经验。");
    }

    private static List<ResumeContent.EducationItem> education() {
        return List.of(
                new ResumeContent.EducationItem(
                        "某某大学", "计算机科学与技术", "本科", "2018.09 - 2022.06", "3.6 / 4.0",
                        List.of("国家奖学金（2019、2020）", "ACM 校赛二等奖")));
    }

    private static List<ResumeContent.WorkItem> work() {
        return List.of(
                new ResumeContent.WorkItem(
                        "某某科技有限公司", "后端工程师", "2023.03 - 至今",
                        List.of(
                                "主导订单中心单元化改造，将单体应用拆分为 12 个独立部署单元，发布频率从每周 1 次提升至每天 5 次，故障回滚时间从 30 分钟降至 3 分钟",
                                "设计并落地基于 Redis Lua 脚本的库存扣减方案，支撑大促峰值 8000 TPS，库存超卖率从 0.3% 降至 0",
                                "重构核心接口的慢查询治理，通过复合索引与读写分离将 P99 延迟从 780ms 降低至 120ms",
                                "搭建基于 Prometheus + Grafana 的可观测体系，覆盖 6 个核心服务，接入告警规则 23 条，MTTD 缩短 65%")));
    }

    private static List<ResumeContent.ProjectItem> projects() {
        return List.of(
                new ResumeContent.ProjectItem(
                        "实时风控引擎", "后端负责人", "2024.03 - 2024.11",
                        "为交易场景提供毫秒级风险识别能力，替代原有的人工审核流程",
                        "Spring Boot, Flink, Redis, Kafka, ClickHouse",
                        List.of(
                                "基于 Flink 构建实时特征计算链路，规则命中延迟稳定在 200ms 以内",
                                "设计规则热更新机制，运营可在不发版的情况下调整风控策略，配置耗时从 2 天缩短到 10 分钟",
                                "接入 Kafka 埋点与 ClickHouse 明细存储，日均处理事件 3.2 亿条，支撑策略回溯分析")));
    }

    private static List<ResumeContent.SkillGroup> skills() {
        return List.of(
                new ResumeContent.SkillGroup("后端", List.of("Java", "Spring Boot", "Spring Cloud", "MySQL", "Redis")),
                new ResumeContent.SkillGroup("分布式", List.of("Kafka", "RocketMQ", "Flink", "Elasticsearch")),
                new ResumeContent.SkillGroup("工程效率", List.of("Docker", "Kubernetes", "Jenkins", "GitLab CI", "Prometheus")));
    }

    private static List<String> honors() {
        return List.of("2023 年度技术突破奖", "发明专利 1 项：一种分布式库存一致性校验方法");
    }
}
