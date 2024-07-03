package org.dspace.app.rest.utils;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;

public class DigitalSignatureInterface implements SignatureInterface {

    private PrivateKey privateKey;
    private Certificate[] certificateChain;

    public DigitalSignatureInterface(PrivateKey privateKey, Certificate[] certificateChain) {
        this.privateKey = privateKey;
        this.certificateChain = certificateChain;
    }

    @Override
    public byte[] sign(InputStream content) throws IOException {
        try {
            // Create a Signature instance using the private key
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);

            // Read the content of the document and update the Signature instance
            byte[] buffer = new byte[8192];
            int len;
            while ((len = content.read(buffer)) != -1) {
                signature.update(buffer, 0, len);
            }

            // Perform the actual signing
            byte[] signedData = signature.sign();

            // In a real-world scenario, you would handle the signed data appropriately
            // For example, you might want to encode it in Base64 or perform additional processing

            return signedData;
        } catch (Exception e) {
            throw new IOException("Error signing document content", e);
        }
    }
}

