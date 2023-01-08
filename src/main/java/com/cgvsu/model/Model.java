package com.cgvsu.model;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.objreader.ObjReaderException;

import java.util.*;

public class Model {

    public List<Vector3f> vertices;
    public List<Vector2f> textureVertices;
    public List<Vector3f> normals;
    public List<Polygon> polygons;

    public Model(final List<Vector3f> vertices, final List<Vector2f> textureVertices, final List<Vector3f> normals, final List<Polygon> polygons) {
        this.vertices = vertices;
        this.textureVertices = textureVertices;
        this.normals = normals;
        this.polygons = polygons;
    }

    public Model() {
        vertices = new ArrayList<>();
        textureVertices = new ArrayList<>();
        normals = new ArrayList<>();
        polygons = new ArrayList<>();
    }

    public boolean checkCorrectOfData() {
        for (int i = 0; i < polygons.size(); i++) {
            List<Integer> vertexIndices = polygons.get(i).getVertexIndices();
            List<Integer> textureVertexIndices = polygons.get(i).getTextureVertexIndices();
            List<Integer> normalIndices = polygons.get(i).getNormalIndices();

            //кол-во точек с кол-вом текстур
            if (vertexIndices.size()!=textureVertexIndices.size()
                    && vertexIndices.size() != 0 && textureVertexIndices.size() != 0) {
                throw new ObjReaderException.ObjContentException("Polygon data is incorrect.");
            }
            //кол-во точек с кол-вом нормалей
            if (vertexIndices.size()!= normalIndices.size()
                    && vertexIndices.size() != 0 && normalIndices.size()!=0) {
                throw new ObjReaderException.ObjContentException("Polygon data is incorrect.");
            }
            //кол-во нормалей и полигонов
            if (textureVertexIndices.size()!=normalIndices.size()
                    && textureVertexIndices.size()!=0 && normalIndices.size()!=0) {
                throw new ObjReaderException.ObjContentException("Polygon data is incorrect.");
            }

            //корректность листа с номерами точек
            for (Integer vertexIndex : vertexIndices) {
                if (vertexIndex > vertices.size()) {
                    throw new ObjReaderException.ObjContentException("Polygon parameter(vertex) is incorrect");
                }
            }

            for (Integer textureVertexIndex : textureVertexIndices) {
                if (textureVertexIndex > textureVertices.size()) {
                    throw new ObjReaderException.ObjContentException("Polygon parameter(texture vertex) is incorrect");
                }
            }

            for (Integer normalIndex : normalIndices) {
                if (normalIndex > normals.size()) {
                    throw new ObjReaderException.ObjContentException("Polygon parameter(normal) is incorrect");
                }
            }
        }
        return true;
    }

    public List<Vector3f> getVertices() {
        return vertices;
    }

    public void setVertices(final List<Vector3f> vertices) {
        this.vertices = vertices;
    }

    public List<Vector2f> getTextureVertices() {
        return textureVertices;
    }

    public void setTextureVertices(final List<Vector2f> textureVertices) {
        this.textureVertices = textureVertices;
    }

    public List<Vector3f> getNormals() {
        return normals;
    }

    public void setNormals(final List<Vector3f> normals) {
        this.normals = normals;
    }

    public List<Polygon> getPolygons() {
        return polygons;
    }

    public void setPolygons(final List<Polygon> polygons) {
        this.polygons = polygons;
    }
}
