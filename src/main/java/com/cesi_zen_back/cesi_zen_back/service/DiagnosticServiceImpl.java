package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.*;
import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticQuestion;
import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticResult;
import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticResultConfig;
import com.cesi_zen_back.cesi_zen_back.mapper.DiagnosticMapper;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticQuestionRepository;
import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticResultConfigRepository;
import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiagnosticServiceImpl implements DiagnosticService {

    private final DiagnosticQuestionRepository questionRepository;
    private final DiagnosticResultRepository resultRepository;
    private final DiagnosticResultConfigRepository resultConfigRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public List<DiagnosticQuestionResponseDto> listActiveQuestions() {
        return questionRepository.findByActiveTrueOrderByCreatedAtAsc()
                .stream()
                .map(DiagnosticMapper::toQuestionDto)
                .toList();
    }

    @Override
    public DiagnosticResponseDto calculateAnonymous(DiagnosticRequestDto request) {
        return calculateDiagnostic(request, null, false);
    }

    @Override
    public DiagnosticResponseDto calculateAndSave(DiagnosticRequestDto request, Jwt jwt) {
        AppUser user = getCurrentUser(jwt);
        return calculateDiagnostic(request, user, true);
    }

    @Override
    public List<DiagnosticResultResponseDto> myResults(Jwt jwt) {
        AppUser user = getCurrentUser(jwt);

        return resultRepository.findByAppUserIdUserOrderByCreatedAtDesc(user.getIdUser())
                .stream()
                .map(this::toResultDto)
                .toList();
    }

    @Override
    public List<DiagnosticQuestionResponseDto> listAdminQuestions() {
        return questionRepository.findAll()
                .stream()
                .map(DiagnosticMapper::toQuestionDto)
                .toList();
    }

    @Override
    public DiagnosticQuestionResponseDto createQuestion(DiagnosticQuestionRequestDto request) {
        DiagnosticQuestion question = new DiagnosticQuestion();
        question.setQuestion(request.question());
        question.setScore(request.score());
        question.setActive(true);

        return DiagnosticMapper.toQuestionDto(questionRepository.save(question));
    }

    @Override
    public DiagnosticQuestionResponseDto updateQuestion(UUID id, DiagnosticQuestionRequestDto request) {
        DiagnosticQuestion question = getQuestion(id);

        question.setQuestion(request.question());
        question.setScore(request.score());

        return DiagnosticMapper.toQuestionDto(questionRepository.save(question));
    }

    @Override
    public void deleteQuestion(UUID id) {
        DiagnosticQuestion question = getQuestion(id);
        questionRepository.delete(question);
    }

    @Override
    public List<DiagnosticResultConfigResponseDto> listResultConfigs() {
        return resultConfigRepository.findAllByOrderByMinScoreAsc()
                .stream()
                .map(this::toResultConfigDto)
                .toList();
    }

    @Override
    public DiagnosticResultConfigResponseDto createResultConfig(DiagnosticResultConfigRequestDto request) {
        validateResultConfig(request);

        DiagnosticResultConfig config = new DiagnosticResultConfig();
        applyResultConfigRequest(config, request);
        config.setActive(true);

        return toResultConfigDto(resultConfigRepository.save(config));
    }

    @Override
    public DiagnosticResultConfigResponseDto updateResultConfig(UUID id, DiagnosticResultConfigRequestDto request) {
        validateResultConfig(request);

        DiagnosticResultConfig config = getResultConfig(id);
        applyResultConfigRequest(config, request);

        return toResultConfigDto(resultConfigRepository.save(config));
    }

    @Override
    public void deleteResultConfig(UUID id) {
        DiagnosticResultConfig config = getResultConfig(id);
        resultConfigRepository.delete(config);
    }

    private DiagnosticResponseDto calculateDiagnostic(
            DiagnosticRequestDto request,
            AppUser user,
            boolean saveResult
    ) {
        List<DiagnosticQuestion> selectedQuestions = questionRepository.findAllById(request.questionIds());

        if (selectedQuestions.size() != request.questionIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une ou plusieurs questions sont invalides");
        }

        int finalScore = selectedQuestions.stream()
                .filter(DiagnosticQuestion::isActive)
                .mapToInt(DiagnosticQuestion::getScore)
                .sum();

        DiagnosticResultConfig config = resolveResultConfig(finalScore);

        UUID resultId = null;

        if (saveResult) {
            DiagnosticResult result = new DiagnosticResult();
            result.setFinalScore(finalScore);
            result.setLevel(config.getLevel());
            result.setMessage(config.getMessage());
            result.setAppUser(user);

            DiagnosticResult saved = resultRepository.save(result);
            resultId = saved.getId();
        }

        return new DiagnosticResponseDto(
                resultId,
                finalScore,
                config.getLevel(),
                config.getMessage()
        );
    }

    private DiagnosticQuestion getQuestion(UUID id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));
    }

    private DiagnosticResultConfig getResultConfig(UUID id) {
        return resultConfigRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La configuration des résultats introuvable"));
    }

    private DiagnosticResultConfig resolveResultConfig(int finalScore) {
        return resultConfigRepository
                .findFirstByActiveTrueAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqual(finalScore, finalScore)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Aucune configuration de résultat ne correspond au score"
                ));
    }

    private void validateResultConfig(DiagnosticResultConfigRequestDto request) {
        if (request.maxScore() < request.minScore()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le score maximum doit être supérieur ou égal au score minimum"
            );
        }
    }

    private void applyResultConfigRequest(
            DiagnosticResultConfig config,
            DiagnosticResultConfigRequestDto request
    ) {
        config.setMinScore(request.minScore());
        config.setMaxScore(request.maxScore());
        config.setLevel(request.level());
        config.setMessage(request.message());
    }

    private AppUser getCurrentUser(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }

        return appUserRepository.findByMail(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
    }

    private DiagnosticResultResponseDto toResultDto(DiagnosticResult result) {
        return new DiagnosticResultResponseDto(
                result.getId(),
                result.getFinalScore(),
                result.getLevel(),
                result.getMessage(),
                result.getCreatedAt()
        );
    }

    private DiagnosticResultConfigResponseDto toResultConfigDto(DiagnosticResultConfig config) {
        return new DiagnosticResultConfigResponseDto(
                config.getId(),
                config.getMinScore(),
                config.getMaxScore(),
                config.getLevel(),
                config.getMessage(),
                config.isActive(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }
}