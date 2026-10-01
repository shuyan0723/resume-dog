package com.resumedog.service;

import com.resumedog.common.BusinessException;
import com.resumedog.domain.Resume;
import com.resumedog.domain.ResumeContent;
import com.resumedog.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ClaudeResumeService claudeResumeService;

    @Transactional(readOnly = true)
    public List<Resume> list(Long userId) {
        return resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Resume get(Long userId, Long resumeId) {
        return resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new BusinessException(40400, "简历不存在"));
    }

    @Transactional
    public Resume create(Long userId, String title, String targetPosition) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(title == null || title.isBlank() ? "我的简历" : title);
        resume.setTargetPosition(targetPosition);
        resume.setCreatedAt(Instant.now());
        resume.setUpdatedAt(Instant.now());
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume update(Long userId, Long resumeId, Resume patch) {
        Resume resume = get(userId, resumeId);
        if (patch.getTitle() != null) {
            resume.setTitle(patch.getTitle());
        }
        if (patch.getTargetPosition() != null) {
            resume.setTargetPosition(patch.getTargetPosition());
        }
        if (patch.getTemplateCode() != null) {
            resume.setTemplateCode(patch.getTemplateCode());
        }
        if (patch.getRawFragments() != null) {
            resume.setRawFragments(patch.getRawFragments());
        }
        if (patch.getContent() != null) {
            resume.setContent(patch.getContent());
        }
        return resumeRepository.save(resume);
    }

    /**
     * 调 Claude 把经历碎片改写成结构化简历并落库。
     */
    @Transactional
    public Resume generate(Long userId, Long resumeId, String fragments, String targetPosition) {
        Resume resume = get(userId, resumeId);

        String effectiveFragments = fragments != null ? fragments : resume.getRawFragments();
        if (effectiveFragments == null || effectiveFragments.isBlank()) {
            throw new BusinessException(40000, "请先填写经历碎片");
        }
        String effectivePosition = targetPosition != null ? targetPosition : resume.getTargetPosition();

        long start = System.currentTimeMillis();
        ResumeContent content = claudeResumeService.generate(effectiveFragments, effectivePosition);
        log.info("简历 {} 生成完成，耗时 {} ms", resumeId, System.currentTimeMillis() - start);

        resume.setRawFragments(effectiveFragments);
        resume.setTargetPosition(effectivePosition);
        resume.setContent(content);
        return resumeRepository.save(resume);
    }

    @Transactional
    public void delete(Long userId, Long resumeId) {
        Resume resume = get(userId, resumeId);
        resumeRepository.delete(resume);
    }
}
