package com.github.donovan_dead.Physics.areaLights;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Physics.BaseLightSource;

/**
 * Abstract base class for area light sources supporting stratified sampling.
 * Subclasses implement different geometric shapes (circle, rectangle, triangle).
 * Uses reduced sampling when Depth of Field is active to improve performance.
 */
public abstract class AreaLight extends BaseLightSource {
    /** Number of samples for area light without DOF */
    public static final int Samples = 23;
    /** Number of samples for area light with DOF (reduced for efficiency) */
    public static final int SamplesWithDOF = 10;
    protected Vector3 currentSample;

    /**
     * Constructs an area light with the given origin and color.
     *
     * @param origin the position of the light source
     * @param lightColor the RGB color of emitted light
     * @param intensity the intensity/power of the light
     */
    public AreaLight(Vector3 origin, RGBColor lightColor, double intensity) {
        super(origin, lightColor, intensity);
    }

    /**
     * Generates a random sample point on the light surface.
     * Used for stratified sampling in soft shadow computations.
     *
     * @return a random point on the area light surface
     */
    public abstract Vector3 getSample();
}
