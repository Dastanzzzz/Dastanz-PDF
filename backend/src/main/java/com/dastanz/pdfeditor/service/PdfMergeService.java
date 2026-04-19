package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class PdfMergeService {

    /**
     * Merge multiple PDF input streams into a single PDF.
     *
     * @param inputStreams  ordered list of PDF input streams to merge
     * @return              byte[] of the merged PDF
     */
    public byte[] merge(List<InputStream> inputStreams) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();

        for (InputStream is : inputStreams) {
            merger.addSource(is);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        merger.setDestinationStream(out);
        merger.mergeDocuments(null); // null = use default memory settings

        return out.toByteArray();
    }
}
