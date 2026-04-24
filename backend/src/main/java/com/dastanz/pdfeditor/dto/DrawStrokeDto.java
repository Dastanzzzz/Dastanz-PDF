package com.dastanz.pdfeditor.dto;

import lombok.Data;
import java.util.List;

@Data
public class DrawStrokeDto {
    private int page;
    private String color;
    private float thickness;
    private List<PointDto> points;

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public float getThickness() { return thickness; }
    public void setThickness(float thickness) { this.thickness = thickness; }
    public List<PointDto> getPoints() { return points; }
    public void setPoints(List<PointDto> points) { this.points = points; }
}