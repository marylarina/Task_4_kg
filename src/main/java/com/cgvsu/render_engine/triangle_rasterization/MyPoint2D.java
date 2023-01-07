package com.cgvsu.render_engine.triangle_rasterization;

import java.util.Objects;

public class MyPoint2D {

    private float x;
    private float y;

    public MyPoint2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyPoint2D myPoint2D = (MyPoint2D) o;
        return Float.compare(myPoint2D.x, x) == 0 && Float.compare(myPoint2D.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }


    @Override
    public String toString() {
        return "MyPoint2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

