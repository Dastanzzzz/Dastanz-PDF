package com.dastanz.pdfeditor.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class FluidEditorService {

    public String extractFullText(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            StringBuilder html = new StringBuilder();

            for (int pageNum = 0; pageNum < document.getNumberOfPages(); pageNum++) {
                PDPage page = document.getPage(pageNum);
                
                // Keep frontend container exactly the size of the PDF page if possible, 
                // but at minimum provide a relative container for absolute children
                float pageHeight = page.getCropBox().getHeight();
                float pageWidth = page.getCropBox().getWidth();

                html.append(String.format(java.util.Locale.US, 
                    "<div class='pdf-page-content' style='position: relative; width: %.2fpx; height: %.2fpx; overflow: hidden; background-color: white;'>\n", 
                    pageWidth, pageHeight));

                // Extract text from page (absolute placed)
                String pageText = extractPageText(document, pageNum);

                // Extract images with physical placement
                String imagesHtml = extractImagesAbsolute(page);

                // Z-index: Images should be UNDER text elements so they act as background backgrounds / clipping layers
                html.append("<div style='position: absolute; width: 100%; height: 100%; top: 0; left: 0; z-index: 1;'>\n").append(imagesHtml).append("</div>\n");
                html.append("<div style='position: absolute; width: 100%; height: 100%; top: 0; left: 0; z-index: 2;'>\n").append(pageText).append("</div>\n");
                html.append("</div>\n");
            }

            return html.toString();
        }
    }

    private String extractPageText(PDDocument document, int pageNum) throws IOException {
        StringBuilder html = new StringBuilder();

        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
                if (string == null || string.trim().isEmpty() || textPositions.isEmpty()) {
                    return;
                }

                TextPosition first = textPositions.get(0);
                
                float x = first.getXDirAdj();
                float y = first.getYDirAdj() - first.getHeightDir() * 0.8f;     
                float fontSize = first.getFontSizeInPt();

                String fontFamily = "Times New Roman, serif";
                boolean isBold = false;
                if (first.getFont() != null && first.getFont().getName() != null) {
                    String fontName = first.getFont().getName().toLowerCase();  
                    if (fontName.contains("bold")) isBold = true;
                    if (fontName.contains("arial")) fontFamily = "Arial, sans-serif";
                    else if (fontName.contains("helvetica")) fontFamily = "Helvetica, sans-serif";
                    else if (fontName.contains("courier")) fontFamily = "Courier, monospace";
                }

                // Use exact font scaling for crisp, unmerged text elements
                html.append(String.format(java.util.Locale.US,
                    "<span style='position: absolute; left: %.2fpx; top: %.2fpx; font-size: %.2fpx; font-family: %s; %s line-height: 1.0; white-space: pre; margin: 0; padding: 0;'>",
                    x, y, fontSize, fontFamily, isBold ? "font-weight: bold;" : ""
                ));
                html.append(escapeHtml(string));
                html.append("</span>\n");
            }
        };

        // Do not merge spans based on arbitrary proximity, map blocks explicitly
        // By disabling sort, we draw text exactly in the logical flow rather than artificially gluing lines
        stripper.setSortByPosition(false);
        stripper.setStartPage(pageNum + 1);
        stripper.setEndPage(pageNum + 1);
        stripper.getText(document);

        return html.toString();
    }

    private String extractImagesAbsolute(PDPage page) {
        StringBuilder html = new StringBuilder();
        try {
            ImageLocationStripper imageStripper = new ImageLocationStripper(page);
            imageStripper.processPage(page);
            
            for (ImageLocationInfo info : imageStripper.getImages()) {
                String base64Image = bufferedImageToBase64(info.image);
                // Render images directly without strict clipping masks that cut into native graphics bounds
                // Use object-fit: contain to ensure full native bounds render accurately within the transformed box
                html.append(String.format(java.util.Locale.US,
                    "<img src='data:image/png;base64,%s' style='position: absolute; left: %.2fpx; top: %.2fpx; width: %.2fpx; height: %.2fpx; margin: 0; padding: 0; border: none; object-fit: contain; display: block; z-index: 1;' />\n",
                    base64Image, info.x, info.y, info.width, info.height        
                ));
            }
        } catch (Exception e) {
            System.err.println("Warning: failed to extract some images");
        }
        return html.toString();
    }

    private String bufferedImageToBase64(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public byte[] generatePdfFromHtml(String htmlContent) throws Exception {    
        try {
            // Convert all px to pt so OpenHTMLtoPDF scale accurately to 72DPI instead of 96DPI
            htmlContent = htmlContent.replace("px;", "pt;").replace("px\"", "pt\"").replace("px ", "pt ");
            // Strip out any shadow or border that the frontend editor injected for visualization
            htmlContent = htmlContent.replaceAll("border: 1px solid [^;]+;", "border: none;");
            htmlContent = htmlContent.replaceAll("box-shadow: [^;]+;", "box-shadow: none;");

            // Look for page width/height to set proper @page size
            String width = "595.28pt";
            String height = "841.89pt";
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("width:\\s*([0-9.]+)pt;\\s*height:\\s*([0-9.]+)pt;").matcher(htmlContent);
            if (m.find()) {
                width = m.group(1) + "pt";
                height = m.group(2) + "pt";
            }

            // Wrap content in proper HTML structure
            String wrappedHtml = "<!DOCTYPE html>" +
                    "<html><head><meta charset='UTF-8'><style>" +
                    "body { margin: 0; padding: 0; }" +
                    "@page { margin: 0; size: " + width + " " + height + "; }" +
                    ".pdf-page-content { page-break-after: always; margin: 0 !important; padding: 0 !important; border: none !important; box-shadow: none !important; }" +
                    "</style></head><body>" +
                    htmlContent +
                    "</body></html>";

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {      
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withHtmlContent(wrappedHtml, "file:///");
                builder.toStream(os);
                builder.run();

                byte[] result = os.toByteArray();
                if (result.length == 0) {
                    throw new Exception("Generated PDF is empty");
                }
                return result;
            }
        } catch (Exception e) {
            throw new Exception("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
    
    // Inner class to track image locations
    private static class ImageLocationInfo {
        BufferedImage image;
        float x;
        float y;
        float width;
        float height;
    }

    private static class ImageLocationStripper extends PDFStreamEngine {
        private final List<ImageLocationInfo> images = new ArrayList<>();
        private final float pageHeight;

        public ImageLocationStripper(PDPage page) {
            this.pageHeight = page.getCropBox().getHeight();
            // Basic operator mappings needed to track matrix state
            addOperator(new Concatenate());
            addOperator(new DrawObject());
            addOperator(new SetGraphicsStateParameters());
            addOperator(new Save());
            addOperator(new Restore());
            addOperator(new SetMatrix());
        }

        public List<ImageLocationInfo> getImages() {
            return images;
        }

        @Override
        protected void processOperator(Operator operator, List<org.apache.pdfbox.cos.COSBase> operands) throws IOException {
            if ("Do".equals(operator.getName())) {
                COSName objectName = (COSName) operands.get(0);
                PDXObject xobject = getResources().getXObject(objectName);
                if (xobject instanceof PDImageXObject) {
                    PDImageXObject image = (PDImageXObject) xobject;
                    Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
                    float imageWidth = Math.abs(ctmNew.getScalingFactorX());
                    float imageHeight = Math.abs(ctmNew.getScalingFactorY());

                    // Correctly compute physical boundaries taking inverted crop coordinates into account
                    float x = ctmNew.getTranslateX();
                    float y = pageHeight - ctmNew.getTranslateY() - imageHeight;

                    ImageLocationInfo info = new ImageLocationInfo();
                    info.image = image.getImage();
                    info.x = x;
                    info.y = y;
                    info.width = imageWidth;
                    info.height = imageHeight;
                    images.add(info);
                }
            } else {
                super.processOperator(operator, operands);
            }
        }
    }

}
