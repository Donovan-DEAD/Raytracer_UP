package com.github.donovan_dead.Physics;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.UV;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Structures.Material;
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
    public Vector3 getLightContribution(Vector3 position, Vector3 normal, Material material, Vector3 origin, UV uv){
        Vector3 vecToLight = direction;
        double resultDot = Math.max(0, Utils.dotProduct(vecToLight, normal.normalize()));

        Vector3 diff = Vector3.builder()
            .X(material.getKd().X() * lightColor.R() * intensity * resultDot)
            .Y(material.getKd().Y() * lightColor.G() * intensity * resultDot)
            .Z(material.getKd().Z() * lightColor.B() * intensity * resultDot)
            .build();

        Vector3 reflection = normal.scale(2 * resultDot).subtract(vecToLight.scale(-1)).normalize();
        Vector3 viewDir = origin.subtract(position).normalize();
        double specFactor = Math.pow(
            Math.max(0, Utils.dotProduct(reflection, viewDir)),
            material.getNs()
        );

        Vector3 spec = Vector3.builder()
            .X(material.getKs().X() * lightColor.R() * intensity * specFactor)
            .Y(material.getKs().Y() * lightColor.G() * intensity * specFactor)
            .Z(material.getKs().Z() * lightColor.B() * intensity * specFactor)
            .build();

        return diff.add(spec);
    }
}
