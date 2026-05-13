package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.AdminDashboardStatsDto;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticQuestionRepository;
import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticResultConfigRepository;
import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticResultRepository;
import com.cesi_zen_back.cesi_zen_back.repository.RessourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AppUserRepository appUserRepository;
    private final DiagnosticResultRepository diagnosticResultRepository;
    private final DiagnosticQuestionRepository diagnosticQuestionRepository;
    private final DiagnosticResultConfigRepository diagnosticResultConfigRepository;
    private final RessourceRepository ressourceRepository;

    @GetMapping("/stats")
    public AdminDashboardStatsDto getStats() {
        long totalUsers = appUserRepository.count();
        long activeUsers = appUserRepository.countByIsActiveTrue();

        return new AdminDashboardStatsDto(
                totalUsers,
                activeUsers,
                totalUsers - activeUsers,
                diagnosticResultRepository.count(),
                diagnosticQuestionRepository.count(),
                diagnosticResultConfigRepository.count(),
                ressourceRepository.count()
        );
    }
}