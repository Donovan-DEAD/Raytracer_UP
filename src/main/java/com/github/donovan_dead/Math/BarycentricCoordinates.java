package com.github.donovan_dead.Math;

public record BarycentricCoordinates(double alpha, double beta, double gamma) {
    public boolean isInside(double epsilon) {
        return alpha >= -epsilon && beta >= -epsilon && gamma >= -epsilon;
    }
}
