package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.AppUserDto;
import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class AppUserController {

    private final AppUserRepository repo;

    public AppUserController(AppUserRepository repo) {
        this.repo = repo;
    }

    private AppUserDto toDto(AppUser u) {
        return new AppUserDto(
                u.getId(),
                u.getMail(),
                u.getPseudo(),
                u.isAppUserIsActive(),
                u.getLastConnectionAt()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppUserDto create(@RequestBody AppUser u) {
        u.setId(null);
        return toDto(repo.save(u));
    }

    @GetMapping
    public List<AppUserDto> list() {
        return repo.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public AppUserDto get(@PathVariable UUID id) {
        return repo.findById(id)
                .map(this::toDto)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @PutMapping("/{id}")
    public AppUserDto update(@PathVariable UUID id, @RequestBody AppUser body) {
        AppUser existing = repo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        body.setId(existing.getId());
        return toDto(repo.save(body));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        repo.deleteById(id);
    }
}