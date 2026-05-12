package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.AppUserDto;
import com.cesi_zen_back.cesi_zen_back.dto.UpdateCurrentUserDto;
import com.cesi_zen_back.cesi_zen_back.dto.UserDataExportDto;
import com.cesi_zen_back.cesi_zen_back.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping("/me")
    public AppUserDto getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return appUserService.getCurrentUser(jwt.getSubject());
    }

    @GetMapping("/me/export")
    public UserDataExportDto exportCurrentUserData(@AuthenticationPrincipal Jwt jwt) {
        return appUserService.exportCurrentUserData(jwt.getSubject());
    }

    @PutMapping("/me")
    public AppUserDto updateCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateCurrentUserDto dto
    ) {
        return appUserService.updateCurrentUser(jwt.getSubject(), dto);
    }

    @DeleteMapping("/me")
    public void anonymizeCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        appUserService.anonymizeCurrentUser(jwt.getSubject());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppUserDto> getAllUsers() {
        return appUserService.getAllUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AppUserDto getUserById(@PathVariable UUID id) {
        return appUserService.getUserById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AppUserDto updateUser(
            @PathVariable UUID id,
            @RequestBody AppUserDto appUserDto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return appUserService.updateUser(id, appUserDto, jwt.getSubject());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        appUserService.deleteUser(id, jwt.getSubject());
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public AppUserDto disableUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return appUserService.disableUser(id, jwt.getSubject());
    }

    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public AppUserDto enableUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return appUserService.enableUser(id, jwt.getSubject());
    }
}