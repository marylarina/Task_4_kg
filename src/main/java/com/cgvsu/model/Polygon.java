package com.cgvsu.model;

import java.util.ArrayList;
import java.util.List;

public class Polygon {

    private List<Integer> vertexIndices;
    private List<Integer> textureVertexIndices;
    private List<Integer> normalIndices;

    public Polygon() {
        vertexIndices = new ArrayList<>();
        textureVertexIndices = new ArrayList<>();
        normalIndices = new ArrayList<>();
    }

    public Polygon(final List<Integer> vertexIndices, final List<Integer> textureVertexIndices, final List<Integer> normalIndices) {
        this.vertexIndices = vertexIndices;
        this.textureVertexIndices = textureVertexIndices;
        this.normalIndices = normalIndices;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        Polygon other = (Polygon) obj;
        return this.getVertexIndices().equals(other.getVertexIndices())
                && this.getTextureVertexIndices().equals(other.getTextureVertexIndices())
                && this.getNormalIndices().equals(other.getNormalIndices());
    }

    public void setVertexIndices(final List<Integer> vertexIndices) {
        assert vertexIndices.size() >= 3;
        this.vertexIndices = vertexIndices;
    }

    public void setTextureVertexIndices(final List<Integer> textureVertexIndices) {
        //assert textureVertexIndices.size() >= 3;
        this.textureVertexIndices = textureVertexIndices;
    }

    public void setNormalIndices(final List<Integer> normalIndices) {
        //assert normalIndices.size() >= 3;
        this.normalIndices = normalIndices;
    }

    public void addVertexIndex(final int vertexIndex) {
        this.vertexIndices.add(vertexIndex);
    }

    public void addTextureVertexIndex(final int textureVertexIndex) {
        this.textureVertexIndices.add(textureVertexIndex);
    }

    public void addNormalIndex(final int normalIndex) {
        this.normalIndices.add(normalIndex);
    }

    public List<Integer> getVertexIndices() {
        return vertexIndices;
    }

    public List<Integer> getTextureVertexIndices() {
        return textureVertexIndices;
    }

    public List<Integer> getNormalIndices() {
        return normalIndices;
    }
}
