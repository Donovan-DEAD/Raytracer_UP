package com.github.donovan_dead.Physics;

import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Structures.Material;

public abstract class BaseLightSource {
    public abstract Vector3 getLightContribution(Vector3 position, Vector3 normal, Material material, Vector3 origin);
}
