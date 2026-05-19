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
        Vector3 vecToLight     = this.origin.subtract(position);
        Vector3 vecToLightNorm = vecToLight.normalize();

        // angle between spotlight direction and ray from light to surface
        double cosTheta = Utils.dotProduct(direction, vecToLightNorm.scale(-1));

        if (cosTheta < cosOuter) return Vector3.Zero();

        // quadratic falloff in the penumbra ring
        double spotFactor = (cosTheta >= cosInner) ? 1.0
            : Math.pow((cosTheta - cosOuter) / (cosInner - cosOuter), 2);

        double diffuse = Math.max(0, Utils.dotProduct(vecToLightNorm, normal.normalize()));
        double atenuation = intensity / Math.pow(vecToLight.getMagnitude(), 2);

        Vector3 diff;
        if (material.getDiffuseTexture() != null) {
            Color c = new Color(material.getDiffuseTexture().getPixel(uv));
            diff = Vector3.builder()
                .X(material.getKd().X() * c.getRed() / 255.0 * lightColor.R() * atenuation * diffuse * spotFactor)
                .Y(material.getKd().Y() * c.getGreen() / 255.0 * lightColor.G() * atenuation * diffuse * spotFactor)
                .Z(material.getKd().Z() * c.getBlue() / 255.0 * lightColor.B() * atenuation * diffuse * spotFactor)
                .build();
        } else
            diff = Vector3.builder()
                .X(material.getKd().X() * lightColor.R() * atenuation * diffuse * spotFactor)
                .Y(material.getKd().Y() * lightColor.G() * atenuation * diffuse * spotFactor)
                .Z(material.getKd().Z() * lightColor.B() * atenuation * diffuse * spotFactor)
                .build();

        Vector3 reflection = normal.scale(2 * diffuse).subtract(vecToLightNorm.scale(-1)).normalize();
        Vector3 viewDir = origin.subtract(position).normalize();

        double ns = material.getNsTexture() != null
            ? material.getNs() * (new Color(material.getNsTexture().getPixel(uv)).getRed() / 255.0)
            : material.getNs();

        double specFactor = Math.pow(
            Math.max(0, Utils.dotProduct(reflection, viewDir)),
            ns
        );

        Vector3 spec;
        if (material.getSpecularTexture() != null) {
            Color c = new Color(material.getSpecularTexture().getPixel(uv));
            spec = Vector3.builder()
                .X(material.getKs().X() * c.getRed() / 255.0 * lightColor.R() * atenuation * specFactor * spotFactor)
                .Y(material.getKs().Y() * c.getGreen() / 255.0 * lightColor.G() * atenuation * specFactor * spotFactor)
                .Z(material.getKs().Z() * c.getBlue() / 255.0 * lightColor.B() * atenuation * specFactor * spotFactor)
                .build();
        } else
            spec = Vector3.builder()
                .X(material.getKs().X() * lightColor.R() * atenuation * specFactor * spotFactor)
                .Y(material.getKs().Y() * lightColor.G() * atenuation * specFactor * spotFactor)
                .Z(material.getKs().Z() * lightColor.B() * atenuation * specFactor * spotFactor)
                .build();

        return diff.add(spec);
    }
}