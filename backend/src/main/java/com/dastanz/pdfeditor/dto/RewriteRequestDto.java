package com.dastanz.pdfeditor.dto;

public class RewriteRequestDto {
    private String originalText;
    private String instruction;

    public RewriteRequestDto() {
    }

    public RewriteRequestDto(String originalText, String instruction) {
        this.originalText = originalText;
        this.instruction = instruction;
    }

    public String getOriginalText() { return originalText; }
    public String getInstruction() { return instruction; }

    public void setOriginalText(String originalText) { this.originalText = originalText; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
}
