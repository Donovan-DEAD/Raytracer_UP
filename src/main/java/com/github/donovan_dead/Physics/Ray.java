package com.github.donovan_dead.Physics;

import com.github.donovan_dead.Math.Vector3;

public record Ray( Vector3 origin, Vector3 direction) {

    public Vector3 getPos(double t){
        return Vector3.builder()
                .X(t * direction.X() + origin.X())
                .Y(t * direction.Y() + origin.Y())
                .Z(t * direction.Z() + origin.Z())
                .build();
    }

    public Vector3 getRay(double t){
        return Vector3.builder()
                .X(t * direction.X())
                .Y(t * direction.Y())
                .Z(t * direction.Z())
                .build();
    }

} 
