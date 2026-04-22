package com.dastanz.pdfeditor.repository;

import com.dastanz.pdfeditor.model.DocumentMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentMetaRepository extends JpaRepository<DocumentMeta, UUID> {
}
