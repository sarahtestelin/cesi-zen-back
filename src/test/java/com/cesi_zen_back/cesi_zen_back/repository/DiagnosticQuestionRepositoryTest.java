package com.cesi_zen_back.cesi_zen_back.repository;

import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticQuestion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class DiagnosticQuestionRepositoryTest {

    @Autowired
    private DiagnosticQuestionRepository diagnosticQuestionRepository;

    @Test
    void findByActiveTrueOrderByCreatedAtAsc_shouldReturnQuestionsOrderedByCreationDate() {
        DiagnosticQuestion second = new DiagnosticQuestion();
        second.setQuestion("Question récente");
        second.setScore(20);
        second.setActive(true);
        second.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 0));

        DiagnosticQuestion first = new DiagnosticQuestion();
        first.setQuestion("Question ancienne");
        first.setScore(10);
        first.setActive(true);
        first.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

        diagnosticQuestionRepository.save(second);
        diagnosticQuestionRepository.save(first);

        assertThat(diagnosticQuestionRepository.findByActiveTrueOrderByCreatedAtAsc())
                .extracting(DiagnosticQuestion::getQuestion)
                .containsExactly("Question ancienne", "Question récente");
    }
}