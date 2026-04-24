package com.dastanz.pdfeditor.dto;

import lombok.Data;

@Data
public class TranslateResponseDto {
    private String translatedText;

    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }
}