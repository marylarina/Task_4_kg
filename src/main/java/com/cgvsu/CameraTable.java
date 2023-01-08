package com.cgvsu;

import com.cgvsu.render_engine.Camera;

public class CameraTable {
    public Camera camera;
    public String name;

    public CameraTable(Camera camera, String name){
        this.camera = camera;
        this.name = name;
    }

    public Camera getCamera() {
        return camera;
    }
    public String getName(){
        return name;
    }
}
