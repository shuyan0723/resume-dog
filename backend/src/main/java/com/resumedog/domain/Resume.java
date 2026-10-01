package com.resumedog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * 一份简历。
 *
 * <p>内容用 {@link ResumeContent} 以 JSON 列存储，而不是拆成 education/work/project 等多张子表。
 * 理由有三：
 * <ul>
 *   <li>简历是「读多写多、几乎不按字段查询」的文档型数据，拆表只会带来无谓的 JOIN 与级联维护成本；</li>
 *   <li>AI 生成的字段会随模型能力演进而增删，JSON 列不需要迁移；</li>
 *   <li>整份简历天然是一个编辑单元，读改写一次即可，避免部分更新的不一致。</li>
 * </ul>
 * 代价是失去了「按技能检索所有简历」这类查询能力——如果后续要做人才库检索，
 * 再加一张倒排表（{@code resume_skill_index}）抽取 skills 字段即可，主表不用动。
 */
@Entity
@Table(name = "resumes", indexes = {
        @Index(name = "idx_resumes_user", columnList = "userId")
})
@Getter
@Setter
@NoArgsConstructor
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 用户给这份简历起的名字，例如「字节后端 - 2026 秋招」 */
    @Column(nullable = false, length = 128)
    private String title;

    /** 目标岗位，AI 生成时会针对它做措辞与重点取舍 */
    @Column(name = "target_position", length = 128)
    private String targetPosition;

    /** 模板编码，对应 resources/templates 下的 Thymeleaf 模板文件名 */
    @Column(name = "template_code", nullable = false, length = 64)
    private String templateCode = "classic";

    /** 原始的经历碎片，AI 生成的输入 */
    @Lob
    @Column(name = "raw_fragments", columnDefinition = "TEXT")
    private String rawFragments;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", columnDefinition = "json")
    private ResumeContent content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
