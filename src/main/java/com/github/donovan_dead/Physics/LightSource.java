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

        Vector3 diff;
        if(material.getDiffuseTexture() != null){
            
            Color c  = new Color(material.getDiffuseTexture().getPixel(uv));

            diff = Vector3.builder()
                .X(material.getKd().X() * c.getRed() / 255.0 * lightColor.R() * atenuation * resultDot)
                .Y(material.getKd().Y() * c.getGreen() / 255.0 * lightColor.G() * atenuation * resultDot)
                .Z(material.getKd().Z() * c.getBlue() / 255.0 * lightColor.B() * atenuation * resultDot)
            .build(); 

        } else 
            diff = Vector3.builder()
                .X(material.getKd().X() * lightColor.R() * atenuation * resultDot)
                .Y(material.getKd().Y() * lightColor.G() * atenuation * resultDot)
                .Z(material.getKd().Z() * lightColor.B() * atenuation * resultDot)
            .build();

        Vector3 reflection = normal.scale(2 * resultDot).subtract(vecToLight.scale(-1)).normalize();
        Vector3 viewDir = origin.subtract(position).normalize();
        
        double ns = material.getNsTexture() != null
            ? material.getNs() * (new Color(material.getNsTexture().getPixel(uv)).getRed() / 255.0)
            : material.getNs();

        double specFactor = Math.pow(
            Math.max(0, Utils.dotProduct(reflection, viewDir)),
            ns
        );

        Vector3 spec;
        if(material.getSpecularTexture() != null){
            Color c  = new Color(material.getSpecularTexture().getPixel(uv));
            
            spec = Vector3.builder()
                .X(material.getKs().X() * c.getRed() / 255.0 * lightColor.R() * atenuation * specFactor)
                .Y(material.getKs().Y() * c.getGreen() / 255.0 * lightColor.G() * atenuation * specFactor)
                .Z(material.getKs().Z() * c.getBlue() / 255.0 * lightColor.B() * atenuation * specFactor)
            .build();

        } else 
            spec = Vector3.builder()
                .X(material.getKs().X() * lightColor.R() * atenuation * specFactor)
                .Y(material.getKs().Y() * lightColor.G() * atenuation * specFactor)
                .Z(material.getKs().Z() * lightColor.B() * atenuation * specFactor)
            .build();

        return diff.add(spec);
    }
}