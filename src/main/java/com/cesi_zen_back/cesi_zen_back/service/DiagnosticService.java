package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.*;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

public interface DiagnosticService {

    List<DiagnosticQuestionResponseDto> listActiveQuestions();

    DiagnosticResponseDto calculateAnonymous(DiagnosticRequestDto request);

    DiagnosticResponseDto calculateAndSave(DiagnosticRequestDto request, Jwt jwt);

    List<DiagnosticResultResponseDto> myResults(Jwt jwt);

    List<DiagnosticQuestionResponseDto> listAdminQuestions();

    DiagnosticQuestionResponseDto createQuestion(DiagnosticQuestionRequestDto request);

    DiagnosticQuestionResponseDto updateQuestion(UUID id, DiagnosticQuestionRequestDto request);

    DiagnosticQuestionResponseDto enableQuestion(UUID id);

    DiagnosticQuestionResponseDto disableQuestion(UUID id);

    void deleteQuestion(UUID id);

    List<DiagnosticResultConfigResponseDto> listResultConfigs();

    DiagnosticResultConfigResponseDto createResultConfig(DiagnosticResultConfigRequestDto request);

    DiagnosticResultConfigResponseDto updateResultConfig(UUID id, DiagnosticResultConfigRequestDto request);

    DiagnosticResultConfigResponseDto enableResultConfig(UUID id);

    DiagnosticResultConfigResponseDto disableResultConfig(UUID id);

    void deleteResultConfig(UUID id);
}