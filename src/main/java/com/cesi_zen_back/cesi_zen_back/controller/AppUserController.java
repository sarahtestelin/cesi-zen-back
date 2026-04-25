package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.AppUserDto;
import com.cesi_zen_back.cesi_zen_back.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping
    public List<AppUserDto> getAllUsers() {
        return appUserService.getAllUsers();
    }

    @GetMapping("/{id}")
    public AppUserDto getUserById(@PathVariable UUID id) {
        return appUserService.getUserById(id);
    }

    @PutMapping("/{id}")
    public AppUserDto updateUser(
            @PathVariable UUID id,
            @RequestBody AppUserDto appUserDto
    ) {
        return appUserService.updateUser(id, appUserDto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        appUserService.deleteUser(id);
    }

    @PatchMapping("/{id}/disable")
    public AppUserDto disableUser(@PathVariable UUID id) {
        return appUserService.disableUser(id);
    }

    @PatchMapping("/{id}/enable")
    public AppUserDto enableUser(@PathVariable UUID id) {
        return appUserService.enableUser(id);
    }
}