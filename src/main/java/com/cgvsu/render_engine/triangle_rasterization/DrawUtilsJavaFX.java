package com.cgvsu.render_engine.triangle_rasterization;


import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class DrawUtilsJavaFX extends GraphicsUtils<Canvas> {

    public DrawUtilsJavaFX(Canvas graphics) {
        super(graphics);
    }

    @Override
    public void setPixel(int x, int y, Color myColor) {
        graphics.getGraphicsContext2D().getPixelWriter().setColor(x, y, myColor);
    }

}
