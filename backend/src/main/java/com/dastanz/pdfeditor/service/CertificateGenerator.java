package com.dastanz.pdfeditor.service;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertificateGenerator {

    public static KeyPairAndCertificate generateSelfSignedCertificate() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        X500Name issuer = new X500Name("CN=AI Dastanz Editor PDF Signer, O=Dastanz Org, C=ID");
        X500Name subject = issuer;

        long now = System.currentTimeMillis();
        Date notBefore = new Date(now - 24 * 60 * 60 * 1000);
        Date notAfter = new Date(now + 365L * 24 * 60 * 60 * 1000); 

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                BigInteger.valueOf(now),
                notBefore,
                notAfter,
                subject,
                keyPair.getPublic()
        );

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .build(keyPair.getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(contentSigner);
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHolder);

        return new KeyPairAndCertificate(keyPair.getPrivate(), new Certificate[]{cert});
    }

    public static class KeyPairAndCertificate {
        private final PrivateKey privateKey;
        private final Certificate[] chain;

        public KeyPairAndCertificate(PrivateKey privateKey, Certificate[] chain) {
            this.privateKey = privateKey;
            this.chain = chain;
        }

        public PrivateKey getPrivateKey() { return privateKey; }
        public Certificate[] getChain() { return chain; }
    }
}