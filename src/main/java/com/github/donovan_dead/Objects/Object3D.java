package com.github.donovan_dead.Objects;

import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Structures.AABB;
import com.github.donovan_dead.Physics.Intersection;
import com.github.donovan_dead.Physics.Ray;

public abstract class Object3D {
    protected static final double inf_T = 1e10;
    public abstract Intersection calculateIntersection(Ray ray);
    public abstract void translate(Vector3 v);
    public abstract void scale(double s);
    public abstract AABB getBox();
}
