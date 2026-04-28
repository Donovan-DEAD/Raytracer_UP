package com.github.donovan_dead.Objects;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Utils;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Physics.Intersection;
import com.github.donovan_dead.Physics.Ray;

public class Triangule extends Object3D {
    private static final double EPSILON = 1e-8;

    private Vector3 v0;
    private Vector3 v1;
    private Vector3 v2;
    private RGBColor color;

    public Triangule(Vector3 a, Vector3 b, Vector3 c, RGBColor color) {
        this.v0 = a;
        this.v1 = b;
        this.v2 = c;
        this.color = color;
    }

    public Intersection calculateIntersection(Ray ray) {
        Vector3 edge1 = v1.subtract(v0);
        Vector3 edge2 = v2.subtract(v0);

        Vector3 h = Utils.crossProduct(ray.direction(), edge2);
        double a = Utils.dotProduct(edge1, h);

        if (Math.abs(a) < EPSILON) return null;

        double f = 1.0 / a;
        Vector3 s = ray.origin().subtract(v0);
        double u = f * Utils.dotProduct(s, h);

        if (u < 0.0 || u > 1.0) return null;

        Vector3 q = Utils.crossProduct(s, edge1);
        double v = f * Utils.dotProduct(ray.direction(), q);

        if (v < 0.0 || u + v > 1.0) return null;

        double t = f * Utils.dotProduct(edge2, q);

        if (t < EPSILON) return null;

        Vector3 normal = Utils.crossProduct(edge1, edge2).normalize();
        return new Intersection(normal, t, color);
    }

    public void translate(Vector3 v) {
        v0 = v0.add(v);
        v1 = v1.add(v);
        v2 = v2.add(v);
    }

    public void scale(double s) {
        Vector3 centroid = new Vector3(
            (v0.X() + v1.X() + v2.X()) / 3.0,
            (v0.Y() + v1.Y() + v2.Y()) / 3.0,
            (v0.Z() + v1.Z() + v2.Z()) / 3.0
        );
        v0 = centroid.add(v0.subtract(centroid).scale(s));
        v1 = centroid.add(v1.subtract(centroid).scale(s));
        v2 = centroid.add(v2.subtract(centroid).scale(s));
    }
}
