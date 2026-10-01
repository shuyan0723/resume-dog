package com.resumedog.controller;

import com.resumedog.domain.Resume;
import com.resumedog.dto.ApiResponse;
import com.resumedog.security.CurrentUser;
import com.resumedog.service.PdfService;
import com.resumedog.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Tag(name = "简历", description = "简历的增删改查、AI 生成与 PDF 导出")
public class ResumeController {

    private final ResumeService resumeService;
    private final PdfService pdfService;

    public record CreateRequest(String title, String targetPosition) {
    }

    public record GenerateRequest(String fragments, String targetPosition) {
    }

    @GetMapping
    @Operation(summary = "我的简历列表")
    public ApiResponse<List<Resume>> list() {
        return ApiResponse.ok(resumeService.list(CurrentUser.id()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "简历详情")
    public ApiResponse<Resume> get(@PathVariable Long id) {
        return ApiResponse.ok(resumeService.get(CurrentUser.id(), id));
    }

    @PostMapping
    @Operation(summary = "新建简历（草稿）")
    public ApiResponse<Resume> create(@RequestBody(required = false) CreateRequest request) {
        String title = request == null ? null : request.title();
        String position = request == null ? null : request.targetPosition();
        return ApiResponse.ok(resumeService.create(CurrentUser.id(), title, position));
    }

    @PutMapping("/{id}")
    @Operation(summary = "保存简历（用户在前端手工改过内容后回写）")
    public ApiResponse<Resume> update(@PathVariable Long id, @RequestBody Resume patch) {
        return ApiResponse.ok(resumeService.update(CurrentUser.id(), id, patch));
    }

    @PostMapping("/{id}/generate")
    @Operation(summary = "AI 生成：经历碎片 -> 结构化简历。这是耗时最长、唯一会花钱的接口")
    public ApiResponse<Resume> generate(@PathVariable Long id,
                                        @Valid @RequestBody GenerateRequest request) {
        return ApiResponse.ok(resumeService.generate(
                CurrentUser.id(), id, request.fragments(), request.targetPosition()));
    }

    @GetMapping("/{id}/export")
    @Operation(summary = "导出 PDF")
    public ResponseEntity<ByteArrayResource> export(@PathVariable Long id) {
        Resume resume = resumeService.get(CurrentUser.id(), id);
        byte[] pdf = pdfService.render(resume);
        String encoded = URLEncoder.encode(pdfService.fileName(resume), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                // 同时给 filename(ASCII 兜底) 和 filename*(RFC 5987，中文名正确显示)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"resume.pdf\"; filename*=UTF-8''" + encoded)
                .body(new ByteArrayResource(pdf));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除简历")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        resumeService.delete(CurrentUser.id(), id);
        return ApiResponse.ok(null);
    }
}
