package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;

public class PKISignature implements SignatureInterface {

    private final PrivateKey privateKey;
    private final Certificate[] certificateChain;

    public PKISignature(PrivateKey privateKey, Certificate[] certificateChain) {
        this.privateKey = privateKey;
        this.certificateChain = certificateChain;
    }

    @Override
    public byte[] sign(InputStream content) throws IOException {
        try {
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            X509Certificate cert = (X509Certificate) certificateChain[0];
            
            ContentSigner sha256Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(privateKey);
            gen.addSignerInfoGenerator(
                    new JcaSignerInfoGeneratorBuilder(
                            new JcaDigestCalculatorProviderBuilder().build())
                    .build(sha256Signer, cert));
            
            JcaCertStore certs = new JcaCertStore(Collections.singletonList(cert));
            gen.addCertificates(certs);
            
            CMSTypedData msg = new CMSProcessableByteArray(content.readAllBytes());
            CMSSignedData signedData = gen.generate(msg, false);
            
            return signedData.getEncoded();
        } catch (Exception e) {
            throw new IOException("Failed to sign PDF", e);
        }
    }
}