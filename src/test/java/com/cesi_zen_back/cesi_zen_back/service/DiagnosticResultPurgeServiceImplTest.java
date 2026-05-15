package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticResultPurgeServiceImplTest {

    @Mock
    private DiagnosticResultRepository diagnosticResultRepository;

    @InjectMocks
    private DiagnosticResultPurgeServiceImpl service;

    @Test
    void purgeOldDiagnosticResults_shouldReturnDeletedCount() {
        ReflectionTestUtils.setField(service, "diagnosticRetentionDays", 365);

        when(diagnosticResultRepository.deleteByCreatedAtBefore(any()))
                .thenReturn(4L);

        long deleted = service.purgeOldDiagnosticResults();

        assertThat(deleted).isEqualTo(4L);
        verify(diagnosticResultRepository).deleteByCreatedAtBefore(any());
    }
}
