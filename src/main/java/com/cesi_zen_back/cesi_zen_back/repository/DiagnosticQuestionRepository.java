package com.cesi_zen_back.cesi_zen_back.repository;

import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DiagnosticQuestionRepository extends JpaRepository<DiagnosticQuestion, UUID> {

    List<DiagnosticQuestion> findByActiveTrueOrderByCreatedAtAsc();
}