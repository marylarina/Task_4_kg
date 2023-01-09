package com.cgvsu;

import com.cgvsu.render_engine.Camera;

public class CameraTable {
    public Camera camera;
    public int name;

    public CameraTable(Camera camera, int name){
        this.camera = camera;
        this.name = name;
    }

    public Camera getCamera() {
        return camera;
    }
    public int getName(){
        return name;
    }
}
