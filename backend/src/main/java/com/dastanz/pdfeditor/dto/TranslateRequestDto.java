package com.dastanz.pdfeditor.dto;

import lombok.Data;

@Data
public class TranslateRequestDto {
    private String originalText;
    private String targetLanguage;

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
}