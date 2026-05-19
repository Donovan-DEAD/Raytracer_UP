package com.github.donovan_dead.Physics;

import com.github.donovan_dead.Math.UV;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Structures.Material;

public record Intersection( Vector3 normal, double t, Material material, UV uv){
    
} 
