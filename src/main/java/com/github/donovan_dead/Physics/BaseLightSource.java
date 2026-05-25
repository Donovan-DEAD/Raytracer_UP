package com.github.donovan_dead.Physics;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.UV;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Structures.Material;

public abstract class BaseLightSource {
    protected Vector3 origin;
    protected final RGBColor lightColor;
    protected double intensity;

    protected BaseLightSource(Vector3 origin, RGBColor lightColor, double intensity) {
        this.origin = origin;
        this.lightColor = lightColor;
        this.intensity = intensity;
    }

    public Vector3 origin() {
        return origin;
    }

    public RGBColor lightColor() {
        return lightColor;
    }

    public double intensity() {
        return intensity;
    }

    public abstract Vector3 getLightContribution(Vector3 position, Vector3 normal, Material material, Vector3 rayOrig, double mediumIor, UV uv);
}
