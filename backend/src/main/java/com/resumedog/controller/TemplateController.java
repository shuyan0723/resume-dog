package com.resumedog.controller;

import com.resumedog.dto.ApiResponse;
import com.resumedog.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Tag(name = "模板", description = "简历排版模板")
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    @Operation(summary = "可用模板列表")
    public ApiResponse<List<TemplateService.TemplateInfo>> list() {
        return ApiResponse.ok(templateService.list());
    }
}
