package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.*;
import com.cesi_zen_back.cesi_zen_back.service.DiagnosticService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DiagnosticControllerTest {

    private MockMvc mockMvc;
    private DiagnosticService diagnosticService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private UUID questionId;
    private UUID resultConfigId;
    private UUID resultId;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        diagnosticService = mock(DiagnosticService.class);

        questionId = UUID.randomUUID();
        resultConfigId = UUID.randomUUID();
        resultId = UUID.randomUUID();

        jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user@test.fr")
                .build();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        MappingJackson2HttpMessageConverter jacksonConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new DiagnosticController(diagnosticService))
                .setValidator(validator)
                .setMessageConverters(jacksonConverter)
                .setCustomArgumentResolvers(jwtArgumentResolver())
                .build();
    }

    @Test
    void listActiveQuestions_shouldReturnQuestions() throws Exception {
        DiagnosticQuestionResponseDto question = new DiagnosticQuestionResponseDto(
                questionId,
                "Question de stress ?",
                50,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(diagnosticService.listActiveQuestions()).thenReturn(List.of(question));

        mockMvc.perform(get("/api/v1/diagnostics/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(questionId.toString()))
                .andExpect(jsonPath("$[0].question").value("Question de stress ?"))
                .andExpect(jsonPath("$[0].score").value(50));

        verify(diagnosticService).listActiveQuestions();
    }

    @Test
    void calculateAnonymous_shouldReturnDiagnosticResult() throws Exception {
        when(diagnosticService.calculateAnonymous(any(DiagnosticRequestDto.class)))
                .thenReturn(new DiagnosticResponseDto(null, 150, "MODERE", "Stress modéré"));

        String body = """
                {
                  "questionIds": ["%s"]
                }
                """.formatted(questionId);

        mockMvc.perform(post("/api/v1/diagnostics/anonymous")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalScore").value(150))
                .andExpect(jsonPath("$.level").value("MODERE"));

        verify(diagnosticService).calculateAnonymous(any(DiagnosticRequestDto.class));
    }

    @Test
    void calculateAndSave_shouldForwardJwtAndReturnSavedResult() throws Exception {
        when(diagnosticService.calculateAndSave(any(DiagnosticRequestDto.class), any(Jwt.class)))
                .thenReturn(new DiagnosticResponseDto(resultId, 200, "ELEVE", "Stress élevé"));

        String body = """
                {
                  "questionIds": ["%s"]
                }
                """.formatted(questionId);

        mockMvc.perform(post("/api/v1/diagnostics/submit")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultId").value(resultId.toString()))
                .andExpect(jsonPath("$.finalScore").value(200))
                .andExpect(jsonPath("$.level").value("ELEVE"));

        verify(diagnosticService).calculateAndSave(any(DiagnosticRequestDto.class), eq(jwt));
    }

    @Test
    void myResults_shouldReturnCurrentUserResults() throws Exception {
        DiagnosticResultResponseDto result = new DiagnosticResultResponseDto(
                resultId,
                150,
                "MODERE",
                "Stress modéré",
                LocalDateTime.now()
        );

        when(diagnosticService.myResults(jwt)).thenReturn(List.of(result));

        mockMvc.perform(get("/api/v1/diagnostics/results/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].finalScore").value(150))
                .andExpect(jsonPath("$[0].level").value("MODERE"));

        verify(diagnosticService).myResults(jwt);
    }

    @Test
    void createQuestion_shouldReturnCreated() throws Exception {
        DiagnosticQuestionResponseDto response = new DiagnosticQuestionResponseDto(
                questionId,
                "Nouvelle question",
                42,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(diagnosticService.createQuestion(any(DiagnosticQuestionRequestDto.class)))
                .thenReturn(response);

        String body = """
                {
                  "question": "Nouvelle question",
                  "score": 42
                }
                """;

        mockMvc.perform(post("/api/v1/diagnostics/admin/questions")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.question").value("Nouvelle question"))
                .andExpect(jsonPath("$.score").value(42));

        verify(diagnosticService).createQuestion(any(DiagnosticQuestionRequestDto.class));
    }

    @Test
    void updateQuestion_shouldCallService() throws Exception {
        DiagnosticQuestionResponseDto response = new DiagnosticQuestionResponseDto(
                questionId,
                "Question modifiée",
                80,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(diagnosticService.updateQuestion(eq(questionId), any(DiagnosticQuestionRequestDto.class)))
                .thenReturn(response);

        String body = """
                {
                  "question": "Question modifiée",
                  "score": 80
                }
                """;

        mockMvc.perform(put("/api/v1/diagnostics/admin/questions/{id}", questionId)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("Question modifiée"));

        verify(diagnosticService).updateQuestion(eq(questionId), any(DiagnosticQuestionRequestDto.class));
    }

    @Test
    void deleteQuestion_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/diagnostics/admin/questions/{id}", questionId))
                .andExpect(status().isNoContent());

        verify(diagnosticService).deleteQuestion(questionId);
    }

    @Test
    void createResultConfig_shouldReturnCreated() throws Exception {
        DiagnosticResultConfigResponseDto response = new DiagnosticResultConfigResponseDto(
                resultConfigId,
                0,
                149,
                "FAIBLE",
                "Stress faible",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(diagnosticService.createResultConfig(any(DiagnosticResultConfigRequestDto.class)))
                .thenReturn(response);

        String body = """
                {
                  "minScore": 0,
                  "maxScore": 149,
                  "level": "FAIBLE",
                  "message": "Stress faible"
                }
                """;

        mockMvc.perform(post("/api/v1/diagnostics/admin/result-configs")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level").value("FAIBLE"));

        verify(diagnosticService).createResultConfig(any(DiagnosticResultConfigRequestDto.class));
    }

    @Test
    void deleteResultConfig_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/diagnostics/admin/result-configs/{id}", resultConfigId))
                .andExpect(status().isNoContent());

        verify(diagnosticService).deleteResultConfig(resultConfigId);
    }

    private HandlerMethodArgumentResolver jwtArgumentResolver() {
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return Jwt.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(
                    MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
            ) {
                return jwt;
            }
        };
    }
}
