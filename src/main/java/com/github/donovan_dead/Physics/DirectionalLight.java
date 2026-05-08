package com.github.donovan_dead.Physics;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Math.Utils;

public class DirectionalLight extends BaseLightSource {
    private final Vector3 direction;
    private final RGBColor lightColor;
    private final double intensity;

    /**
     * Creates a directional light with a given direction and color.
     *
     * @param direction the direction from which light comes (will be normalized)
     * @param lightColor the color of the light
     * @param intensity the intensity of the light
     */
    public DirectionalLight(Vector3 direction, RGBColor lightColor, double intensity) {
        this.direction = direction.normalize();
        this.lightColor = lightColor;
        this.intensity = intensity;
    }

    public Vector3 direction() {
        return direction;
    }

    public RGBColor lightColor() {
        return lightColor;
    }

    public double intensity() {
        return intensity;
    }

    @Override
    public Vector3 getLightContribution(Vector3 position, Vector3 normal, Vector3 baseColor) {
        Vector3 vecToLight = direction;
        double resultDot = Math.max(0, Utils.dotProduct(vecToLight, normal.normalize()));

        Vector3 scaledColor = Vector3.builder()
            .X(lightColor.R() * intensity)
            .Y(lightColor.G() * intensity)
            .Z(lightColor.B() * intensity)
            .build();

        return Vector3.builder()
            .X(baseColor.X() * scaledColor.X() * resultDot)
            .Y(baseColor.Y() * scaledColor.Y() * resultDot)
            .Z(baseColor.Z() * scaledColor.Z() * resultDot)
            .build();
    }
}
