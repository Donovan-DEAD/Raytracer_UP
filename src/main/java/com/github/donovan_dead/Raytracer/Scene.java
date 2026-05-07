package com.github.donovan_dead.Raytracer;

import java.util.ArrayList;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Objects.Object3D;
import com.github.donovan_dead.Physics.BaseLightSource;

public class Scene {
    protected ArrayList < Object3D > objects = new ArrayList<>();
    protected ArrayList < BaseLightSource > lights = new ArrayList<>();

    public RGBColor background = new RGBColor(0,0,0);

    public void addObject(Object3D o){
        objects.add(o);
    }

    public void addLightSource(BaseLightSource l){
        lights.add(l);
    }

    public ArrayList< Object3D > getObjects(){
        return this.objects;
    }

    public ArrayList<BaseLightSource> getLights(){
        return this.lights;
    }

}
