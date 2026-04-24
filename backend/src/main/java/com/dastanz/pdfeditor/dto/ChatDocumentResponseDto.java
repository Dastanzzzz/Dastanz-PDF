package com.dastanz.pdfeditor.dto;

import lombok.Data;

@Data
public class ChatDocumentResponseDto {
    private String answer;

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}