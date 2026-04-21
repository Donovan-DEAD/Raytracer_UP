package com.github.donovan_dead.Math;

public class Utils {
    public static double dotProduct (Vector3 a, Vector3 b){
        return a.X() * b.X() + a.Y() * b.Y() + a.Z() * b.Z();
    }

    public static Vector3 crossProduct(Vector3 a, Vector3 b){        
        return Vector3.builder()
                .X(a.Y() * b.Z() - a.Z() * b.Y())
                .Y(a.Z() * b.X() - a.X() * b.Z())
                .Z(a.X() * b.Y() - a.Y() * b.X())
                .build();
    }

    public static double angleBetweenVectors(Vector3 a, Vector3 b){
        return Math.acos(Utils.dotProduct(a.normalize(), b.normalize()));
    }

    public static Vector3 diffVector3(Vector3 a, Vector3 b){
        return Vector3.builder()
            .X(a.X() - b.X())
            .Y(a.Y() - b.Y())
            .Z(a.Z() - b.Z())
            .build();
    }
}
