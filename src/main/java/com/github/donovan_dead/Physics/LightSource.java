package com.github.donovan_dead.Physics;

import java.awt.Color;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.UV;
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
    public Vector3 getLightContribution(Vector3 position, Vector3 normal, Material material, Vector3 origin, UV uv) {
        Vector3 vecToLightRaw = this.origin.subtract(position);
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

        // Cook torrence implementation

        // Vectors V and H of the equations for the function of specular
        Vector3 viewDir = origin.subtract(position).normalize();
        Vector3 halfVec = vecToLight.add(viewDir).normalize();

        // Distribution of microfacets function
        double roughness2;
        
        if(material.getRoughnessTexture() != null){
            Color c = new Color(material.getRoughnessTexture().getPixel(uv));

            roughness2 = Math.pow( material.getRoughness() * c.getRed()  / 255.0 ,2);
        } else {
            roughness2 = material.getRoughness() * material.getRoughness();
        }

        // D(h) = a^2 / (Pi * (NdotH^2 * (a^2- 1) + 1)^2 )
        double NdotH_d = Utils.dotProduct(normal, halfVec);
        double D = roughness2 / (Math.PI * Math.pow(NdotH_d * NdotH_d * (roughness2 - 1) + 1, 2) + 1e-7);

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
        
        // F(v, h) =  F0 + (1 - F0)(1-Cos)^5
        // cos = VdotH
        // F(v, h) =  F0 + (1 - F0)(1-VdotH)^5
        Vector3 F = new Vector3(
            F0.X() + (1 - F0.X()) * oneMinusCos5,
            F0.Y() + (1 - F0.Y()) * oneMinusCos5,
            F0.Z() + (1 - F0.Z()) * oneMinusCos5
        );

        // I'm using the simplified form of the formula for the geometric shadowing/masking function in order to save time from the square roots
        // G(n, v, l) = min( 1, 2 * (NdotH) * (NdotV) / VdotH,  2 * (NdotH) * (NdotL) / VdotH )
        // Clamp all dot products to >= 0: perturbed normals (from normal maps) can produce negative
        // NdotV or NdotL, making G negative and spec a negative number that corrupts color channels.
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

        // fspec(v,h,l) = F * D * G / (4 * NdotH * NdotL) 
        // because F is different for each channel I precalculate the result of the common part of the equation
        Vector3 spec = new Vector3(
            F.X() * common_part_eq * lightColor.R() * atenuation * resultDot,
            F.Y() * common_part_eq * lightColor.G() * atenuation * resultDot,
            F.Z() * common_part_eq * lightColor.B() * atenuation * resultDot
        );

        return diffContrib.add(spec);
    }
}