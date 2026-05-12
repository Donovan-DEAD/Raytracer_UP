package com.github.donovan_dead.Objects.Structures;

import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Physics.Ray;

public record AABB(Vector3 min, Vector3 max) {

    public boolean intersectsBox(Ray ray){
        double txMin = (min.X() - ray.origin().X()) / ray.direction().X();
        double txMax = (max.X() - ray.origin().X()) / ray.direction().X();
        if (txMin > txMax) { double t = txMin; txMin = txMax; txMax = t; }

        double tyMin = (min.Y() - ray.origin().Y()) / ray.direction().Y();
        double tyMax = (max.Y() - ray.origin().Y()) / ray.direction().Y();
        if (tyMin > tyMax) { double t = tyMin; tyMin = tyMax; tyMax = t; }

        double tzMin = (min.Z() - ray.origin().Z()) / ray.direction().Z();
        double tzMax = (max.Z() - ray.origin().Z()) / ray.direction().Z();
        if (tzMin > tzMax) { double t = tzMin; tzMin = tzMax; tzMax = t; }

        double tMin = Math.max(txMin, Math.max(tyMin, tzMin));
        double tMax = Math.min(txMax, Math.min(tyMax, tzMax));

        return tMax >= tMin && tMax >= 0;
    }

    public double getSurfaceArea(){
        return  (max.X() - min.X()) * (max.Y() - min.Y()) +
                (max.Z() - min.Z()) * (max.Y() - min.Y()) +  
                (max.X() - min.X()) * (max.Z() - min.Z());
    }

    public AABB extendToVertex(Vector3 v){
        return new AABB(
            new Vector3(
                Math.min(min.X(), v.X()), 
                Math.min(min.Y(), v.Y()), 
                Math.min(min.Z(), v.Z())
            ),
            new Vector3(
                Math.max(max.X(), v.X()), 
                Math.max(max.Y(), v.Y()), 
                Math.max(max.Z(), v.Z())
            ));
    }
} 