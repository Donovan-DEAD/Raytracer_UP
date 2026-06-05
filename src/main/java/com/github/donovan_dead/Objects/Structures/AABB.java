package com.github.donovan_dead.Objects.Structures;

import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Physics.Ray;

/**
 * Axis-Aligned Bounding Box used for spatial acceleration and ray-box intersection tests.
 * Stores min/max coordinates and provides efficient bounding volume operations.
 */
public class AABB {
    private double minX, minY, minZ;
    private double maxX, maxY, maxZ;

    /**
     * Constructs an AABB from minimum and maximum corner vectors.
     *
     * @param min the minimum corner (low X, Y, Z values)
     * @param max the maximum corner (high X, Y, Z values)
     */
    public AABB(Vector3 min, Vector3 max) {
        this.minX = min.X(); this.minY = min.Y(); this.minZ = min.Z();
        this.maxX = max.X(); this.maxY = max.Y(); this.maxZ = max.Z();
    }

    /**
     * Gets the minimum corner of the bounding box.
     *
     * @return the minimum corner vector
     */
    public Vector3 min() { return new Vector3(minX, minY, minZ); }

    /**
     * Gets the maximum corner of the bounding box.
     *
     * @return the maximum corner vector
     */
    public Vector3 max() { return new Vector3(maxX, maxY, maxZ); }

    /**
     * Tests ray-box intersection using the slab method.
     * Handles division by zero for rays parallel to axes.
     *
     * @param ray the ray to test
     * @return true if the ray intersects this box, false otherwise
     */
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

    /**
     * Calculates the surface area of this bounding box.
     * Used in SAH (Surface Area Heuristic) cost functions during BVH construction.
     *
     * @return the total surface area
     */
    public double getSurfaceArea() {
        return  (maxX - minX) * (maxY - minY) +
                (maxZ - minZ) * (maxY - minY) +
                (maxX - minX) * (maxZ - minZ);
    }

    /**
     * Gets the geometric centroid of this bounding box.
     *
     * @return the center point
     */
    public Vector3 getCentroid() {
        return new Vector3(
            (minX + maxX) / 2d,
            (minY + maxY) / 2d,
            (minZ + maxZ) / 2d
        );
    }

    /**
     * Extends this bounding box to include the given vertex.
     * Modifies this AABB in-place and returns it for chaining.
     *
     * @param v the vertex to include
     * @return this AABB for method chaining
     */
    public AABB extendToVertex(Vector3 v) {
        if (v.X() < minX) minX = v.X();
        if (v.Y() < minY) minY = v.Y();
        if (v.Z() < minZ) minZ = v.Z();
        if (v.X() > maxX) maxX = v.X();
        if (v.Y() > maxY) maxY = v.Y();
        if (v.Z() > maxZ) maxZ = v.Z();
        return this;
    }

    /**
     * Extends this bounding box to include another AABB.
     * Modifies this AABB in-place and returns it for chaining.
     *
     * @param other the AABB to include
     * @return this AABB for method chaining
     */
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
