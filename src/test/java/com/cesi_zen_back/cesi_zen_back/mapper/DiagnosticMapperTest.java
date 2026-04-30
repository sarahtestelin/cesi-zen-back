package com.cesi_zen_back.cesi_zen_back.mapper;

import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticQuestionResponseDto;
import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticQuestion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DiagnosticMapperTest {

    @Test
    void toQuestionDto_shouldMapQuestionFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        DiagnosticQuestion question = new DiagnosticQuestion();
        question.setId(id);
        question.setQuestion("Question ?");
        question.setScore(50);
        question.setActive(true);
        question.setCreatedAt(createdAt);
        question.setUpdatedAt(updatedAt);

        DiagnosticQuestionResponseDto dto = DiagnosticMapper.toQuestionDto(question);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.question()).isEqualTo("Question ?");
        assertThat(dto.score()).isEqualTo(50);
        assertThat(dto.active()).isTrue();
        assertThat(dto.createdAt()).isEqualTo(createdAt);
        assertThat(dto.updatedAt()).isEqualTo(updatedAt);
    }
}