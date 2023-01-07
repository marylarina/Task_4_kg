package com.cgvsu.model;

import com.cgvsu.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static com.cgvsu.math.Vector3f.*;

public class ModelUtils {

    public static ArrayList<Polygon> triangulatePolygons(List<Polygon> initialList) {
        ArrayList<Polygon> convertedList = new ArrayList<>();
        for(Polygon polygon: initialList) {
            for (int i = 0; i < polygon.getVertexIndices().size() - 2; i++) {
                Polygon triangle = new Polygon();

                ArrayList<Integer> vert = new ArrayList<>();
                vert.add(polygon.getVertexIndices().get(0));
                vert.add(polygon.getVertexIndices().get(i + 1));
                vert.add(polygon.getVertexIndices().get(i + 2));
                triangle.setVertexIndices(vert);

                if (polygon.getTextureVertexIndices().size() != 0) {
                    ArrayList<Integer> text = new ArrayList<>();
                    text.add(polygon.getTextureVertexIndices().get(0));
                    text.add(polygon.getTextureVertexIndices().get(i + 1));
                    text.add(polygon.getTextureVertexIndices().get(i + 2));
                    triangle.setTextureVertexIndices(text);
                }

                if (polygon.getNormalIndices().size() != 0) {
                    ArrayList<Integer> norm = new ArrayList<>();
                    norm.add(polygon.getNormalIndices().get(0));
                    norm.add(polygon.getNormalIndices().get(i + 1));
                    norm.add(polygon.getNormalIndices().get(i + 2));
                    triangle.setNormalIndices(norm);
                }

                convertedList.add(triangle);
            }
        }
        return convertedList;
    }

    public static void recalculateNormals(Model model) {
        model.normals.clear();
        for (int i = 0; i < model.vertices.size(); i++) {
            model.normals.add(calculateNormalForVertexInModel(model, i));
        }
    }

    public static Vector3f calculateNormalForPolygon(final Polygon polygon, final Model model){

        List<Integer> vertexIndices = polygon.getVertexIndices();
        int verticesCount = vertexIndices.size();

        Vector3f vector1 = fromTwoPoints(model.vertices.get(vertexIndices.get(0)), model.vertices.get(vertexIndices.get(1)));
        Vector3f vector2 = fromTwoPoints(model.vertices.get(vertexIndices.get(0)), model.vertices.get(vertexIndices.get(verticesCount - 1)));

        return calculateCrossProduct(vector1, vector2);
    }

    protected static Vector3f calculateNormalForVertexInModel(final Model model, final int vertexIndex) {
        ArrayList<Vector3f> saved = new ArrayList<>();
        for (Polygon polygon : model.polygons) {
            if (polygon.getVertexIndices().contains(vertexIndex)) {
                saved.add(calculateNormalForPolygon(polygon, model));
            }
        }
        return sum(saved).divide(saved.size());
    }
}
