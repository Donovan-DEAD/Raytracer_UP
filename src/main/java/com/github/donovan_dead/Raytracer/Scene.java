package com.github.donovan_dead.Raytracer;

import java.util.ArrayList;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Objects.Object3D;
import com.github.donovan_dead.Physics.LightSource;

public class Scene {
    protected ArrayList < Object3D > objects = new ArrayList<>();
    protected ArrayList < LightSource > lights = new ArrayList<>(); // This is going to be used later;

    public RGBColor background = new RGBColor(0,0,0);

    public void addObject(Object3D o){
        objects.add(o);
    }

    public void addLightSource(LightSource l){
        lights.add(l);
    }

    public ArrayList< Object3D > getObjects(){
        return this.objects;
    }

    public ArrayList <LightSource> getLights(){
        return this.lights;
    }

}
