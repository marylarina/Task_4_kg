package com.cgvsu.render_engine.triangle_rasterization;

import javafx.scene.paint.Color;

public abstract class GraphicsUtils<T> {
    T graphics;

    public GraphicsUtils(T graphics) {
        this.graphics = graphics;
    }

    public abstract void setPixel(int x, int y, Color myColor);

}
