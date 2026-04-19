package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class PdfPasswordService {

    /**
     * Encrypt a PDF with a user password (required to open) and an owner password (for permissions).
     *
     * @param pdfFile       the source PDF file
     * @param userPassword  password required to open the document
     * @param ownerPassword password for owner access (if null/empty, uses userPassword)
     * @param allowPrint    whether printing is permitted
     * @param allowCopy     whether copying text is permitted
     * @return              byte[] of the encrypted PDF
     */
    public byte[] addPassword(File pdfFile, String userPassword, String ownerPassword,
                               boolean allowPrint, boolean allowCopy) throws IOException {
        if (ownerPassword == null || ownerPassword.isEmpty()) {
            ownerPassword = userPassword;
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {
            AccessPermission permissions = new AccessPermission();
            permissions.setCanPrint(allowPrint);
            permissions.setCanExtractContent(allowCopy);
            permissions.setCanModify(false);

            StandardProtectionPolicy policy = new StandardProtectionPolicy(ownerPassword, userPassword, permissions);
            policy.setEncryptionKeyLength(128);

            document.protect(policy);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
