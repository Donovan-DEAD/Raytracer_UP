package com.github.donovan_dead.Physics.areaLights;

import java.awt.Color;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.UV;
import com.github.donovan_dead.Math.Utils;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Structures.Material;

/**
 * Circular area light source with uniform disc sampling.
 * Supports rotation in any axis for directional control.
 * Implements Cook-Torrance BRDF for physically-based lighting.
 */
public class CircleAreaLight extends AreaLight {
    private Vector3 up = new Vector3(0, 1, 0);
    private Vector3 side = new Vector3(1, 0, 0);
    private double radius;

    /**
     * Constructs a circular area light.
     *
     * @param origin the position of the light
     * @param radius the radius of the circular light surface
     * @param lightColor the RGB color of emitted light
     * @param intensity the intensity/power of the light
     */
    public CircleAreaLight(Vector3 origin, double radius, RGBColor lightColor, double intensity) {
        super(origin, lightColor, intensity);
        this.radius = radius;
    }

    /**
     * Gets the radius of the circular light surface.
     *
     * @return the radius
     */
    public double radius() {
        return radius;
    }

    /**
     * Generates a random sample point uniformly distributed on the circular surface.
     * Uses proper square-root sampling to ensure uniform distribution.
     *
     * @return a random point on the circle surface
     */
    @Override
    public Vector3 getSample() {
        double angle = Math.toRadians(Math.random() * 360);
        double rad = Math.sqrt(Math.random()) * this.radius;

        currentSample = up.scale(Math.sin(angle) * rad).add(side.scale(Math.cos(angle) * rad)).add(origin);
        return currentSample;
    }

    @Override
    public Vector3 getLightContribution(Vector3 position, Vector3 normal, Material material, Vector3 rayOrig, double mediumIor, UV uv) {
        Vector3 vecToLightRaw = currentSample.subtract(position);
        Vector3 vecToLight    = vecToLightRaw.normalize();
        double resultDot = Math.max(0, Utils.dotProduct(vecToLight, normal.normalize()));

        double atenuation = intensity / Math.pow(vecToLightRaw.getMagnitude(), 2);
        if (atenuation < 10e-6) atenuation = 0;

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
            albedo.X() * lightColor.R() * atenuation * resultDot,
            albedo.Y() * lightColor.G() * atenuation * resultDot,
            albedo.Z() * lightColor.B() * atenuation * resultDot
        );

        Vector3 viewDir = rayOrig.subtract(position).normalize();
        Vector3 halfVec = vecToLight.add(viewDir).normalize();

        double roughness2;
        
        if(material.getRoughnessTexture() != null){
            Color c = new Color(material.getRoughnessTexture().getPixel(uv));

            roughness2 = Math.pow( material.getRoughness() * c.getRed()  / 255.0 ,2);
        } else {
            roughness2 = material.getRoughness() * material.getRoughness();
        }

        double NdotH_d = Utils.dotProduct(normal, halfVec);
        double D = roughness2 / (Math.PI * Math.pow(NdotH_d * NdotH_d * (roughness2 - 1) + 1, 2) + 1e-7);

        double IoRCoeff2 = Math.pow((mediumIor - material.getNi()) / (material.getNi() + mediumIor), 2);
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
        double NdotH_g = Math.max(0, Utils.dotProduct(normal, halfVec));
        double NdotV_g = Math.max(0, Utils.dotProduct(normal, viewDir));
        double NdotL_g = resultDot;
        double G = Math.min(1, Math.min(
            (2 * NdotH_g * NdotV_g) / safeHdotV,
            (2 * NdotH_g * NdotL_g) / safeHdotV
        ));



        double NdotV = Math.max(0, Utils.dotProduct(normal, viewDir));
        double denominator = 4.0 * resultDot * NdotV + 1e-6;
        double common_part_eq = D * G / denominator;

        Vector3 diffContrib = new Vector3(
            (1 - F.X()) * (1 - metallic) * diff.X() / Math.PI,
            (1 - F.Y()) * (1 - metallic) * diff.Y() / Math.PI,
            (1 - F.Z()) * (1 - metallic) * diff.Z() / Math.PI
        );

        Vector3 spec = new Vector3(
            F.X() * common_part_eq * lightColor.R() * atenuation * resultDot,
            F.Y() * common_part_eq * lightColor.G() * atenuation * resultDot,
            F.Z() * common_part_eq * lightColor.B() * atenuation * resultDot
        );

        return diffContrib.add(spec);
    }

    /**
     * Rotates the light's orientation around the X-axis.
     *
     * @param angleInRad rotation angle in radians
     */
    public void rotateX(double angleInRad){
        up = up.rotateX(angleInRad).normalize();
        side = side.rotateX(angleInRad).normalize();
    }

    /**
     * Rotates the light's orientation around the Y-axis.
     *
     * @param angleInRad rotation angle in radians
     */
    public void rotateY(double angleInRad){
        up = up.rotateY(angleInRad).normalize();
        side = side.rotateY(angleInRad).normalize();
    }

    /**
     * Rotates the light's orientation around the Z-axis.
     *
     * @param angleInRad rotation angle in radians
     */
    public void rotateZ(double angleInRad){
        up = up.rotateZ(angleInRad).normalize();
        side = side.rotateZ(angleInRad).normalize();
    }

}
