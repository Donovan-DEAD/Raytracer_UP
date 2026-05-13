package com.github.donovan_dead.Objects.Structures;

import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Physics.Ray;

public class AABB {
    private double minX, minY, minZ;
    private double maxX, maxY, maxZ;

    public AABB(Vector3 min, Vector3 max) {
        this.minX = min.X(); this.minY = min.Y(); this.minZ = min.Z();
        this.maxX = max.X(); this.maxY = max.Y(); this.maxZ = max.Z();
    }

    public Vector3 min() { return new Vector3(minX, minY, minZ); }
    public Vector3 max() { return new Vector3(maxX, maxY, maxZ); }

    public boolean intersectsBox(Ray ray) {
        double txMin = (minX - ray.origin().X()) / ray.direction().X();
        double txMax = (maxX - ray.origin().X()) / ray.direction().X();
        if (txMin > txMax) { double t = txMin; txMin = txMax; txMax = t; }

        double tyMin = (minY - ray.origin().Y()) / ray.direction().Y();
        double tyMax = (maxY - ray.origin().Y()) / ray.direction().Y();
        if (tyMin > tyMax) { double t = tyMin; tyMin = tyMax; tyMax = t; }

        double tzMin = (minZ - ray.origin().Z()) / ray.direction().Z();
        double tzMax = (maxZ - ray.origin().Z()) / ray.direction().Z();
        if (tzMin > tzMax) { double t = tzMin; tzMin = tzMax; tzMax = t; }

        double tMin = Math.max(txMin, Math.max(tyMin, tzMin));
        double tMax = Math.min(txMax, Math.min(tyMax, tzMax));

        return tMax >= tMin && tMax >= 0;
    }

    public double getSurfaceArea() {
        return  (maxX - minX) * (maxY - minY) +
                (maxZ - minZ) * (maxY - minY) +
                (maxX - minX) * (maxZ - minZ);
    }

    public Vector3 getCentroid() {
        return new Vector3(
            (minX + maxX) / 2d,
            (minY + maxY) / 2d,
            (minZ + maxZ) / 2d
        );
    }

    public AABB extendToVertex(Vector3 v) {
        if (v.X() < minX) minX = v.X();
        if (v.Y() < minY) minY = v.Y();
        if (v.Z() < minZ) minZ = v.Z();
        if (v.X() > maxX) maxX = v.X();
        if (v.Y() > maxY) maxY = v.Y();
        if (v.Z() > maxZ) maxZ = v.Z();
        return this;
    }

    public AABB extendToAABB(AABB other) {
        if (other.minX < minX) minX = other.minX;
        if (other.minY < minY) minY = other.minY;
        if (other.minZ < minZ) minZ = other.minZ;
        if (other.maxX > maxX) maxX = other.maxX;
        if (other.maxY > maxY) maxY = other.maxY;
        if (other.maxZ > maxZ) maxZ = other.maxZ;
        return this;
    }
}
