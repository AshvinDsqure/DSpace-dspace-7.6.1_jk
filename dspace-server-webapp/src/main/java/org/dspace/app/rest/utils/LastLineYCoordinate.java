package org.dspace.app.rest.utils;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.List;

public class LastLineYCoordinate extends PDFTextStripper {
    private float lastLineY = -1;
    private int lastPage = -1;
    private String lastLine = "";

    public LastLineYCoordinate() throws IOException {
        super();
    }

    @Override
    protected void startPage(PDPage page) throws IOException {
        super.startPage(page);
        lastPage = getCurrentPageNo();
    }
    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        super.writeString(string, textPositions);
        if (!textPositions.isEmpty()) {
            float currentY = textPositions.get(0).getYDirAdj();
            if (getCurrentPageNo() == lastPage && currentY > lastLineY) {
                lastLineY = currentY;
                lastLine = string;
            }
        }
    }

    public float getLastLineY() {
        return lastLineY;
    }
    public String getLastLine() {
        return lastLine;
    }

}
