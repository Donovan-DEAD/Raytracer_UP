package com.github.donovan_dead.Objects;

import com.github.donovan_dead.Math.Utils;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Structures.AABB;
import com.github.donovan_dead.Objects.Structures.Material;
import com.github.donovan_dead.Physics.Intersection;
import com.github.donovan_dead.Physics.Ray;

public class Plane extends Object3D {
    
    private Vector3 normal;
    private Vector3 origin;

    private Material color;

    public Plane(Vector3 n, Vector3 o, Material m){
        this.normal = n.normalize();
        this.origin = o;
        this.color = m;
    }

    public void setColor(Material m){ this.color = m; }

    public void translate(Vector3 v) {
        origin = origin.add(v);
    }

    public void scale(double s) {}

    public AABB getBox() {
        return new AABB(
            new Vector3(-inf_T, -inf_T, -inf_T),
            new Vector3(inf_T, inf_T, inf_T)
        );
    }

    public Intersection calculateIntersection(Ray ray){
        Vector3 dir = ray.direction();
        double dotDen = Utils.dotProduct(dir, normal);

        if (dotDen == 0 ) return null;

        Vector3 difference = origin.subtract(ray.origin());
        double dot = Utils.dotProduct(normal, difference);

        if(dot/dotDen  < 0) return null;
        return new Intersection(
            normal, 
            dot/dotDen, 
            color,
            null
        );
    }
}
