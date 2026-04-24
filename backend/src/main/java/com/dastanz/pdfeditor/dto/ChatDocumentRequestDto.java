package com.dastanz.pdfeditor.dto;

import lombok.Data;

@Data
public class ChatDocumentRequestDto {
    private String documentId;
    private String question;

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}