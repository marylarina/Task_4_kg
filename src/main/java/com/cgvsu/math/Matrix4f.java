package com.cgvsu.math;

import java.io.Serializable;

public class Matrix4f {

    public float m00;
    public float m01;
    public float m02;
    public float m03;
    public float m10;
    public float m11;
    public float m12;
    public float m13;
    public float m20;
    public float m21;
    public float m22;
    public float m23;
    public float m30;
    public float m31;
    public float m32;
    public float m33;
    private static final double EPS = 1.0E-8D;

    public Matrix4f(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14, float var15, float var16) {
        this.m00 = var1;
        this.m01 = var2;
        this.m02 = var3;
        this.m03 = var4;
        this.m10 = var5;
        this.m11 = var6;
        this.m12 = var7;
        this.m13 = var8;
        this.m20 = var9;
        this.m21 = var10;
        this.m22 = var11;
        this.m23 = var12;
        this.m30 = var13;
        this.m31 = var14;
        this.m32 = var15;
        this.m33 = var16;
    }

    public Matrix4f(Matrix4f var1) {
        this.m00 = var1.m00;
        this.m01 = var1.m01;
        this.m02 = var1.m02;
        this.m03 = var1.m03;
        this.m10 = var1.m10;
        this.m11 = var1.m11;
        this.m12 = var1.m12;
        this.m13 = var1.m13;
        this.m20 = var1.m20;
        this.m21 = var1.m21;
        this.m22 = var1.m22;
        this.m23 = var1.m23;
        this.m30 = var1.m30;
        this.m31 = var1.m31;
        this.m32 = var1.m32;
        this.m33 = var1.m33;
    }

    public Matrix4f() {

    }

    public Matrix4f(float[] var1) {
        this.m00 = var1[0];
        this.m01 = var1[1];
        this.m02 = var1[2];
        this.m03 = var1[3];
        this.m10 = var1[4];
        this.m11 = var1[5];
        this.m12 = var1[6];
        this.m13 = var1[7];
        this.m20 = var1[8];
        this.m21 = var1[9];
        this.m22 = var1[10];
        this.m23 = var1[11];
        this.m30 = var1[12];
        this.m31 = var1[13];
        this.m32 = var1[14];
        this.m33 = var1[15];
    }

    public final void mul(Matrix4f var1) {
        float var2 = this.m00 * var1.m00 + this.m01 * var1.m10 + this.m02 * var1.m20 + this.m03 * var1.m30;
        float var3 = this.m00 * var1.m01 + this.m01 * var1.m11 + this.m02 * var1.m21 + this.m03 * var1.m31;
        float var4 = this.m00 * var1.m02 + this.m01 * var1.m12 + this.m02 * var1.m22 + this.m03 * var1.m32;
        float var5 = this.m00 * var1.m03 + this.m01 * var1.m13 + this.m02 * var1.m23 + this.m03 * var1.m33;
        float var6 = this.m10 * var1.m00 + this.m11 * var1.m10 + this.m12 * var1.m20 + this.m13 * var1.m30;
        float var7 = this.m10 * var1.m01 + this.m11 * var1.m11 + this.m12 * var1.m21 + this.m13 * var1.m31;
        float var8 = this.m10 * var1.m02 + this.m11 * var1.m12 + this.m12 * var1.m22 + this.m13 * var1.m32;
        float var9 = this.m10 * var1.m03 + this.m11 * var1.m13 + this.m12 * var1.m23 + this.m13 * var1.m33;
        float var10 = this.m20 * var1.m00 + this.m21 * var1.m10 + this.m22 * var1.m20 + this.m23 * var1.m30;
        float var11 = this.m20 * var1.m01 + this.m21 * var1.m11 + this.m22 * var1.m21 + this.m23 * var1.m31;
        float var12 = this.m20 * var1.m02 + this.m21 * var1.m12 + this.m22 * var1.m22 + this.m23 * var1.m32;
        float var13 = this.m20 * var1.m03 + this.m21 * var1.m13 + this.m22 * var1.m23 + this.m23 * var1.m33;
        float var14 = this.m30 * var1.m00 + this.m31 * var1.m10 + this.m32 * var1.m20 + this.m33 * var1.m30;
        float var15 = this.m30 * var1.m01 + this.m31 * var1.m11 + this.m32 * var1.m21 + this.m33 * var1.m31;
        float var16 = this.m30 * var1.m02 + this.m31 * var1.m12 + this.m32 * var1.m22 + this.m33 * var1.m32;
        float var17 = this.m30 * var1.m03 + this.m31 * var1.m13 + this.m32 * var1.m23 + this.m33 * var1.m33;
        this.m00 = var2;
        this.m01 = var3;
        this.m02 = var4;
        this.m03 = var5;
        this.m10 = var6;
        this.m11 = var7;
        this.m12 = var8;
        this.m13 = var9;
        this.m20 = var10;
        this.m21 = var11;
        this.m22 = var12;
        this.m23 = var13;
        this.m30 = var14;
        this.m31 = var15;
        this.m32 = var16;
        this.m33 = var17;
    }

}

