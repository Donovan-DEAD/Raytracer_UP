package com.github.donovan_dead.Math;

public record Vector3 (double X, double Y, double Z){
    public double getMagnitude(){
        return Math.sqrt(
            Math.pow(X(), 2) +
            Math.pow(Y(), 2) +
            Math.pow(Z(), 2)
        );
    }

    public Vector3 normalize() {
        return Vector3.builder()
                .X(X() / getMagnitude())
                .Y(Y() / getMagnitude())
                .Z(Z() / getMagnitude())
                .build();
    }

    public Vector3 rotateX(double angleRadians) {
        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);

        double newY = Y() * cos - Z() * sin;
        double newZ = Y() * sin + Z() * cos;

        return Vector3.builder()
                .X(X())
                .Y(newY)
                .Z(newZ)
                .build();
    }

    public Vector3 rotateY(double angleRadians) {
        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);

        double newX = X() * cos + Z() * sin;
        double newZ = -X() * sin + Z() * cos;

        return Vector3.builder()
                .X(newX)
                .Y(Y())
                .Z(newZ)
                .build();
    }

    public Vector3 rotateZ(double angleRadians) {
        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);

        double newX = X() * cos - Y() * sin;
        double newY = X() * sin + Y() * cos;

        return Vector3.builder()
                .X(newX)
                .Y(newY)
                .Z(Z())
                .build();
    }

    public Vector3 scale(double scale){
        return Vector3.builder()
        .X(this.X()*scale)
        .Y(this.Y()*scale)
        .Z(this.Z()*scale)
        .build();
    }

    public Vector3 subtract(Vector3 b){
        return Vector3.builder()
            .X(this.X() - b.X())
            .Y(this.Y() - b.Y())
            .Z(this.Z() - b.Z())
            .build();
    }

    public Vector3 add(Vector3 b){
        return Vector3.builder()
            .X(this.X() + b.X())
            .Y(this.Y() + b.Y())
            .Z(this.Z() + b.Z())
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private double x, y, z;

        public Builder X(double x) { this.x = x; return this; }
        public Builder Y(double y) { this.y = y; return this; }
        public Builder Z(double z) { this.z = z; return this; }

        public Vector3 build() {
            return new Vector3(x, y, z);
        }
    }
};
