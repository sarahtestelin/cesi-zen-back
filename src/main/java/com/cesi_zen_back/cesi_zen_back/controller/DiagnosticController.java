package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticQuestionRequestDto;
import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticRequestDto;
import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticResultResponseDto;
import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticQuestion;
import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticResult;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticQuestionRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticResultRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/diagnostics")
public class DiagnosticController {

    private final DiagnosticQuestionRepository questionRepository;
    private final DiagnosticResultRepository resultRepository;
    private final AppUserRepository appUserRepository;

    public DiagnosticController(
            DiagnosticQuestionRepository questionRepository,
            DiagnosticResultRepository resultRepository,
            AppUserRepository appUserRepository
    ) {
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
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

        String level = resolveLevel(finalScore);
        String message = resolveMessage(finalScore);

        UUID resultId = null;

        if (saveResult) {
            DiagnosticResult result = new DiagnosticResult();
            result.setFinalScore(finalScore);
            result.setLevel(level);
            result.setMessage(message);
            result.setAppUser(user);

            DiagnosticResult saved = resultRepository.save(result);
            resultId = saved.getId();
        }

        return new DiagnosticResponseDto(resultId, finalScore, level, message);
    }

    private DiagnosticQuestion getQuestion(UUID id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));
    }

    private AppUser getCurrentUser(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }

        return appUserRepository.findByMail(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
    }

    private String resolveLevel(int finalScore) {
        if (finalScore < 150) {
            return "FAIBLE";
        }

        if (finalScore < 300) {
            return "MODERE";
        }

        return "ELEVE";
    }

    private String resolveMessage(int finalScore) {
        if (finalScore < 150) {
            return "Votre niveau de stress semble faible. Continuez à préserver votre équilibre au quotidien.";
        }

        if (finalScore < 300) {
            return "Votre niveau de stress semble modéré. Il peut être utile d'identifier les sources de tension et de mettre en place des actions de prévention.";
        }

        return "Votre niveau de stress semble élevé. Ce résultat n'est pas un diagnostic médical, mais il peut être utile d'en parler à un professionnel de santé.";
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
}