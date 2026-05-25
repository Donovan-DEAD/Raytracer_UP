package com.github.donovan_dead.Physics.areaLights;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Physics.BaseLightSource;

public abstract class AreaLight extends BaseLightSource {
    public static final int Samples = 20;
    protected Vector3 currentSample;
    public AreaLight(Vector3 origin, RGBColor lightColor, double intensity) {
        super(origin, lightColor, intensity);
    }

    public abstract Vector3 getSample();
}
