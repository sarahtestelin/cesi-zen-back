package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticResultPurgeServiceImplTest {

    @Mock
    private DiagnosticResultRepository diagnosticResultRepository;

    @InjectMocks
    private DiagnosticResultPurgeServiceImpl service;

    @Test
    void purgeOldDiagnosticResults_shouldDeleteResultsOlderThanRetentionDelay() {
        ReflectionTestUtils.setField(service, "diagnosticRetentionDays", 365);

        when(diagnosticResultRepository.deleteByCreatedAtBefore(any()))
                .thenReturn(4L);

        long deleted = service.purgeOldDiagnosticResults();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(diagnosticResultRepository).deleteByCreatedAtBefore(captor.capture());

        assertThat(deleted).isEqualTo(4L);
        assertThat(captor.getValue()).isBefore(LocalDateTime.now().minusDays(364));
        assertThat(captor.getValue()).isAfter(LocalDateTime.now().minusDays(366));
    }
}