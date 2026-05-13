package com.cesi_zen_back.cesi_zen_back.dto;

public record AdminDashboardStatsDto(
        long totalUsers,
        long activeUsers,
        long inactiveUsers,
        long totalDiagnostics,
        long totalDiagnosticQuestions,
        long totalResultConfigs,
        long totalResources
) {}