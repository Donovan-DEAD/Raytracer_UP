package com.github.donovan_dead.Physics;

import java.awt.Color;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.UV;
import com.github.donovan_dead.Math.Utils;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Structures.Material;

public class SpotLight extends BaseLightSource {
    private final Vector3 origin;
    private final Vector3 direction;
    private final RGBColor lightColor;
    private final double intensity;
    private final double cosInner;
    private final double cosOuter;

    /**
     * @param innerAngle radians — full intensity inside this cone
     * @param outerAngle radians — zero intensity outside this cone, smooth falloff between
     */
    public SpotLight(Vector3 origin, Vector3 direction, RGBColor lightColor,
                     double intensity, double innerAngle, double outerAngle) {
        this.origin    = origin;
        this.direction = direction.normalize();
        this.lightColor = lightColor;
        this.intensity  = intensity;
        this.cosInner   = Math.cos(innerAngle);
        this.cosOuter   = Math.cos(outerAngle);
    }

    public Vector3 origin()    { return origin; }
    public Vector3 direction() { return direction; }
    public RGBColor lightColor() { return lightColor; }
    public double intensity()  { return intensity; }

    @Override
    public Vector3 getLightContribution(Vector3 position, Vector3 normal, Material material, Vector3 origin, UV uv) {
        Vector3 vecToLightRaw  = this.origin.subtract(position);
        Vector3 vecToLight     = vecToLightRaw.normalize();

        double cosTheta = Utils.dotProduct(direction, vecToLight.scale(-1));
        if (cosTheta < cosOuter) return Vector3.Zero();

        double spotFactor = (cosTheta >= cosInner) ? 1.0
            : Math.pow((cosTheta - cosOuter) / (cosInner - cosOuter), 2);

        double NdotL = Math.max(0, Utils.dotProduct(vecToLight, normal.normalize()));
        double atenuation = intensity / Math.pow(vecToLightRaw.getMagnitude(), 2);

        Vector3 albedo;
        if (material.getDiffuseTexture() != null) {
            Color c = new Color(material.getDiffuseTexture().getPixel(uv));
            albedo = new Vector3(
                material.getKd().X() * c.getRed()   / 255.0,
                material.getKd().Y() * c.getGreen() / 255.0,
                material.getKd().Z() * c.getBlue()  / 255.0
            );
        } else {
            albedo = material.getKd();
        }

        Vector3 diff = new Vector3(
            albedo.X() * lightColor.R() * atenuation * NdotL,
            albedo.Y() * lightColor.G() * atenuation * NdotL,
            albedo.Z() * lightColor.B() * atenuation * NdotL
        );

        Vector3 viewDir = origin.subtract(position).normalize();
        Vector3 halfVec = vecToLight.add(viewDir).normalize();

        double roughness2;
        if (material.getRoughnessTexture() != null) {
            Color c = new Color(material.getRoughnessTexture().getPixel(uv));
            roughness2 = Math.pow(material.getRoughness() * c.getRed() / 255.0, 2);
        } else {
            roughness2 = material.getRoughness() * material.getRoughness();
        }

        double NdotH = Utils.dotProduct(normal, halfVec);
        double D = roughness2 / (Math.PI * Math.pow(NdotH * NdotH * (roughness2 - 1) + 1, 2));

        double IoRCoeff2 = Math.pow((1.0 - material.getNi()) / (1.0 + material.getNi()), 2);
        double metallic = material.getMetallicTexture() != null
            ? material.getMetallic() * (new Color(material.getMetallicTexture().getPixel(uv)).getRed() / 255.0)
            : material.getMetallic();

        Vector3 F0 = new Vector3(
            IoRCoeff2 * (1 - metallic) + albedo.X() * metallic,
            IoRCoeff2 * (1 - metallic) + albedo.Y() * metallic,
            IoRCoeff2 * (1 - metallic) + albedo.Z() * metallic
        );

        double HdotV = Math.max(0, Utils.dotProduct(halfVec, viewDir));
        double oneMinusCos5 = Math.pow(1.0 - HdotV, 5);
        Vector3 F = new Vector3(
            F0.X() + (1 - F0.X()) * oneMinusCos5,
            F0.Y() + (1 - F0.Y()) * oneMinusCos5,
            F0.Z() + (1 - F0.Z()) * oneMinusCos5
        );

        double safeHdotV = Math.max(HdotV, 1e-6);
        double G = Math.min(1, Math.min(
            (2 * NdotH * Utils.dotProduct(normal, viewDir)) / safeHdotV,
            (2 * NdotH * Utils.dotProduct(normal, vecToLight)) / safeHdotV
        ));

        double NdotV = Math.max(0, Utils.dotProduct(normal, viewDir));
        double denominator = 4.0 * NdotL * NdotV + 1e-6;
        double DG_denom = D * G / denominator;

        Vector3 diffContrib = new Vector3(
            (1 - F.X()) * (1 - metallic) * diff.X() / Math.PI,
            (1 - F.Y()) * (1 - metallic) * diff.Y() / Math.PI,
            (1 - F.Z()) * (1 - metallic) * diff.Z() / Math.PI
        );

        Vector3 spec = new Vector3(
            F.X() * DG_denom * lightColor.R() * atenuation * NdotL,
            F.Y() * DG_denom * lightColor.G() * atenuation * NdotL,
            F.Z() * DG_denom * lightColor.B() * atenuation * NdotL
        );

        return diffContrib.add(spec).scale(spotFactor);
    }
}
