package com.cgvsu.render_engine;
import com.cgvsu.render_engine.triangle_rasterization.MyPoint2D;

//import javax.vecmath.*;

import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Matrix4f;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate() {
        float[] matrix = new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1};
        return new Matrix4f(matrix);
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultX = new Vector3f();
        Vector3f resultY = new Vector3f();
        Vector3f resultZ = new Vector3f();

        resultZ = Vector3f.sub(target, eye);
        resultX = Vector3f.calculateCrossProduct(up, resultZ);
        resultY = Vector3f.calculateCrossProduct(resultZ, resultX);

        Vector3f.normalize(resultX);
        Vector3f.normalize(resultY);
        Vector3f.normalize(resultZ);

        float[] matrix = new float[]{
                resultX.getX(), resultY.getX(), resultZ.getX(), 0,
                resultX.getY(), resultY.getY(), resultZ.getY(), 0,
                resultX.getZ(), resultY.getZ(), resultZ.getZ(), 0,
                -Vector3f.dotProduct(resultX, eye), -Vector3f.dotProduct(resultY, eye), -Vector3f.dotProduct(resultZ, eye), 1};

        /*float[] matrix = new float[]{
                resultX.getX(), resultY.getX(), resultZ.getX(), 0,
                resultX.getY(), resultY.getY(), resultZ.getY(), 0,
                resultX.getZ(), resultY.getZ(), resultZ.getZ(), 0,
                -resultX.dot(eye), -resultY.dot(eye), -resultZ.dot(eye), 1};*/
        return new Matrix4f(matrix);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        Matrix4f result = new Matrix4f();
        float tangentMinusOnDegree = (float) (1.0F / (Math.tan(fov * 0.5F)));
        result.m00 = tangentMinusOnDegree / aspectRatio;
        result.m11 = tangentMinusOnDegree;
        result.m22 = (farPlane + nearPlane) / (farPlane - nearPlane);
        result.m23 = 1.0F;
        result.m32 = 2 * (nearPlane * farPlane) / (nearPlane - farPlane);
        return result;
    }

    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {
        final float x = (vertex.getX() * matrix.m00) + (vertex.getY() * matrix.m10) + (vertex.getZ() * matrix.m20) + matrix.m30;
        final float y = (vertex.getX() * matrix.m01) + (vertex.getY() * matrix.m11) + (vertex.getZ() * matrix.m21) + matrix.m31;
        final float z = (vertex.getX() * matrix.m02) + (vertex.getY() * matrix.m12) + (vertex.getZ() * matrix.m22) + matrix.m32;
        final float w = (vertex.getX() * matrix.m03) + (vertex.getY() * matrix.m13) + (vertex.getZ() * matrix.m23) + matrix.m33;
        return new Vector3f(x / w, y / w, z / w);
    }

    public static Vector2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Vector2f(vertex.getX() * width + width / 2.0F, -vertex.getY() * height + height / 2.0F);
    }
}
