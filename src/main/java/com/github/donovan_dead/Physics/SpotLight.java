package com.github.donovan_dead.Physics;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Utils;
import com.github.donovan_dead.Math.Vector3;

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
    public Vector3 getLightContribution(Vector3 position, Vector3 normal, Vector3 baseColor) {
        Vector3 vecToLight     = origin.subtract(position);
        Vector3 vecToLightNorm = vecToLight.normalize();

        // angle between spotlight direction and ray from light to surface
        double cosTheta = Utils.dotProduct(direction, vecToLightNorm.scale(-1));

        if (cosTheta < cosOuter) return Vector3.Zero();

        // quadratic falloff in the penumbra ring
        double spotFactor = (cosTheta >= cosInner) ? 1.0
            : Math.pow((cosTheta - cosOuter) / (cosInner - cosOuter), 2);

        double diffuse = Math.max(0, Utils.dotProduct(vecToLightNorm, normal.normalize()));

        return Vector3.builder()
            .X(baseColor.X() * lightColor.R() * intensity / Math.pow(vecToLight.getMagnitude(), 2) * diffuse * spotFactor)
            .Y(baseColor.Y() * lightColor.G() * intensity / Math.pow(vecToLight.getMagnitude(), 2) * diffuse * spotFactor)
            .Z(baseColor.Z() * lightColor.B() * intensity / Math.pow(vecToLight.getMagnitude(), 2) * diffuse * spotFactor)
            .build();
    }
}
