package com.dastanz.pdfeditor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RewriteResponseDto {
    @JsonProperty("edited_text")
    private String editedText;

    @JsonProperty("short_reason")
    private String shortReason;

    @JsonProperty("changed_terms")
    private List<String> changedTerms;

    public RewriteResponseDto() {
    }

    public RewriteResponseDto(String editedText, String shortReason, List<String> changedTerms) {
        this.editedText = editedText;
        this.shortReason = shortReason;
        this.changedTerms = changedTerms;
    }

    public String getEditedText() { return editedText; }
    public String getShortReason() { return shortReason; }
    public List<String> getChangedTerms() { return changedTerms; }

    public void setEditedText(String editedText) { this.editedText = editedText; }
    public void setShortReason(String shortReason) { this.shortReason = shortReason; }
    public void setChangedTerms(List<String> changedTerms) { this.changedTerms = changedTerms; }
}
