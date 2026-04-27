package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticQuestionRequestDto;
import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticRequestDto;
import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticResultConfigRequestDto;
import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticResultConfigResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticResultResponseDto;
import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticQuestion;
import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticResult;
import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticResultConfig;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticQuestionRepository;
import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticResultConfigRepository;
import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticResultRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/diagnostics")
public class DiagnosticController {

    private final DiagnosticQuestionRepository questionRepository;
    private final DiagnosticResultRepository resultRepository;
    private final DiagnosticResultConfigRepository resultConfigRepository;
    private final AppUserRepository appUserRepository;

    public DiagnosticController(
            DiagnosticQuestionRepository questionRepository,
            DiagnosticResultRepository resultRepository,
            DiagnosticResultConfigRepository resultConfigRepository,
            AppUserRepository appUserRepository
    ) {
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
        this.resultConfigRepository = resultConfigRepository;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/questions")
    public List<DiagnosticQuestion> listActiveQuestions() {
        return questionRepository.findByActiveTrueOrderByCreatedAtAsc();
    }

    @PostMapping("/anonymous")
    public DiagnosticResponseDto calculateAnonymous(@Valid @RequestBody DiagnosticRequestDto request) {
        return calculateDiagnostic(request, null, false);
    }

    @PostMapping("/submit")
    public DiagnosticResponseDto calculateAndSave(
            @Valid @RequestBody DiagnosticRequestDto request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AppUser user = getCurrentUser(jwt);
        return calculateDiagnostic(request, user, true);
    }

    @GetMapping("/results/me")
    public List<DiagnosticResultResponseDto> myResults(@AuthenticationPrincipal Jwt jwt) {
        AppUser user = getCurrentUser(jwt);

        return resultRepository.findByAppUserIdUserOrderByCreatedAtDesc(user.getIdUser())
                .stream()
                .map(this::toResultDto)
                .toList();
    }

    @GetMapping("/admin/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DiagnosticQuestion> listAdminQuestions() {
        return questionRepository.findAll();
    }

    @PostMapping("/admin/questions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DiagnosticQuestion createQuestion(@Valid @RequestBody DiagnosticQuestionRequestDto request) {
        DiagnosticQuestion question = new DiagnosticQuestion();
        question.setQuestion(request.question());
        question.setScore(request.score());
        question.setActive(true);

        return questionRepository.save(question);
    }

    @PutMapping("/admin/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DiagnosticQuestion updateQuestion(
            @PathVariable UUID id,
            @Valid @RequestBody DiagnosticQuestionRequestDto request
    ) {
        DiagnosticQuestion question = getQuestion(id);

        question.setQuestion(request.question());
        question.setScore(request.score());

        return questionRepository.save(question);
    }

    @PatchMapping("/admin/questions/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public DiagnosticQuestion enableQuestion(@PathVariable UUID id) {
        DiagnosticQuestion question = getQuestion(id);
        question.setActive(true);
        return questionRepository.save(question);
    }

    @PatchMapping("/admin/questions/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public DiagnosticQuestion disableQuestion(@PathVariable UUID id) {
        DiagnosticQuestion question = getQuestion(id);
        question.setActive(false);
        return questionRepository.save(question);
    }

    @DeleteMapping("/admin/questions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteQuestion(@PathVariable UUID id) {
        DiagnosticQuestion question = getQuestion(id);
        question.setActive(false);
        questionRepository.save(question);
    }

    @GetMapping("/admin/result-configs")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DiagnosticResultConfigResponseDto> listResultConfigs() {
        return resultConfigRepository.findAllByOrderByMinScoreAsc()
                .stream()
                .map(this::toResultConfigDto)
                .toList();
    }

    @PostMapping("/admin/result-configs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DiagnosticResultConfigResponseDto createResultConfig(
            @Valid @RequestBody DiagnosticResultConfigRequestDto request
    ) {
        validateResultConfig(request);

        DiagnosticResultConfig config = new DiagnosticResultConfig();
        applyResultConfigRequest(config, request);
        config.setActive(true);

        return toResultConfigDto(resultConfigRepository.save(config));
    }

    @PutMapping("/admin/result-configs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DiagnosticResultConfigResponseDto updateResultConfig(
            @PathVariable UUID id,
            @Valid @RequestBody DiagnosticResultConfigRequestDto request
    ) {
        validateResultConfig(request);

        DiagnosticResultConfig config = getResultConfig(id);
        applyResultConfigRequest(config, request);

        return toResultConfigDto(resultConfigRepository.save(config));
    }

    @PatchMapping("/admin/result-configs/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public DiagnosticResultConfigResponseDto enableResultConfig(@PathVariable UUID id) {
        DiagnosticResultConfig config = getResultConfig(id);
        config.setActive(true);
        return toResultConfigDto(resultConfigRepository.save(config));
    }

    @PatchMapping("/admin/result-configs/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public DiagnosticResultConfigResponseDto disableResultConfig(@PathVariable UUID id) {
        DiagnosticResultConfig config = getResultConfig(id);
        config.setActive(false);
        return toResultConfigDto(resultConfigRepository.save(config));
    }

    @DeleteMapping("/admin/result-configs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteResultConfig(@PathVariable UUID id) {
        DiagnosticResultConfig config = getResultConfig(id);
        config.setActive(false);
        resultConfigRepository.save(config);
    }

    private DiagnosticResponseDto calculateDiagnostic(
            DiagnosticRequestDto request,
            AppUser user,
            boolean saveResult
    ) {
        List<DiagnosticQuestion> selectedQuestions = questionRepository.findAllById(request.selectedQuestionIds());

        if (selectedQuestions.size() != request.selectedQuestionIds().size()) {
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Configuration de résultat introuvable"));
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