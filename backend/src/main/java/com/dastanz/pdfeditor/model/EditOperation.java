package com.dastanz.pdfeditor.model;

public class EditOperation {
    private int blockId;
    private int pageNumber;
    private String originalText;
    private String newText;
    private float x;
    private float y;
    private float width;
    private float height;
    private float fontSize;

    public EditOperation() {
    }

    public EditOperation(int blockId, int pageNumber, String originalText, String newText, float x, float y, float width, float height, float fontSize) {
        this.blockId = blockId;
        this.pageNumber = pageNumber;
        this.originalText = originalText;
        this.newText = newText;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fontSize = fontSize;
    }

    public int getBlockId() { return blockId; }
    public int getPageNumber() { return pageNumber; }
    public String getOriginalText() { return originalText; }
    public String getNewText() { return newText; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getFontSize() { return fontSize; }

    public void setBlockId(int blockId) { this.blockId = blockId; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }
    public void setNewText(String newText) { this.newText = newText; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setWidth(float width) { this.width = width; }
    public void setHeight(float height) { this.height = height; }
    public void setFontSize(float fontSize) { this.fontSize = fontSize; }
}
