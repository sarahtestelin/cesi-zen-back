package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.entity.Survey;
import com.cesi_zen_back.cesi_zen_back.repository.SurveyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/surveys")
public class SurveyController {

    private final SurveyRepository repo;

    public SurveyController(SurveyRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Survey create(@RequestBody Survey s) {
        s.setId(null);
        return repo.save(s);
    }

    @GetMapping
    public List<Survey> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Survey get(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found"));
    }

    @PutMapping("/{id}")
    public Survey update(@PathVariable UUID id, @RequestBody Survey body) {
        Survey existing = get(id);
        body.setId(existing.getId());
        return repo.save(body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found");
        repo.deleteById(id);
    }
}