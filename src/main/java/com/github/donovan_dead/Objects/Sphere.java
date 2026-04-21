package com.github.donovan_dead.Objects;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Utils;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Physics.Intersection;
import com.github.donovan_dead.Physics.Ray;

public class Sphere extends Object3D{
    private Vector3 center;
    private double radius;
    private RGBColor color;

    public Sphere(Vector3 c, double r, RGBColor color){
        this.center = c;
        this.radius = r;
        this.color = color;
    }

    public Intersection calculateIntersection(Ray ray){
        Vector3 L = Utils.diffVector3(ray.origin(), center);

        double tc = -Utils.dotProduct(L, ray.direction());
        if(tc < 0) return null;

        double d2 = Utils.dotProduct(L, L) - tc * tc;
        if(d2 >= radius * radius) return null;

        double t = tc - Math.sqrt(radius * radius - d2);
        if(t < 0) return null;

        return new Intersection(
            Utils.diffVector3(ray.getPos(t), center).normalize(),
            t,
            color
        );
    }
}
