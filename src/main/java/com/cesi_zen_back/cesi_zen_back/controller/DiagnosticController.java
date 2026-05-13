package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.*;
import com.cesi_zen_back.cesi_zen_back.service.DiagnosticService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/diagnostics")
@RequiredArgsConstructor
public class DiagnosticController {

    private final DiagnosticService diagnosticService;

    @GetMapping("/questions")
    public List<DiagnosticQuestionResponseDto> listActiveQuestions() {
        return diagnosticService.listActiveQuestions();
    }

    @PostMapping("/anonymous")
    public DiagnosticResponseDto calculateAnonymous(@Valid @RequestBody DiagnosticRequestDto request) {
        return diagnosticService.calculateAnonymous(request);
    }

    @PostMapping("/submit")
    public DiagnosticResponseDto calculateAndSave(
            @Valid @RequestBody DiagnosticRequestDto request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return diagnosticService.calculateAndSave(request, jwt);
    }

    @GetMapping("/results/me")
    public List<DiagnosticResultResponseDto> myResults(@AuthenticationPrincipal Jwt jwt) {
        return diagnosticService.myResults(jwt);
    }

    @GetMapping("/admin/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DiagnosticQuestionResponseDto> listAdminQuestions() {
        return diagnosticService.listAdminQuestions();
    }

    @PostMapping("/admin/questions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DiagnosticQuestionResponseDto createQuestion(@Valid @RequestBody DiagnosticQuestionRequestDto request) {
        return diagnosticService.createQuestion(request);
    }

    @PutMapping("/admin/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DiagnosticQuestionResponseDto updateQuestion(
            @PathVariable UUID id,
            @Valid @RequestBody DiagnosticQuestionRequestDto request
    ) {
        return diagnosticService.updateQuestion(id, request);
    }

    @DeleteMapping("/admin/questions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteQuestion(@PathVariable UUID id) {
        diagnosticService.deleteQuestion(id);
    }

    @GetMapping("/admin/result-configs")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DiagnosticResultConfigResponseDto> listResultConfigs() {
        return diagnosticService.listResultConfigs();
    }

    @PostMapping("/admin/result-configs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DiagnosticResultConfigResponseDto createResultConfig(
            @Valid @RequestBody DiagnosticResultConfigRequestDto request
    ) {
        return diagnosticService.createResultConfig(request);
    }

    @PutMapping("/admin/result-configs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DiagnosticResultConfigResponseDto updateResultConfig(
            @PathVariable UUID id,
            @Valid @RequestBody DiagnosticResultConfigRequestDto request
    ) {
        return diagnosticService.updateResultConfig(id, request);
    }

    @DeleteMapping("/admin/result-configs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteResultConfig(@PathVariable UUID id) {
        diagnosticService.deleteResultConfig(id);
    }
}