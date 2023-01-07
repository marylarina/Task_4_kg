package com.cgvsu.objwriter;

import com.cgvsu.objwriter.ObjWriterException;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class ObjWriter {

    public static String write(final Model model) {
        String str = "";

        str += writeVertex(model.vertices);
        str += writeTextureVertex(model.textureVertices);
        str += writeNormals(model.normals);

        str += writePolygons(model.polygons);

        return str;
    }

    public static String writeVertex(final List<Vector3f> vertex) {
        StringBuilder s = new StringBuilder();
        for (Vector3f vector3f : vertex) {
            final String vx = String.format("%.2f", vector3f.getX()).replace(',', '.');
            final String vy = String.format("%.2f", vector3f.getY()).replace(',', '.');
            final String vz = String.format("%.2f", vector3f.getZ()).replace(',', '.');
            s.append("v  ").append(vx).append(" ").append(vy).append(" ").append(vz).append("\n");
        }
        s.append("# ").append(vertex.size()).append(" vertices");
        s.append("\n");
        s.append("\n");

        return s.toString();
    }

    public static String writeTextureVertex(final List<Vector2f> textureVertex) {
        StringBuilder s = new StringBuilder();
        for (Vector2f vector2f : textureVertex) {
            final String vtx = String.format("%.2f", vector2f.getX()).replace(',', '.');
            final String vty = String.format("%.2f", vector2f.getY()).replace(',', '.');
            s.append("vt ").append(vtx).append(" ").append(vty).append(" ").append(0.000).append("\n");
        }
        s.append("# ").append(textureVertex.size()).append(" texture vertices");
        s.append("\n");
        s.append("\n");
        return s.toString();
    }

    public static String writeNormals(final List<Vector3f> normals) {
        StringBuilder s = new StringBuilder();
        for (Vector3f normal : normals) {
            final String vnx = String.format("%.2f", normal.getX()).replace(',', '.');
            final String vny = String.format("%.2f", normal.getY()).replace(',', '.');
            final String vnz = String.format("%.2f", normal.getZ()).replace(',', '.');
            s.append("vn ").append(vnx).append(" ").append(vny).append(" ").append(vnz).append("\n");
        }
        s.append("# ").append(normals.size()).append(" normals");
        s.append("\n");
        s.append("\n");
        return s.toString();
    }

    public static String writePolygons(final List<Polygon> polygons) {
        StringBuilder str = new StringBuilder();
        for (Polygon polygon : polygons) {
            str.append("f ");
            for (int j = 0; j < polygon.getVertexIndices().size(); j++) {
                if (!polygon.getTextureVertexIndices().isEmpty() && polygon.getNormalIndices().isEmpty()) {
                    str.append(polygon.getVertexIndices().get(j) + 1).append("/").append(polygon.getTextureVertexIndices().get(j) + 1).append(" ");
                }
                if (polygon.getTextureVertexIndices().isEmpty() && polygon.getNormalIndices().isEmpty()) {
                    str.append(polygon.getVertexIndices().get(j) + 1).append(" ");
                }
                if (!polygon.getTextureVertexIndices().isEmpty() && !polygon.getNormalIndices().isEmpty()) {
                    str.append(polygon.getVertexIndices().get(j) + 1).append("/").append(polygon.getTextureVertexIndices().get(j) + 1).append("/").append(polygon.getNormalIndices().get(j) + 1).append(" ");
                }
                if (polygon.getTextureVertexIndices().isEmpty() && !polygon.getNormalIndices().isEmpty()) {
                    str.append(polygon.getVertexIndices().get(j) + 1).append("//").append(polygon.getNormalIndices().get(j) + 1).append(" ");
                }
            }
            str.append("\n");
        }
        str.append("# ").append(polygons.size()).append(" polygons");
        str.append("\n");
        str.append("\n");
        return str.toString();
    }
}

