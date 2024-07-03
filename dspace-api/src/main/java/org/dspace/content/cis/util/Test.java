//package org.dspace.content.cis.util;
//
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDPageContentStream;
//import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
//
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import javax.imageio.ImageIO;
//import java.nio.file.Files;
//
//
//public class Test {
//    public static void main(String[] args) {
//        try {
//            // Load the watermark image
//            BufferedImage watermarkImage = ImageIO.read(new File("image.png"));
//
//            // Load the existing PDF document
//            File file = new File("input.pdf");
//            PDDocument document = PDDocument.load(file);
//
//            // Get the number of pages in the PDF
//            int pageCount = document.getNumberOfPages();
//
//            // Add watermark to each page
//            for (int i = 0; i < pageCount; i++) {
//                PDPage page = document.getPage(i);
//                addWatermarkToPage(page, watermarkImage);
//            }
//
//            // Save the modified document
//            document.save("output.pdf");
//            document.close();
//
//            System.out.println("Watermark added successfully.");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void addWatermarkToPage(PDPage page, BufferedImage watermarkImage) throws IOException {
//        try (PDPageContentStream contentStream = new PDPageContentStream(page, PDPageContentStream.AppendMode.APPEND,true, true)) {
//
//            // Get the dimensions of the page
//            float pageWidth = page.getMediaBox().getWidth();
//            float pageHeight = page.getMediaBox().getHeight();
//
//            // Scale the watermark image to fit the page
//            float imageWidth = watermarkImage.getWidth();
//            float imageHeight = watermarkImage.getHeight();
//
//            float scalingFactor = Math.min(pageWidth / imageWidth, pageHeight / imageHeight);
//            imageWidth *= scalingFactor;
//            imageHeight *= scalingFactor;
//
//            // Calculate positioning for centering the watermark
//            float posX = (pageWidth - imageWidth) / 2;
//            float posY = (pageHeight - imageHeight) / 2;
//
//            // Add the watermark image to the page
//            PDImageXObject imageXObject = PDImageXObject.createFromByteArray(page.(), toByteArray(watermarkImage), "image");
//            contentStream.drawImage(imageXObject, posX, posY, imageWidth, imageHeight);
//        }
//    }
//
//    private static byte[] toByteArray(BufferedImage image) throws IOException {
//        File tempFile = File.createTempFile("temp", null);
//        ImageIO.write(image, "png", tempFile);
//        return Files.readAllBytes(tempFile.toPath());
//    }
//}
