package com.github.donovan_dead.Physics;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Vector3;
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
    public Vector3 getLightContribution(Vector3 position, Vector3 normal, Vector3 baseColor) {
        Vector3 vecToLight = origin.subtract(position).normalize();
        double resultDot = Math.max(0, Utils.dotProduct(vecToLight, normal.normalize()));

        double atenuation = intensity / Math.pow(vecToLight.getMagnitude(), 2);
        if (atenuation < 10e-6) atenuation = 0;
        
        Vector3 scaledColor = Vector3.builder()
            .X(lightColor.R() * atenuation)
            .Y(lightColor.G() * atenuation)
            .Z(lightColor.B() * atenuation)
            .build();

        return Vector3.builder()
            .X(baseColor.X() * scaledColor.X() * resultDot)
            .Y(baseColor.Y() * scaledColor.Y() * resultDot)
            .Z(baseColor.Z() * scaledColor.Z() * resultDot)
            .build();
    }
}