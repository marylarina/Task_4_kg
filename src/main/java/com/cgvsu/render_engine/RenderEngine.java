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
import com.cgvsu.model.ModelUtils;
import com.cgvsu.render_engine.triangle_rasterization.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import javax.vecmath.*;

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
            boolean drawMesh,
            boolean fillPolygons,
            boolean texturePolygons) {
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        float[][] z_buffer;
        z_buffer = new float[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                z_buffer[i][j] = 1000000F;
            }
        }

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            ArrayList<Vector2f> resultPoints = new ArrayList<>();
            ArrayList<Vector2f> textureVertices = new ArrayList<>();
            ArrayList<Vector3f> normals = new ArrayList<>();
            ArrayList<Float> z_coordinates = new ArrayList<>();
            Vector3f polygonNormal = new Vector3f(0,0,0);

            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));
                //int index = mesh.polygons.get(polygonInd).getNormalIndices().get(vertexInPolygonInd);
                //Vector3f normal = mesh.normals.get(index);

                //normals.add(normal);

                if(mesh.textureVertices.size() != 0) {
                    Vector2f textureVertex = mesh.textureVertices.get(mesh.polygons.get(polygonInd).getTextureVertexIndices().get(vertexInPolygonInd));
                    textureVertices.add(textureVertex);
                }

                javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f(vertex.getX(), vertex.getY(), vertex.getZ());

                Vector2f resultPoint = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath), width, height);
                resultPoints.add(resultPoint);
                z_coordinates.add(vertex.getZ());

                polygonNormal = ModelUtils.calculateNormalForPolygon(mesh.polygons.get(polygonInd), mesh);
            }

            GraphicsUtils graphicsUtils = new DrawUtilsJavaFX(canvas);

            if(drawMesh) {
                drawPolygon(graphicsContext, resultPoints, nVerticesInPolygon);
            }

            if(fillPolygons) {
                Vector3f position = new Vector3f(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
                Vector3f target = new Vector3f(camera.getTarget().x, camera.getTarget().y, camera.getTarget().z);
                fillPolygonWithLight(graphicsUtils, resultPoints, Color.RED, z_buffer, position, target, z_coordinates, polygonNormal);
            }

            if(texturePolygons && mesh.textureVertices.size() != 0) {
                Vector3f position = new Vector3f(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
                Vector3f target = new Vector3f(camera.getTarget().x, camera.getTarget().y, camera.getTarget().z);
                try {
                    BufferedImage texture = ImageIO.read(new File("aaaaa.jpg"));
                    texturePolygon(graphicsUtils, resultPoints, textureVertices, texture, z_buffer, position, target, z_coordinates, polygonNormal);
                } catch (IOException e) {
                    System.out.println(e);
                }
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

    private static void fillPolygonWithLight(
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

    private static void fillPolygon(final GraphicsUtils gr, ArrayList<Vector2f> resultPoints, Color color, float[][] z_buffer, Vector3f position, ArrayList<Float> z_coordinates, float shadowIndex) {

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
                /*if (Math.abs(pos - findDepth(x, y, x1, x2, x3, y1, y2, y3, z1, z2, z3)) < z_buffer[Math.abs(y)][Math.abs(x)]) {
                    gr.setPixel(x, y, new Color(red, green, blue, 1));
                    z_buffer[Math.abs(y)][Math.abs(x)] = Math.abs(pos - findDepth(x, y, x1, x2, x3, y1, y2, y3, z1, z2, z3));
                }*/
                if(length < z_buffer[y][x]) {
                    gr.setPixel(x, y, new Color(red, green, blue, 1));
                    z_buffer[y][x] = length;
                }
            }
        }
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