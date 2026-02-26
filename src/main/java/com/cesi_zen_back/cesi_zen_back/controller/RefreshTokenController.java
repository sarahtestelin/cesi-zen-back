package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.entity.RefreshToken;
import com.cesi_zen_back.cesi_zen_back.repository.RefreshTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/refresh-tokens")
public class RefreshTokenController {

    private final RefreshTokenRepository repo;

    public RefreshTokenController(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RefreshToken create(@RequestBody RefreshToken token) {
        token.setId(null);
        return repo.save(token);
    }

    @GetMapping
    public List<RefreshToken> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public RefreshToken get(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Refresh token not found"));
    }

    @PutMapping("/{id}")
    public RefreshToken update(@PathVariable UUID id, @RequestBody RefreshToken body) {
        RefreshToken existing = get(id);
        body.setId(existing.getId());
        return repo.save(body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Refresh token not found");
        }
        repo.deleteById(id);
    }
}