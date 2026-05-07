package com.github.donovan_dead.Physics;

import com.github.donovan_dead.Math.Vector3;

public abstract class BaseLightSource {
    public abstract Vector3 getLightContribution(Vector3 position, Vector3 normal, Vector3 baseColor);
}
