package org.dspace.app.rest.utils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
public class PDFWaterMark {

    public static void main(String[] args) {
        try {
            System.out.println("in ddd");
            InputStream inputStream = new FileInputStream(new File("D:/a1.pdf"));
            InputStream img = new FileInputStream(new File("E://p.jpg"));
            addImageWatermarkToPdf(inputStream,img,"D://abc1.pdf");
            System.out.println("done!");
        }catch (Exception e){

e.printStackTrace();
        }
    }

    public static void  addImageWatermarkToPdf(InputStream pdfinputStream,InputStream image, String outputFileName) throws IOException {
        try (PDDocument document = PDDocument.load(pdfinputStream)) {
            BufferedImage bImage = ImageIO.read(image);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bImage, "png", baos);
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, baos.toByteArray(), null);
            float scaleX = bImage.getWidth() / pdImage.getWidth();
            float scaleY = bImage.getHeight() / pdImage.getHeight();
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                PDPage page = document.getPage(i);
                PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
                contentStream.drawImage(pdImage, 100, 100, pdImage.getWidth() * scaleX, pdImage.getHeight() * scaleY);
                contentStream.close();
            }
            document.save(outputFileName);
        }
    }
}
