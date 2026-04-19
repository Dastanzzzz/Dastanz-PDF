package com.dastanz.pdfeditor.dto;

import com.dastanz.pdfeditor.service.PdfArrangeService.PageInstruction;

import java.util.List;

public class ArrangeRequestDto {
    private String documentId;
    private List<PageInstruction> pages;

    public ArrangeRequestDto() {}

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public List<PageInstruction> getPages() { return pages; }
    public void setPages(List<PageInstruction> pages) { this.pages = pages; }
}
