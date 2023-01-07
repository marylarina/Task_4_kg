package com.cgvsu;

import com.cgvsu.model.Model;

import java.io.File;

public class ModelTable {
    public Model model;
    public String string;

    public ModelTable(Model model, String string){
        this.model = model;
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public Model getModel() {
        return model;
    }
}
