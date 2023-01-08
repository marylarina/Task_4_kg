package com.cgvsu.render_engine;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.model.ModelUtils;
import com.cgvsu.render_engine.triangle_rasterization.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
//import javax.vecmath.*;

import com.cgvsu.model.Model;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            Canvas canvas,
            Color color,
            boolean drawMesh,
            boolean useLighting,
            boolean texturePolygons,
            float[][] z_buffer) {
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            ArrayList<Vector2f> resultPoints = new ArrayList<>();
            ArrayList<Vector2f> textureVertices = new ArrayList<>();
            ArrayList<Vector3f> normals = new ArrayList<>();

            ArrayList<Vector3f> resultPoints3D = new ArrayList<>();

            ArrayList<Float> z_coordinates = new ArrayList<>();
            Vector3f polygonNormal = new Vector3f(0,0,0);

            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));

                int index = mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd);
                Vector3f normal = mesh.normals.get(index);

                Vector3f.normalize(normal);
                normals.add(normal);

                if(mesh.textureVertices.size() != 0) {
                    Vector2f textureVertex = mesh.textureVertices.get(mesh.polygons.get(polygonInd).getTextureVertexIndices().get(vertexInPolygonInd));
                    textureVertices.add(textureVertex);
                }

                Vector3f vertexVecmath = new Vector3f(vertex.getX(), vertex.getY(), vertex.getZ());

                resultPoints3D.add(vertexVecmath);

                Vector2f resultPoint = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath), width, height);
                resultPoints.add(resultPoint);
                z_coordinates.add(vertex.getZ());

                polygonNormal = ModelUtils.calculateNormalForPolygon(mesh.polygons.get(polygonInd), mesh);
            }

            GraphicsUtils graphicsUtils = new DrawUtilsJavaFX(canvas);

            if(drawMesh) {
                drawPolygon(graphicsContext, resultPoints, nVerticesInPolygon);
            }

            if(useLighting && !texturePolygons) {
                Vector3f position = new Vector3f(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
                Vector3f target = new Vector3f(camera.getTarget().x, camera.getTarget().y, camera.getTarget().z);
                fillPolygonWithLight1(graphicsUtils, resultPoints, color, z_buffer, position, target, z_coordinates, polygonNormal);
            }

            if(!useLighting && !texturePolygons) {
                Vector3f position = new Vector3f(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
                fillPolygonWithoutLight(graphicsUtils, resultPoints, color, z_buffer, position, z_coordinates);
            }

            if(texturePolygons && mesh.textureVertices.size() != 0) {
                /*Vector3f position = new Vector3f(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
                Vector3f target = new Vector3f(camera.getTarget().x, camera.getTarget().y, camera.getTarget().z);
                try {
                    BufferedImage texture = ImageIO.read(new File("aaaaaa.jpg"));
                    texturePolygon(graphicsUtils, resultPoints, textureVertices, texture, z_buffer, position, target, z_coordinates, polygonNormal);
                } catch (IOException e) {
                    System.out.println(e);
                }*/
                Vector3f position = new Vector3f(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
                Vector3f target = new Vector3f(camera.getTarget().x, camera.getTarget().y, camera.getTarget().z);
                //fillPolygonWithLight(graphicsUtils, resultPoints, color, z_buffer, position, target, z_coordinates, polygonNormal);
                fillPolygonWithLight(graphicsUtils, resultPoints, color, z_buffer, position, target, z_coordinates, normals, resultPoints3D);
            }
        }
    }

    private static void texturePolygon(
            final GraphicsUtils gr,
            ArrayList<Vector2f> resultPoints,
            ArrayList<Vector2f> textureVertices,
            BufferedImage texture, float[][] z_buffer,
            Vector3f position, Vector3f target,
            ArrayList<Float> z_coordinates,
            Vector3f polygonNormal) {
        Vector3f.normalize(polygonNormal);
        Vector3f lightDirection = Vector3f.fromTwoPoints(target, position);
        Vector3f.normalize(lightDirection);
        float shadowIndex = Vector3f.dotProduct(polygonNormal, lightDirection);

        ArrayList<Vector2f> points = new ArrayList<>();
        points.add(resultPoints.get(0));
        points.add(resultPoints.get(1));
        points.add(resultPoints.get(2));

        points.sort(Comparator.comparing(Vector2f::getY));

        final float x1 = points.get(0).getX();
        float x2 = points.get(1).getX();
        float x3 = points.get(2).getX();
        float y1 = points.get(0).getY();
        float y2 = points.get(1).getY();
        float y3 = points.get(2).getY();
        float z1 = z_coordinates.get(0);
        float z2 = z_coordinates.get(1);
        float z3 = z_coordinates.get(2);

        for (int y = (int) Math.round(y1 + 0.5); y <= y2; y++) {
            double startX = getX(y, x1, x2, y1, y2);
            double endX = getX(y, x1, x3, y1, y3);
            textureLine(gr, y, startX, endX, z_buffer, position.getZ(), x1, x2, x3, y1, y2, y3, z1, z2, z3, shadowIndex, texture, textureVertices);
        }

        for (int y = (int) Math.round(y2 + 0.5); y < y3; y++) {
            double startX = getX(y, x1, x3, y1, y3);
            double endX = getX(y, x2, x3, y2, y3);
            textureLine(gr, y, startX, endX, z_buffer, position.getZ(), x1, x2, x3, y1, y2, y3, z1, z2, z3, shadowIndex, texture, textureVertices);
        }
    }

    private static void textureLine(
            final GraphicsUtils gr, int y, double startX, double endX, float[][] z_buffer, float pos,
            float x1, float x2, float x3, float y1, float y2, float y3, float z1, float z2, float z3, float shadowIndex, BufferedImage texture, ArrayList<Vector2f> textureVertices) {

        if (Double.compare(startX, endX) > 0) {
            double temp = startX;
            startX = endX;
            endX = temp;
        }

        float colorShift = Math.abs(shadowIndex);
        for (int x = (int) Math.round(startX + 0.5); x < endX; x++) {
            if(x >= 0 && y >= 0) {
                float[] pixelCoordinates = findPixel(x, y, x1, x2, x3, y1, y2, y3, textureVertices);
                int color = texture.getRGB((int)(pixelCoordinates[0]*texture.getWidth()), (int)(pixelCoordinates[1]*texture.getHeight()));

                System.out.print((int)(pixelCoordinates[0]*texture.getWidth()) + " ");
                System.out.print((int)(pixelCoordinates[1]*texture.getHeight()) + " ");
                System.out.print(color + "; ");


                color = Math.abs(color);
                float r = (float)((color & 0x00ff0000) >> 16) / 255;
                System.out.print(r + " ");
                float g = (float)((color & 0x0000ff00) >> 8) / 255;
                System.out.print(g + " ");
                float b = (float)(color & 0x000000ff) / 255;
                System.out.print(b + "; ");
                Color col = new Color(r, g, b, 1);
                double red = col.getRed() * colorShift;
                System.out.print(red + " ");
                double green = col.getGreen() * colorShift;
                System.out.print(green + " ");
                double blue = col.getBlue() * colorShift;
                System.out.println(blue + " ");
                if (Math.abs(pos - findDepth(x, y, x1, x2, x3, y1, y2, y3, z1, z2, z3)) < z_buffer[Math.abs(y)][Math.abs(x)]) {
                    gr.setPixel(x, y, new Color(red, green, blue, 1));
                    z_buffer[Math.abs(y)][Math.abs(x)] = Math.abs(pos - findDepth(x, y, x1, x2, x3, y1, y2, y3, z1, z2, z3));
                    System.out.println("aaaa");
                }
            }
        }
    }

    private static float[] findPixel(float x, float y, float x1, float x2, float x3, float y1, float y2, float y3, ArrayList<Vector2f> textureVertices) {
        float detT = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);
        float alpha = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / detT;
        float beta = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / detT;
        float gamma = 1 - alpha - beta;
        float resultX = alpha * textureVertices.get(0).x + beta * textureVertices.get(1).x + gamma * textureVertices.get(2).x;
        float resultY = alpha * textureVertices.get(0).y + beta * textureVertices.get(1).y + gamma * textureVertices.get(2).y;
        return new float[]{resultX, resultY};
    }

    private static void fillPolygonWithoutLight(
            final GraphicsUtils gr,
            ArrayList<Vector2f> resultPoints,
            Color color, float[][] z_buffer,
            Vector3f position,
            ArrayList<Float> z_coordinates) {
        fillPolygon(gr, resultPoints, color, z_buffer, position, z_coordinates, 1);
    }

    private static void fillPolygonWithLight1(
            final GraphicsUtils gr,
            ArrayList<Vector2f> resultPoints,
            Color color, float[][] z_buffer,
            Vector3f position, Vector3f target,
            ArrayList<Float> z_coordinates,
            Vector3f polygonNormal) {
        Vector3f.normalize(polygonNormal);
        Vector3f lightDirection = Vector3f.fromTwoPoints(target, position);
        Vector3f.normalize(lightDirection);
        float shadowIndex = Vector3f.dotProduct(polygonNormal, lightDirection);
        fillPolygon(gr, resultPoints, color, z_buffer, position, z_coordinates, shadowIndex);
    }


    private static void drawPolygon(final GraphicsContext graphicsContext, ArrayList<Vector2f> resultPoints, final int nVerticesInPolygon) {
        for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
            graphicsContext.strokeLine(
                    resultPoints.get(vertexInPolygonInd - 1).getX(),
                    resultPoints.get(vertexInPolygonInd - 1).getY(),
                    resultPoints.get(vertexInPolygonInd).getX(),
                    resultPoints.get(vertexInPolygonInd).getY());
        }

        if (nVerticesInPolygon > 0) {
            graphicsContext.strokeLine(
                    resultPoints.get(nVerticesInPolygon - 1).getX(),
                    resultPoints.get(nVerticesInPolygon - 1).getY(),
                    resultPoints.get(0).getX(),
                    resultPoints.get(0).getY());
        }
    }

    private static float findDepth(float x, float y, float x1, float x2, float x3, float y1, float y2, float y3, float z1, float z2, float z3) {
        float detT = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);
        float alpha = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / detT;
        float beta = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / detT;
        float gamma = 1 - alpha - beta;
        return alpha * z1 + beta * z2 + gamma * z3;
    }

    private static void fillPolygon(
            final GraphicsUtils gr,
            ArrayList<Vector2f> resultPoints,
            Color color, float[][] z_buffer,
            Vector3f position, ArrayList<Float> z_coordinates,
            float shadowIndex) {

        ArrayList<Vector2f> points = new ArrayList<>();
        points.add(resultPoints.get(0));
        points.add(resultPoints.get(1));
        points.add(resultPoints.get(2));

        points.sort(Comparator.comparing(Vector2f::getY));

        final float x1 = points.get(0).getX();
        float x2 = points.get(1).getX();
        float x3 = points.get(2).getX();
        float y1 = points.get(0).getY();
        float y2 = points.get(1).getY();
        float y3 = points.get(2).getY();
        float z1 = z_coordinates.get(0);
        float z2 = z_coordinates.get(1);
        float z3 = z_coordinates.get(2);

        for (int y = (int) Math.round(y1 + 0.5); y <= y2; y++) {
            double startX = getX(y, x1, x2, y1, y2);
            double endX = getX(y, x1, x3, y1, y3);
            fillLine(gr, y, startX, endX, color, z_buffer, position, x1, x2, x3, y1, y2, y3, z1, z2, z3, shadowIndex);
        }

        for (int y = (int) Math.round(y2 + 0.5); y < y3; y++) {
            double startX = getX(y, x1, x3, y1, y3);
            double endX = getX(y, x2, x3, y2, y3);
            fillLine(gr, y, startX, endX, color, z_buffer, position, x1, x2, x3, y1, y2, y3, z1, z2, z3, shadowIndex);
        }
    }

    private static double getX(double y, double x1, double x2, double y1, double y2) {
        return (x2 - x1) * (y - y1) / (y2 - y1) + x1;
    }

    private static void fillLine(
            final GraphicsUtils gr, int y, double startX, double endX, Color color, float[][] z_buffer, Vector3f position,
            float x1, float x2, float x3, float y1, float y2, float y3, float z1, float z2, float z3, float shadowIndex) {

        if (Double.compare(startX, endX) > 0) {
            double temp = startX;
            startX = endX;
            endX = temp;
        }

        float colorShift = Math.abs(shadowIndex);
        double red = color.getRed() * colorShift;
        double green = color.getGreen() * colorShift;
        double blue = color.getBlue() * colorShift;
        for (int x = (int) Math.round(startX + 0.5); x < endX; x++) {
            if(x >= 0 && y >= 0) {
                Vector3f currentPoint = new Vector3f(x, y, findDepth(x, y, x1, x2, x3, y1, y2, y3, z1, z2, z3));
                Vector3f vec = new Vector3f(position, currentPoint);
                float length = Vector3f.length(vec);
                if(length < z_buffer[y][x]) {
                    gr.setPixel(x, y, new Color(red, green, blue, 1));
                    z_buffer[y][x] = length;
                }
            }
        }
    }

    private static void fillPolygonWithLight(
            final GraphicsUtils gr,
            ArrayList<Vector2f> resultPoints,
            Color color, float[][] z_buffer,
            Vector3f position, Vector3f target,
            ArrayList<Float> z_coordinates,
            ArrayList<Vector3f> normals,
            ArrayList<Vector3f> resultPoints3D) {

        Vector3f lightDirection = Vector3f.fromTwoPoints(target, position);
        Vector3f.normalize(lightDirection);

        ArrayList<Vector2f> points = new ArrayList<>();
        points.add(resultPoints.get(0));
        points.add(resultPoints.get(1));
        points.add(resultPoints.get(2));

        points.sort(Comparator.comparing(Vector2f::getY));

        final float x1 = points.get(0).getX();
        float x2 = points.get(1).getX();
        float x3 = points.get(2).getX();
        float y1 = points.get(0).getY();
        float y2 = points.get(1).getY();
        float y3 = points.get(2).getY();
        float z1 = z_coordinates.get(0);
        float z2 = z_coordinates.get(1);
        float z3 = z_coordinates.get(2);

        for (int y = (int) Math.round(y1 + 0.5); y <= y2; y++) {
            double startX = getX(y, x1, x2, y1, y2);
            double endX = getX(y, x1, x3, y1, y3);
            fillLineWithLight(gr, y, startX, endX, color, z_buffer, position, x1, x2, x3, y1, y2, y3, z1, z2, z3, normals, resultPoints3D, lightDirection);
        }

        for (int y = (int) Math.round(y2 + 0.5); y < y3; y++) {
            double startX = getX(y, x1, x3, y1, y3);
            double endX = getX(y, x2, x3, y2, y3);
            fillLineWithLight(gr, y, startX, endX, color, z_buffer, position, x1, x2, x3, y1, y2, y3, z1, z2, z3, normals, resultPoints3D, lightDirection);
        }
    }

    private static void fillLineWithLight(
            final GraphicsUtils gr, int y, double startX, double endX, Color color, float[][] z_buffer, Vector3f position,
            float x1, float x2, float x3, float y1, float y2, float y3, float z1, float z2, float z3, ArrayList<Vector3f> normals, ArrayList<Vector3f> resultPoints3D, Vector3f lightDirection) {

        if (Double.compare(startX, endX) > 0) {
            double temp = startX;
            startX = endX;
            endX = temp;
        }

        for (int x = (int) Math.round(startX + 0.5); x < endX; x++) {
            Vector3f normal = findNormal(x, y, x1, x2, x3, y1, y2, y3, z1, z2, z3, normals, resultPoints3D);
            Vector3f.normalize(normal);
            /*Vector3f currentPoint = new Vector3f(x, y, findDepth(x, y, x1, x2, x3, y1, y2, y3, z1, z2, z3));
            Vector3f light = new Vector3f(currentPoint, position);
            Vector3f vec = light;
            Vector3f.normalize(light);*/
            float shadowIndex = Vector3f.dotProduct(normal, lightDirection);
            //float colorShift = Math.abs(shadowIndex);
            float colorShift;
            if(shadowIndex < 0) {
                colorShift = 0.05f;
            } else {
                colorShift = shadowIndex;
            }
            double red = color.getRed() * colorShift;
            double green = color.getGreen() * colorShift;
            double blue = color.getBlue() * colorShift;
            if(x >= 0 && y >= 0) {
                Vector3f currentPoint = new Vector3f(x, y, findDepth(x, y, x1, x2, x3, y1, y2, y3, z1, z2, z3));
                Vector3f vec = new Vector3f(position, currentPoint);
                float length = Vector3f.length(vec);
                if(length < z_buffer[y][x]) {
                    gr.setPixel(x, y, new Color(red, green, blue, 1));
                    z_buffer[y][x] = length;
                }
            }
        }
    }

    private static Vector3f findNormal(float x, float y, float x11, float x22, float x33, float y11, float y22, float y33, float z11, float z22, float z33, ArrayList<Vector3f> normals, ArrayList<Vector3f> resultPoints3D) {
        /*normals.sort(Comparator.comparing(Vector3f::getY));
        normals.sort(Comparator.comparing(Vector3f::getX));*/

        //TreeMap<Float, Vector3f> sortedNormals = new TreeMap<>();

        normals.sort(Comparator.comparing(Vector3f::getY).thenComparing(Vector3f::getX).thenComparing(Vector3f::getZ));
        //HashMap<Float, Vector3f> pointToNormal = new HashMap<>();
        Vector3f vert1 = new Vector3f(x11, y11, z11);
        Vector3f vert2 = new Vector3f(x22, y22, z22);
        Vector3f vert3 = new Vector3f(x33, y33, z33);
        ArrayList<Vector3f> vertexes = new ArrayList<>();
        vertexes.add(vert1);
        vertexes.add(vert2);
        vertexes.add(vert3);
        vertexes.sort(Comparator.comparing(Vector3f::getY).thenComparing(Vector3f::getX).thenComparing(Vector3f::getZ));
        //resultPoints3D.sort(Comparator.comparing(Vector3f::getZ));
        float x1 = vertexes.get(0).getX();
        float y1 = vertexes.get(0).getY();
        float z1 = vertexes.get(0).getZ();
        float x2 = vertexes.get(1).getX();
        float y2 = vertexes.get(1).getY();
        float z2 = vertexes.get(1).getZ();
        float x3 = vertexes.get(2).getX();
        float y3 = vertexes.get(2).getY();
        float z3 = vertexes.get(2).getZ();
        float detT = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);
        float gamma = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / detT;
        float beta = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / detT;
        float alpha = 1 - beta - gamma;
        /*float[] xs = {x1, x2, x3};
        float[] ys = {y1, y2, y3};
        Arrays.sort(xs);
        Arrays.sort(ys);
        float detT = (ys[1] - ys[2]) * (xs[0] - xs[2]) + (xs[2] - xs[1]) * (ys[0] - ys[2]);
        float gamma = ((ys[1] - ys[2]) * (x - xs[2]) + (xs[2] - xs[1]) * (y - ys[2])) / detT;
        float beta = ((ys[2] - ys[0]) * (x - xs[2]) + (xs[0] - xs[2]) * (y - ys[2])) / detT;
        float alpha = 1 - gamma - beta;*/
        /*float x1 = resultPoints3D.get(0).getX();
        float y1 = resultPoints3D.get(0).getY();
        float z1 = resultPoints3D.get(0).getZ();
        float x2 = resultPoints3D.get(1).getX();
        float y2 = resultPoints3D.get(1).getY();
        float z2 = resultPoints3D.get(1).getZ();
        float x3 = resultPoints3D.get(2).getX();
        float y3 = resultPoints3D.get(2).getY();
        float z3 = resultPoints3D.get(2).getZ();*/
        /*float detT = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);
        float gamma = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / detT;
        float beta = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / detT;
        float alpha = 1 - gamma - beta;*/
        float resultX = alpha * normals.get(0).getX() + beta * normals.get(1).getX() + gamma * normals.get(2).getX();
        float resultY = alpha * normals.get(0).getY() + beta * normals.get(1).getY() + gamma * normals.get(2).getY();
        float resultZ = alpha * normals.get(0).getZ() + beta * normals.get(1).getZ() + gamma * normals.get(2).getZ();
        return new Vector3f(resultX, resultY, resultZ);
    }

    /*private static void fillPolygonWithLight(
            final GraphicsUtils gr,
            ArrayList<Vector2f> resultPoints,
            Color color, float[][] z_buffer,
            Vector3f position, Vector3f target,
            ArrayList<Float> z_coordinates,
            ArrayList<Vector3f> normals) {

        Vector3f lightDirection = Vector3f.fromTwoPoints(target, position);
        Vector3f.normalize(lightDirection);

        ArrayList<Vector2f> points = new ArrayList<>();
        points.add(resultPoints.get(0));
        points.add(resultPoints.get(1));
        points.add(resultPoints.get(2));

        points.sort(Comparator.comparing(Vector2f::getY));

        final float x1 = points.get(0).getX();
        float x2 = points.get(1).getX();
        float x3 = points.get(2).getX();
        float y1 = points.get(0).getY();
        float y2 = points.get(1).getY();
        float y3 = points.get(2).getY();
        float z1 = z_coordinates.get(0);
        float z2 = z_coordinates.get(1);
        float z3 = z_coordinates.get(2);

        for (int y = (int) Math.round(y1 + 0.5); y <= y2; y++) {
            double startX = getX(y, x1, x2, y1, y2);
            double endX = getX(y, x1, x3, y1, y3);
            fillLineWithLight(gr, y, startX, endX, color, z_buffer, position, x1, x2, x3, y1, y2, y3, z1, z2, z3, normals, lightDirection);
        }

        for (int y = (int) Math.round(y2 + 0.5); y < y3; y++) {
            double startX = getX(y, x1, x3, y1, y3);
            double endX = getX(y, x2, x3, y2, y3);
            fillLineWithLight(gr, y, startX, endX, color, z_buffer, position, x1, x2, x3, y1, y2, y3, z1, z2, z3, normals, lightDirection);
        }
    }

    private static void fillLineWithLight(
            final GraphicsUtils gr, int y, double startX, double endX, Color color, float[][] z_buffer, Vector3f position,
            float x1, float x2, float x3, float y1, float y2, float y3, float z1, float z2, float z3, ArrayList<Vector3f> normals, Vector3f lightDirection) {

        if (Double.compare(startX, endX) > 0) {
            double temp = startX;
            startX = endX;
            endX = temp;
        }

        *//*float colorShift = Math.abs(shadowIndex);
        double red = color.getRed() * colorShift;
        double green = color.getGreen() * colorShift;
        double blue = color.getBlue() * colorShift;*//*
        for (int x = (int) Math.round(startX + 0.5); x < endX; x++) {
            Vector3f normal = findNormal(x, y, x1, x2, x3, y1, y2, y3, normals);
            Vector3f.normalize(normal);
            float shadowIndex = Vector3f.dotProduct(normal, lightDirection);
            float colorShift = Math.abs(shadowIndex);
            double red = color.getRed() * colorShift;
            double green = color.getGreen() * colorShift;
            double blue = color.getBlue() * colorShift;
            if(x >= 0 && y >= 0) {
                Vector3f currentPoint = new Vector3f(x, y, findDepth(x, y, x1, x2, x3, y1, y2, y3, z1, z2, z3));
                Vector3f vec = new Vector3f(position, currentPoint);
                float length = Vector3f.length(vec);
                *//*if (Math.abs(pos - findDepth(x, y, x1, x2, x3, y1, y2, y3, z1, z2, z3)) < z_buffer[Math.abs(y)][Math.abs(x)]) {
                    gr.setPixel(x, y, new Color(red, green, blue, 1));
                    z_buffer[Math.abs(y)][Math.abs(x)] = Math.abs(pos - findDepth(x, y, x1, x2, x3, y1, y2, y3, z1, z2, z3));
                }*//*
                if(length < z_buffer[y][x]) {
                    gr.setPixel(x, y, new Color(red, green, blue, 1));
                    z_buffer[y][x] = length;
                }
            }
        }
    }

    private static Vector3f findNormal(float x, float y, float x1, float x2, float x3, float y1, float y2, float y3, ArrayList<Vector3f> normals) {
        float detT = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);
        float alpha = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / detT;
        float beta = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / detT;
        float gamma = 1 - alpha - beta;
        float resultX = alpha * normals.get(0).getX() + beta * normals.get(1).getX() + gamma * normals.get(2).getX();
        float resultY = alpha * normals.get(0).getY() + beta * normals.get(1).getY() + gamma * normals.get(2).getY();
        float resultZ = alpha * normals.get(0).getZ() + beta * normals.get(1).getZ() + gamma * normals.get(2).getZ();
        return new Vector3f(resultX, resultY, resultZ);
    }*/
}