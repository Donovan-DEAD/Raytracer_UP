package com.github.donovan_dead.Raytracer;

import com.github.donovan_dead.Math.Utils;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Plane;
import com.github.donovan_dead.Physics.Ray;

public class Camera {

    Vector3 height = Vector3.builder().X(0).Y(1).Z(0).build(); // eje vertical
    Vector3 width  = Vector3.builder().X(1).Y(0).Z(0).build(); // eje horizontal
    Ray normal;

    double focal_distance;
    Vector3 center;

    double aspect_ratio = 16.0/9.0; // width / height
    double viewport_width;
    double viewport_height;

    double farplane_dist;

    Vector3 horizontal;
    Vector3 vertical;
    Vector3 lower_left_corner;

    public Camera(Vector3 center, double f, double far) {
        this.center = center;
        this.focal_distance = f;
        this.farplane_dist = far;

        updateOrientation();
        updateViewport();
    }

    private void updateOrientation() {
        this.normal = new Ray(center, Utils.crossProduct(height, width).normalize());
    }

    private void updateViewport() {
        viewport_height = 2.0;
        viewport_width  = aspect_ratio * viewport_height;

        horizontal = width.normalize().scale(viewport_width);
        vertical   = height.normalize().scale(viewport_height);

        lower_left_corner =
            center
                .subtract(horizontal.scale(0.5))
                .subtract(vertical.scale(0.5))
                .add(normal.direction().scale(focal_distance));
    }

    public void setAspectRatio(double ar) {
        this.aspect_ratio = ar;
        updateViewport();
    }

    public void rotateX(double angleRadians) {
        this.height = this.height.rotateX(angleRadians).normalize();
        this.width  = this.width.rotateX(angleRadians).normalize();

        updateOrientation();
        updateViewport();
    }

    public void rotateY(double angleRadians) {
        this.height = this.height.rotateY(angleRadians).normalize();
        this.width  = this.width.rotateY(angleRadians).normalize();

        updateOrientation();
        updateViewport();
    }

    public void rotateZ(double angleRadians) {
        this.height = this.height.rotateZ(angleRadians).normalize();
        this.width  = this.width.rotateZ(angleRadians).normalize();

        updateOrientation();
        updateViewport();
    }

    public void translate(Vector3 t){
        this.center = center.add(t);
        
        updateOrientation();
        updateViewport();
    }

    public Ray getRay(double u, double v) {
        Vector3 pixel =
            lower_left_corner
                .add(horizontal.scale(u))
                .add(vertical.scale(v));

        return new Ray(center, pixel.subtract(center).normalize());
    }

    public Plane getFarPlane(){
        Ray r = getRay(0.5, 0.5);

        return new Plane(
            r.direction().scale(-1), 
            r.getPos(farplane_dist), 
            null
        );
    }
}
