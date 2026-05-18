package com.github.donovan_dead.Physics;

import java.util.Vector;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Structures.Material;
import com.github.donovan_dead.Math.Utils;

public class LightSource extends BaseLightSource {
    private final Vector3 origin;
    private final RGBColor lightColor;
    private final double intensity;

    public LightSource(Vector3 origin, RGBColor lightColor, double intensity) {
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

    @Override
    public Vector3 getLightContribution(Vector3 position, Vector3 normal, Material material, Vector3 origin) {
        Vector3 vecToLight = this.origin.subtract(position).normalize();
        double resultDot = Math.max(0, Utils.dotProduct(vecToLight, normal.normalize()));

        double atenuation = intensity / Math.pow(vecToLight.getMagnitude(), 2);
        if (atenuation < 10e-6) atenuation = 0;

        Vector3 diff = Vector3.builder()
            .X(material.getKd().X() * lightColor.R() * atenuation * resultDot)
            .Y(material.getKd().Y() * lightColor.G() * atenuation * resultDot)
            .Z(material.getKd().Z() * lightColor.B() * atenuation * resultDot)
        .build();

        Vector3 reflection = normal.scale(2 * resultDot).subtract(vecToLight.scale(-1)).normalize();
        Vector3 viewDir = origin.subtract(position).normalize();
        double specFactor = Math.pow(
            Math.max(0, Utils.dotProduct(reflection, viewDir)),
            material.getNs()
        );

        Vector3 spec = Vector3.builder()
            .X(material.getKs().X() * lightColor.R() * atenuation * specFactor)
            .Y(material.getKs().Y() * lightColor.G() * atenuation * specFactor)
            .Z(material.getKs().Z() * lightColor.B() * atenuation * specFactor)
        .build();

        return diff.add(spec);
    }
}