package com.dastanz.pdfeditor.dto;

import com.dastanz.pdfeditor.service.PdfCompareService.DiffEntry;

import java.util.List;

public class CompareResultDto {
    private List<DiffEntry> diffs;

    public CompareResultDto() {}

    public CompareResultDto(List<DiffEntry> diffs) {
        this.diffs = diffs;
    }

    public List<DiffEntry> getDiffs() { return diffs; }
    public void setDiffs(List<DiffEntry> diffs) { this.diffs = diffs; }
}
