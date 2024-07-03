/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

public class DigitalSingPDF {

    public static void main(String[] args) throws Exception {

        String pdf="D://sample.pdf";
        String output="D://sample_siwngq111.pdf";
        AddDigitalSignClient(pdf,output,"Ashvin","Bihar","D://PATNAHIGHCOURT.p12","dspace123",1);
    }

    public static void AddDigitalSignClient(String pdfpath,String outputpath,String name,String reson,String PKCS12,String PKCS12password,int flag){
        try {
            // Add Bouncy Castle provider
            Security.addProvider(new BouncyCastleProvider());
            // Step 1: Check if the file is a valid PKCS12 file
            String pkcs12FilePath = PKCS12;// Path to your PKCS12 file
            FileInputStream n = new FileInputStream(new File(pkcs12FilePath));
            String p = PKCS12password;
            if (isPKCS12File(n, p)) {
                // Step 2: Set up signing using Bouncy Castle
                KeyStore keystore = KeyStore.getInstance("PKCS12", "BC");
                char[] password = PKCS12password.toCharArray(); // PKCS12 file password
                try (FileInputStream fis = new FileInputStream(pkcs12FilePath)) {
                    keystore.load(fis, password);
                }
                // Get private key and certificate from the keystore
                String alias = (String) keystore.aliases().nextElement();
                PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, password);
                X509Certificate certificate = (X509Certificate) keystore.getCertificate(alias);
                Certificate certificate1 = keystore.getCertificate(alias);

                // Load the document
                try (PDDocument doc = PDDocument.load(new File(pdfpath))) {
                    if(flag==1){
                        addDigitalSignatureUploaduser(doc,name,reson);}else{
                        addDigitalSignatureDownloderUSer(doc,name,reson);

                    }
                    // Save the signed PDF document
                    doc.save(new FileOutputStream(outputpath));
                    System.out.println("save!");
                }

            } else {
                System.err.println("Not a valid PKCS12 file.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void addDigitalSignatureUploaduser(PDDocument doc,String name,String reson)
            throws IOException {

        // Get the last page
        int lastPage = doc.getNumberOfPages() - 1;
        PDPage page = doc.getPage(lastPage);

        PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
        if (acroForm == null) {
            acroForm = new PDAcroForm(doc);
            doc.getDocumentCatalog().setAcroForm(acroForm);
        }
        float x = 280;
        float y = 600;
        float width = 215;
        float height = 60;
        // Create a signature rectangle
        PDSignatureField signatureField = new PDSignatureField(acroForm);
        PDAnnotationWidget widget = signatureField.getWidgets().get(0);
        PDRectangle rect = new PDRectangle(x,y , width, height); // Define the rectangle size here
        widget.setRectangle(rect);
        page.getAnnotations().add(widget);

        // Create the signature
        // Set the appearance for the signature field
        PDAppearanceDictionary appearanceDict = new PDAppearanceDictionary();
        PDAppearanceStream appearanceStream = new PDAppearanceStream(doc);
        appearanceStream.setResources(new PDResources());
        appearanceStream.setBBox(rect);
        appearanceDict.setNormalAppearance(appearanceStream);
        widget.setAppearance(appearanceDict);

        // Create a visual representation of the signature
        // You can customize this as per your requirements
        try (PDPageContentStream contentStream = new PDPageContentStream(doc, page,
                PDPageContentStream.AppendMode.APPEND, false)) {
            contentStream.addRect(x, y, width, height);
            //contentStream.stroke();
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(x + 10, y + height - 20); // Adjust text position
            contentStream.showText("Digital Sign by : "+name);
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Date:"+new Date());
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Reason:"+reson);
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Location: Location");
            contentStream.endText();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void addDigitalSignatureDownloderUSer(PDDocument doc,String name,String reson)
            throws IOException {

        // Get the last page
        int lastPage = doc.getNumberOfPages() - 1;
        PDPage page = doc.getPage(lastPage);

        PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
        if (acroForm == null) {
            acroForm = new PDAcroForm(doc);
            doc.getDocumentCatalog().setAcroForm(acroForm);
        }
        float x = 20;
        float y = 300;
        float width = 215;
        float height = 60;
        // Create a signature rectangle
        PDSignatureField signatureField = new PDSignatureField(acroForm);
        PDAnnotationWidget widget = signatureField.getWidgets().get(0);
        PDRectangle rect = new PDRectangle(x,y , width, height); // Define the rectangle size here
        widget.setRectangle(rect);
        page.getAnnotations().add(widget);

        // Create the signature
        // Set the appearance for the signature field
        PDAppearanceDictionary appearanceDict = new PDAppearanceDictionary();
        PDAppearanceStream appearanceStream = new PDAppearanceStream(doc);
        appearanceStream.setResources(new PDResources());
        appearanceStream.setBBox(rect);
        appearanceDict.setNormalAppearance(appearanceStream);
        widget.setAppearance(appearanceDict);

        // Create a visual representation of the signature
        // You can customize this as per your requirements
        try (PDPageContentStream contentStream = new PDPageContentStream(doc, page,
                PDPageContentStream.AppendMode.APPEND, false)) {
            contentStream.addRect(x, y, width, height);
            // contentStream.stroke();
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(x + 10, y + height - 20); // Adjust text position
            contentStream.showText("Digital Sign by :"+name);
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Date:"+new Date());
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Reason:"+reson);
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Location: Location");
            contentStream.endText();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static boolean isPKCS12File(FileInputStream fis, String password) {
        try {
            KeyStore.getInstance("PKCS12", "BC").load(fis, password.toCharArray()); // Use a dummy password for
            // validation
            return true;
        } catch (IOException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
