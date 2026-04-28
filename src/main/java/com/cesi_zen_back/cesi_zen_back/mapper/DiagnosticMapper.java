package com.cesi_zen_back.cesi_zen_back.mapper;

import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticQuestionResponseDto;
import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticQuestion;

public class DiagnosticMapper {

    private DiagnosticMapper() {
    }

    public static DiagnosticQuestionResponseDto toQuestionDto(DiagnosticQuestion question) {
        return new DiagnosticQuestionResponseDto(
                question.getId(),
                question.getQuestion(),
                question.getScore(),
                question.isActive(),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}