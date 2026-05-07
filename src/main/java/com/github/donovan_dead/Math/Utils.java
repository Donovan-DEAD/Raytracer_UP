package com.github.donovan_dead.Math;

/**
 * Utility class for vector mathematics and geometric calculations.
 * Provides functions for basic vector operations and advanced calculations
 * such as barycentric coordinates.
 */
public class Utils {
    private static final double EPSILON = 1e-9;

    /**
     * Calculates the dot product (scalar product) between two vectors.
     * The result indicates the projection of one vector onto another.
     *
     * @param a the first vector
     * @param b the second vector
     * @return the dot product a · b
     */
    public static double dotProduct (Vector3 a, Vector3 b){
        return a.X() * b.X() + a.Y() * b.Y() + a.Z() * b.Z();
    }

    /**
     * Calculates the cross product (vector product) between two vectors.
     * The result is a new vector perpendicular to both input vectors.
     * Useful for calculating surface normals.
     *
     * @param a the first vector
     * @param b the second vector
     * @return the resulting vector a × b (perpendicular to both)
     */
    public static Vector3 crossProduct(Vector3 a, Vector3 b){
        return Vector3.builder()
                .X(a.Y() * b.Z() - a.Z() * b.Y())
                .Y(a.Z() * b.X() - a.X() * b.Z())
                .Z(a.X() * b.Y() - a.Y() * b.X())
                .build();
    }

    /**
     * Calculates the angle in radians between two vectors.
     * The result is in the range [0, π].
     *
     * @param a the first vector
     * @param b the second vector
     * @return the angle in radians between the two vectors
     */
    public static double angleBetweenVectors(Vector3 a, Vector3 b){
        return Math.acos(Utils.dotProduct(a.normalize(), b.normalize()));
    }

    /**
     * Calculates the difference between two vectors (a - b).
     * Equivalent to a.subtract(b).
     *
     * @param a the minuend vector
     * @param b the subtrahend vector
     * @return the difference vector (a - b)
     */
    public static Vector3 diffVector3(Vector3 a, Vector3 b){
        return Vector3.builder()
            .X(a.X() - b.X())
            .Y(a.Y() - b.Y())
            .Z(a.Z() - b.Z())
            .build();
    }

    /**
     * Calculates the barycentric coordinates of a point with respect to a triangle.
     *
     * Barycentric coordinates allow expressing any point as a linear combination
     * of the triangle's vertices: P = alpha*v0 + beta*v1 + gamma*v2
     *
     * Where:
     * - alpha corresponds to the weight of vertex v0 (first triangle corner)
     * - beta corresponds to the weight of vertex v1 (second triangle corner)
     * - gamma corresponds to the weight of vertex v2 (third triangle corner)
     *
     * A point is inside the triangle if alpha >= 0, beta >= 0, and gamma >= 0.
     *
     * @param p the point to evaluate
     * @param v0 the first vertex of the triangle
     * @param v1 the second vertex of the triangle
     * @param v2 the third vertex of the triangle
     * @return a BarycentricCoordinates object containing the alpha, beta, and gamma values
     */
    public static BarycentricCoordinates calculateBarycentricCoordinates(Vector3 p, Vector3 v0, Vector3 v1, Vector3 v2) {
        Vector3 e0 = v1.subtract(v0);
        Vector3 e1 = v2.subtract(v0);
        Vector3 v  = p.subtract(v0);

        double d00 = dotProduct(e0, e0);
        double d01 = dotProduct(e0, e1);
        double d11 = dotProduct(e1, e1);
        double d20 = dotProduct(v,  e0);
        double d21 = dotProduct(v,  e1);

        double den = d00 * d11 - d01 * d01;

        if (Math.abs(den) < 1e-12) {
            return new BarycentricCoordinates(0, 0, 0);
        }

        double beta  = (d11 * d20 - d01 * d21) / den;
        double gamma = (d00 * d21 - d01 * d20) / den;
        double alpha = 1.0 - beta - gamma;

        return new BarycentricCoordinates(alpha, beta, gamma);
    }

    /**
     * Determines whether a point lies inside a triangle using barycentric coordinates.
     * This is an optimized method that avoids expensive square root operations
     * while providing accurate point-in-triangle testing.
     *
     * @param p the point to test
     * @param v0 the first vertex of the triangle
     * @param v1 the second vertex of the triangle
     * @param v2 the third vertex of the triangle
     * @return true if the point is inside or on the boundary of the triangle, false otherwise
     */
    public static boolean pointInTriangle(Vector3 p, Vector3 v0, Vector3 v1, Vector3 v2) {
        BarycentricCoordinates coords = calculateBarycentricCoordinates(p, v0, v1, v2);
        return coords.isInside(EPSILON);
    }
}
