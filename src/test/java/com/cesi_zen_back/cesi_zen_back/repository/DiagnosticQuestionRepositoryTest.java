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
    void findByActiveTrueOrderByCreatedAtAsc_shouldReturnOnlyActiveQuestionsOrderedByCreationDate() {
        DiagnosticQuestion second = new DiagnosticQuestion();
        second.setQuestion("Question active récente");
        second.setScore(20);
        second.setActive(true);
        second.setCreatedAt(LocalDateTime.now().minusDays(1));

        DiagnosticQuestion first = new DiagnosticQuestion();
        first.setQuestion("Question active ancienne");
        first.setScore(10);
        first.setActive(true);
        first.setCreatedAt(LocalDateTime.now().minusDays(2));

        DiagnosticQuestion inactive = new DiagnosticQuestion();
        inactive.setQuestion("Question inactive");
        inactive.setScore(30);
        inactive.setActive(false);
        inactive.setCreatedAt(LocalDateTime.now().minusDays(3));

        diagnosticQuestionRepository.save(second);
        diagnosticQuestionRepository.save(first);
        diagnosticQuestionRepository.save(inactive);

        assertThat(diagnosticQuestionRepository.findByActiveTrueOrderByCreatedAtAsc())
                .extracting(DiagnosticQuestion::getQuestion)
                .containsExactly(
                        "Question active ancienne",
                        "Question active récente"
                );
    }
}