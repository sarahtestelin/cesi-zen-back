package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.*;
import com.cesi_zen_back.cesi_zen_back.entity.*;
import com.cesi_zen_back.cesi_zen_back.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticServiceImplTest {

    @Mock private DiagnosticQuestionRepository questionRepository;
    @Mock private DiagnosticResultRepository resultRepository;
    @Mock private DiagnosticResultConfigRepository resultConfigRepository;
    @Mock private AppUserRepository appUserRepository;

    @InjectMocks private DiagnosticServiceImpl diagnosticService;

    private UUID questionId1;
    private UUID questionId2;
    private DiagnosticQuestion q1;
    private DiagnosticQuestion q2;
    private DiagnosticResultConfig lowConfig;
    private AppUser user;

    @BeforeEach
    void setUp() {
        questionId1 = UUID.randomUUID();
        questionId2 = UUID.randomUUID();

        q1 = new DiagnosticQuestion();
        q1.setId(questionId1);
        q1.setQuestion("Changement important");
        q1.setScore(100);
        q1.setActive(true);
        q1.setCreatedAt(LocalDateTime.now().minusDays(2));

        q2 = new DiagnosticQuestion();
        q2.setId(questionId2);
        q2.setQuestion("Difficultés personnelles");
        q2.setScore(50);
        q2.setActive(true);
        q2.setCreatedAt(LocalDateTime.now().minusDays(1));

        lowConfig = new DiagnosticResultConfig();
        lowConfig.setId(UUID.randomUUID());
        lowConfig.setMinScore(0);
        lowConfig.setMaxScore(199);
        lowConfig.setLevel("FAIBLE");
        lowConfig.setMessage("Stress faible");
        lowConfig.setActive(true);

        Role role = Role.builder()
                .roleId(UUID.randomUUID())
                .roleName("USER")
                .build();

        user = AppUser.builder()
                .idUser(UUID.randomUUID())
                .mail("user@test.fr")
                .pseudo("Sarah")
                .hashedPassword("hash")
                .isActive(true)
                .lastConnexion(LocalDateTime.now())
                .role(role)
                .build();
    }

    @Test
    void listActiveQuestions_shouldReturnOnlyActiveQuestionsOrdered() {
        when(questionRepository.findByActiveTrueOrderByCreatedAtAsc())
                .thenReturn(List.of(q1, q2));

        List<DiagnosticQuestionResponseDto> result = diagnosticService.listActiveQuestions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(questionId1);
        assertThat(result.get(0).question()).isEqualTo("Changement important");
        assertThat(result.get(0).score()).isEqualTo(100);
    }

    @Test
    void calculateAnonymous_shouldCalculateScoreWithoutSavingResult() {
        DiagnosticRequestDto request = new DiagnosticRequestDto(List.of(questionId1, questionId2));

        when(questionRepository.findAllById(request.selectedQuestionIds()))
                .thenReturn(List.of(q1, q2));

        when(resultConfigRepository.findFirstByActiveTrueAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqual(150, 150))
                .thenReturn(Optional.of(lowConfig));

        DiagnosticResponseDto response = diagnosticService.calculateAnonymous(request);

        assertThat(response.resultId()).isNull();
        assertThat(response.finalScore()).isEqualTo(150);
        assertThat(response.level()).isEqualTo("FAIBLE");
        assertThat(response.message()).isEqualTo("Stress faible");

        verify(resultRepository, never()).save(any());
    }

    @Test
    void calculateAnonymous_shouldIgnoreInactiveQuestionsInScore() {
        q2.setActive(false);

        DiagnosticRequestDto request = new DiagnosticRequestDto(List.of(questionId1, questionId2));

        when(questionRepository.findAllById(request.selectedQuestionIds()))
                .thenReturn(List.of(q1, q2));

        when(resultConfigRepository.findFirstByActiveTrueAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqual(100, 100))
                .thenReturn(Optional.of(lowConfig));

        DiagnosticResponseDto response = diagnosticService.calculateAnonymous(request);

        assertThat(response.finalScore()).isEqualTo(100);
    }

    @Test
    void calculateAnonymous_shouldRejectUnknownQuestion() {
        DiagnosticRequestDto request = new DiagnosticRequestDto(List.of(questionId1, questionId2));

        when(questionRepository.findAllById(request.selectedQuestionIds()))
                .thenReturn(List.of(q1));

        assertThatThrownBy(() -> diagnosticService.calculateAnonymous(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Une ou plusieurs questions sont invalides");
    }

    @Test
    void calculateAndSave_shouldSaveResultForAuthenticatedUser() {
        DiagnosticRequestDto request = new DiagnosticRequestDto(List.of(questionId1, questionId2));
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user@test.fr")
                .build();

        UUID savedResultId = UUID.randomUUID();

        when(appUserRepository.findByMail("user@test.fr"))
                .thenReturn(Optional.of(user));

        when(questionRepository.findAllById(request.selectedQuestionIds()))
                .thenReturn(List.of(q1, q2));

        when(resultConfigRepository.findFirstByActiveTrueAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqual(150, 150))
                .thenReturn(Optional.of(lowConfig));

        when(resultRepository.save(any(DiagnosticResult.class)))
                .thenAnswer(invocation -> {
                    DiagnosticResult result = invocation.getArgument(0);
                    result.setId(savedResultId);
                    return result;
                });

        DiagnosticResponseDto response = diagnosticService.calculateAndSave(request, jwt);

        assertThat(response.resultId()).isEqualTo(savedResultId);
        assertThat(response.finalScore()).isEqualTo(150);

        ArgumentCaptor<DiagnosticResult> captor = ArgumentCaptor.forClass(DiagnosticResult.class);
        verify(resultRepository).save(captor.capture());

        assertThat(captor.getValue().getAppUser()).isEqualTo(user);
        assertThat(captor.getValue().getFinalScore()).isEqualTo(150);
        assertThat(captor.getValue().getLevel()).isEqualTo("FAIBLE");
    }

    @Test
    void calculateAndSave_shouldRejectUnauthenticatedUser() {
        DiagnosticRequestDto request = new DiagnosticRequestDto(List.of(questionId1));

        assertThatThrownBy(() -> diagnosticService.calculateAndSave(request, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Utilisateur non authentifié");

        verify(resultRepository, never()).save(any());
    }

    @Test
    void myResults_shouldReturnCurrentUserResults() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user@test.fr")
                .build();

        DiagnosticResult result = new DiagnosticResult();
        result.setId(UUID.randomUUID());
        result.setFinalScore(150);
        result.setLevel("FAIBLE");
        result.setMessage("Stress faible");
        result.setCreatedAt(LocalDateTime.now());

        when(appUserRepository.findByMail("user@test.fr"))
                .thenReturn(Optional.of(user));

        when(resultRepository.findByAppUserIdUserOrderByCreatedAtDesc(user.getIdUser()))
                .thenReturn(List.of(result));

        List<DiagnosticResultResponseDto> response = diagnosticService.myResults(jwt);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).finalScore()).isEqualTo(150);
        assertThat(response.get(0).level()).isEqualTo("FAIBLE");
    }

    @Test
    void createQuestion_shouldCreateActiveQuestion() {
        DiagnosticQuestionRequestDto request = new DiagnosticQuestionRequestDto("Nouvelle question", 42);

        when(questionRepository.save(any(DiagnosticQuestion.class)))
                .thenAnswer(invocation -> {
                    DiagnosticQuestion question = invocation.getArgument(0);
                    question.setId(UUID.randomUUID());
                    return question;
                });

        DiagnosticQuestionResponseDto response = diagnosticService.createQuestion(request);

        assertThat(response.question()).isEqualTo("Nouvelle question");
        assertThat(response.score()).isEqualTo(42);
        assertThat(response.active()).isTrue();
    }

    @Test
    void updateQuestion_shouldUpdateExistingQuestion() {
        DiagnosticQuestionRequestDto request = new DiagnosticQuestionRequestDto("Question modifiée", 80);

        when(questionRepository.findById(questionId1))
                .thenReturn(Optional.of(q1));

        when(questionRepository.save(q1))
                .thenReturn(q1);

        DiagnosticQuestionResponseDto response = diagnosticService.updateQuestion(questionId1, request);

        assertThat(response.question()).isEqualTo("Question modifiée");
        assertThat(response.score()).isEqualTo(80);
    }

    @Test
    void disableQuestion_shouldDisableExistingQuestion() {
        when(questionRepository.findById(questionId1))
                .thenReturn(Optional.of(q1));

        when(questionRepository.save(q1))
                .thenReturn(q1);

        DiagnosticQuestionResponseDto response = diagnosticService.disableQuestion(questionId1);

        assertThat(response.active()).isFalse();
    }

    @Test
    void enableQuestion_shouldEnableExistingQuestion() {
        q1.setActive(false);

        when(questionRepository.findById(questionId1))
                .thenReturn(Optional.of(q1));

        when(questionRepository.save(q1))
                .thenReturn(q1);

        DiagnosticQuestionResponseDto response = diagnosticService.enableQuestion(questionId1);

        assertThat(response.active()).isTrue();
    }

    @Test
    void deleteQuestion_shouldSoftDeleteQuestion() {
        when(questionRepository.findById(questionId1))
                .thenReturn(Optional.of(q1));

        diagnosticService.deleteQuestion(questionId1);

        assertThat(q1.isActive()).isFalse();
        verify(questionRepository).save(q1);
    }

    @Test
    void createResultConfig_shouldCreateActiveConfig() {
        DiagnosticResultConfigRequestDto request =
                new DiagnosticResultConfigRequestDto(0, 149, "FAIBLE", "Stress faible");

        when(resultConfigRepository.save(any(DiagnosticResultConfig.class)))
                .thenAnswer(invocation -> {
                    DiagnosticResultConfig config = invocation.getArgument(0);
                    config.setId(UUID.randomUUID());
                    return config;
                });

        DiagnosticResultConfigResponseDto response = diagnosticService.createResultConfig(request);

        assertThat(response.minScore()).isEqualTo(0);
        assertThat(response.maxScore()).isEqualTo(149);
        assertThat(response.level()).isEqualTo("FAIBLE");
        assertThat(response.active()).isTrue();
    }

    @Test
    void createResultConfig_shouldRejectInvalidRange() {
        DiagnosticResultConfigRequestDto request =
                new DiagnosticResultConfigRequestDto(300, 100, "INVALIDE", "Erreur");

        assertThatThrownBy(() -> diagnosticService.createResultConfig(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Le score maximum doit être supérieur ou égal au score minimum");

        verify(resultConfigRepository, never()).save(any());
    }
}