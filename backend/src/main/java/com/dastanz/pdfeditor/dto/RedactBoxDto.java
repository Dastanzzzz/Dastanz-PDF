package com.dastanz.pdfeditor.dto;

import lombok.Data;

@Data
public class RedactBoxDto {
    private int page;
    private float x;
    private float y;
    private float width;
    private float height;
    private String color;

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }
    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
