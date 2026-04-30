package com.github.donovan_dead.Objects;

import java.util.ArrayList;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Utils;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Physics.Intersection;
import com.github.donovan_dead.Physics.Ray;


public class ObjObject extends Object3D {
    
    private static final double EPSILON = 1e-8;
    
    ArrayList<Vector3> vertexList = new ArrayList<>();
    ArrayList<Integer> vertIdxList = new ArrayList<>();

    RGBColor color;

    public Intersection calculateIntersection(Ray ray){
        Intersection ans = null;

        Vector3 v0;
        Vector3 v1;
        Vector3 v2;

        for(int i = 0; i < vertIdxList.size(); i += 3){
            v0 =  vertexList.get(vertIdxList.get(i).intValue());
            v1 =  vertexList.get(vertIdxList.get(i+1).intValue());
            v2 =  vertexList.get(vertIdxList.get(i+2).intValue());
            
            Vector3 edge1 = v1.subtract(v0);
            Vector3 edge2 = v2.subtract(v0);

            Vector3 h = Utils.crossProduct(ray.direction(), edge2);
            double a = Utils.dotProduct(edge1, h);

            if (Math.abs(a) < EPSILON) continue;

            double f = 1.0 / a;
            Vector3 s = ray.origin().subtract(v0);
            double u = f * Utils.dotProduct(s, h);

            if (u < 0.0 || u > 1.0) continue;

            Vector3 q = Utils.crossProduct(s, edge1);
            double v = f * Utils.dotProduct(ray.direction(), q);

            if (v < 0.0 || u + v > 1.0) continue;

            double t = f * Utils.dotProduct(edge2, q);

            if (t < EPSILON) continue;

            Vector3 normal = Utils.crossProduct(edge1, edge2).normalize();

            if(ans == null){
                ans = new Intersection(normal, t, color);
                return ans;
            } else if (ans.t() > t){
                ans = new Intersection(normal, t, color);
            }
        }

        return ans;
    }
    
    public void scale(double scale){} // No implementation right now.
    public void translate(Vector3 v){} // No implementation right now.

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        ArrayList<Vector3> vertex;
        ArrayList<Integer> vertIdxList;
        RGBColor color;

        public Builder vertexList(ArrayList<Vector3> v){
            this.vertex = v;
            return this;
        }

        public Builder vertexIdxList(ArrayList<Integer> idxList){
            this.vertIdxList = idxList;
            return this;
        }

        public Builder  color( RGBColor color){
            this.color = color;
            return this;
        }

        public ObjObject build() {
            ObjObject object = new ObjObject();
            object.vertexList = this.vertex;
            object.vertIdxList = this.vertIdxList;
            object.color = this.color;

            return object;
        }
    }
}
