package com.dastanz.pdfeditor.model;

import java.util.List;

public class PageTextBlock {
    private int id;
    private int pageNumber;
    private String text;
    private float x;
    private float y;
    private float width;
    private float height;
    private float fontSize;
    private String fontFamily;
    
    // v2+ Expansion: Rich Interaction & Extracted Font
    private String originalFontId;
    private List<Float> polygonPoints; // [x1,y1, x2,y2, ...] for non-rectangular bounds

    public PageTextBlock() {
    }

    public PageTextBlock(int id, int pageNumber, String text, float x, float y, float width, float height, float fontSize, String fontFamily) {
        this.id = id;
        this.pageNumber = pageNumber;
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fontSize = fontSize;
        this.fontFamily = fontFamily;
    }

    public int getId() { return id; }
    public int getPageNumber() { return pageNumber; }
    public String getText() { return text; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getFontSize() { return fontSize; }
    public String getFontFamily() { return fontFamily; }
    public String getOriginalFontId() { return originalFontId; }
    public List<Float> getPolygonPoints() { return polygonPoints; }

    public void setId(int id) { this.id = id; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
    public void setText(String text) { this.text = text; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setWidth(float width) { this.width = width; }
    public void setHeight(float height) { this.height = height; }
    public void setOriginalFontId(String originalFontId) { this.originalFontId = originalFontId; }
    public void setPolygonPoints(List<Float> polygonPoints) { this.polygonPoints = polygonPoints; }
    public void setFontSize(float fontSize) { this.fontSize = fontSize; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
}
