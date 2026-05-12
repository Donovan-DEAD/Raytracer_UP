package com.github.donovan_dead.Objects;

import java.util.ArrayList;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.BarycentricCoordinates;
import com.github.donovan_dead.Math.Utils;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Structures.AABB;
import com.github.donovan_dead.Objects.Structures.BVHNode;
import com.github.donovan_dead.Physics.Intersection;
import com.github.donovan_dead.Physics.Ray;


public class ObjObject extends Object3D {
    
    private static final double EPSILON = 1e-8;
    
    ArrayList<Vector3> vertexList = new ArrayList<>();
    ArrayList<Integer> vertIdxList = new ArrayList<>();
    
    ArrayList<Vector3> normalList = new ArrayList<>();
    ArrayList<Integer> normalIdxList = new ArrayList<>();

    public ArrayList<BVHNode> BVHTree;
    private ArrayList<Integer> auxiliarIdxList;
    RGBColor color;

    public Intersection calculateIntersection(Ray ray){
        // System.out.println(BVHTree.isEmpty());
        if(BVHTree.isEmpty()) return null;
        return recursiveIntersection(ray, 0);
    }

    private Intersection recursiveIntersection(Ray ray , int nodeIdx){
        BVHNode node = BVHTree.get(nodeIdx);

        if(node.isLeaf()){
            Intersection ans = null;

            Vector3 v0;
            Vector3 v1;
            Vector3 v2;

            for(int i = node.getStart(); i < node.getStart() + node.getCount(); i++){
                v0 =  vertexList.get(vertIdxList.get(i * 3).intValue());
                v1 =  vertexList.get(vertIdxList.get(i * 3 + 1).intValue());
                v2 =  vertexList.get(vertIdxList.get(i * 3 + 2).intValue());

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

                Vector3 normal;
                if (normalIdxList.get(i * 3) == -1 || normalIdxList.get(i * 3 + 1) == -1 || normalIdxList.get(i * 3 + 2) == -1)
                    normal = Utils.crossProduct(edge1, edge2).normalize();
                else {
                    Vector3 n0 = normalList.get(normalIdxList.get(i * 3));
                    Vector3 n1 = normalList.get(normalIdxList.get(i * 3 + 1));
                    Vector3 n2 = normalList.get(normalIdxList.get(i * 3 + 2));

                    BarycentricCoordinates b = Utils.calculateBarycentricCoordinates(ray.getPos(t), v0, v1, v2);

                    normal = Vector3.builder().X(0).Y(0).Z(0).build()
                    .add(n0.scale(b.alpha()))
                    .add(n1.scale(b.beta()))
                    .add(n2.scale(b.gamma()))
                    .normalize();
                }

                if(ans == null){
                    ans = new Intersection(normal, t, color);
                } else if (ans.t() > t){
                    ans = new Intersection(normal, t, color);
                }
            }
            return ans;
        }
        else {
            if(node.intersectsBox(ray)){
                Intersection iLeft = recursiveIntersection(ray, node.getLeftChild());
                Intersection iRight = recursiveIntersection(ray, node.getRightChild());

                if(iLeft == null && iRight == null) return null;
                else if(iLeft == null && iRight != null) return iRight;
                else if(iLeft != null && iRight == null) return iLeft;
                else return (iLeft.t() < iRight.t())? iLeft : iRight;
            }
            else return null;
        }
    }
    
    public void scale(double scale){
        Vector3 centroid = new Vector3(0,0,0);
        
        for(Vector3 vertex : vertexList) centroid = centroid.add(vertex);
        centroid = centroid.scale(1.0 / (double)vertexList.size());
        
        for(int i = 0; i < vertexList.size(); i++){
            vertexList.set(
                i, 
                vertexList.get(i).subtract(centroid).scale(scale).add(centroid)
            );
        }
    }
    
    public void translate(Vector3 v){
        for(int i = 0; i < vertexList.size(); i++){
            vertexList.set(
                i, 
                vertexList.get(i).add(v)
            );
        }

    }

    public void constructBVH(){
        auxiliarIdxList = new ArrayList<>(vertIdxList.size()/3);
        for(int i = 0; i < vertIdxList.size()/3; i++){
            auxiliarIdxList.add(i);
        }

        BVHTree = new ArrayList<>();
        recursiveBVHConstruction(0, auxiliarIdxList.size());

        // Reorganize the triangules in the best order for the BVH
        
        // Arrays for the new positioins of the normals and vertexes 
        ArrayList<Integer> newIndexes = new ArrayList<>();
        ArrayList<Integer> newNormalIndexes = new ArrayList<>();

        for(Integer idx : auxiliarIdxList){
            newIndexes.add(
                vertIdxList.get(idx * 3)
            );
            newIndexes.add(
                vertIdxList.get(idx * 3 + 1)
            );
            newIndexes.add(
                vertIdxList.get(idx * 3 + 2)
            );

            newNormalIndexes.add(
                normalIdxList.get(idx * 3)
            );
            newNormalIndexes.add(
                normalIdxList.get(idx * 3 + 1)
            );
            newNormalIndexes.add(
                normalIdxList.get(idx * 3 + 2)
            );
        }

        vertIdxList = newIndexes;
        normalIdxList = newNormalIndexes;
    }

    private int recursiveBVHConstruction(int start, int end){
        BVHNode node = new BVHNode();
        int nodeIdx = BVHTree.size();
        BVHTree.add(node);

        if(end - start <=  BVHNode.minLeafSize){
            node.setCount(end-start)
                .setStart(start)
                .setBox(constructAABB(start, end));
            return nodeIdx;
        }
        else {
            node.setCount(0)
                .setStart(0)
                .setBox(constructAABB(start, end));

            double candidateCost = Double.MAX_VALUE;
            int candidateIdx = 0;
            int candidateAxis = 0;
            double costTemp = 0;

            // Sort by X axis
            auxiliarIdxList.subList(start, end).sort((a, b)->{
                Vector3 v0 = getTrianguleCentroid(a);
                Vector3 v1 = getTrianguleCentroid(b);
                return Double.compare(v0.X(), v1.X());
            });

            for(int i = start + 1; i < end; i++){
                AABB left = constructAABB(start, i);
                AABB right = constructAABB(i, end);
                costTemp = left.getSurfaceArea() * (i - start) + right.getSurfaceArea() * (end - i);
                if( candidateCost > costTemp){
                    candidateAxis = 0;
                    candidateIdx = i;
                    candidateCost = costTemp;
                }
            }

            // Sort by Y axis
            auxiliarIdxList.subList(start, end).sort((a, b)->{
                Vector3 v0 = getTrianguleCentroid(a);
                Vector3 v1 = getTrianguleCentroid(b);
                return Double.compare(v0.Y(), v1.Y());
            });

            for(int i = start + 1; i < end; i++){
                AABB left = constructAABB(start, i);
                AABB right = constructAABB(i, end);
                costTemp = left.getSurfaceArea() * (i - start) + right.getSurfaceArea() * (end - i);
                if( candidateCost > costTemp){
                    candidateAxis = 1;
                    candidateIdx = i;
                    candidateCost = costTemp;
                }
            }

            // Sort by Z axis
            auxiliarIdxList.subList(start, end).sort((a, b)->{
                Vector3 v0 = getTrianguleCentroid(a);
                Vector3 v1 = getTrianguleCentroid(b);
                return Double.compare(v0.Z(), v1.Z());
            });

            for(int i = start + 1; i < end; i++){
                AABB left = constructAABB(start, i);
                AABB right = constructAABB(i, end);
                costTemp = left.getSurfaceArea() * (i - start) + right.getSurfaceArea() * (end - i);
                if( candidateCost > costTemp){
                    candidateAxis = 2;
                    candidateIdx = i;
                    candidateCost = costTemp;
                }
            }

            // Sort one final time by the best axis
            if(candidateAxis == 0)
                auxiliarIdxList.subList(start, end).sort((a, b)->{
                    Vector3 v0 = getTrianguleCentroid(a);
                    Vector3 v1 = getTrianguleCentroid(b);
                    return Double.compare(v0.X(), v1.X());
                });
            else if (candidateAxis == 1)
                auxiliarIdxList.subList(start, end).sort((a, b)->{
                    Vector3 v0 = getTrianguleCentroid(a);
                    Vector3 v1 = getTrianguleCentroid(b);
                    return Double.compare(v0.Y(), v1.Y());
                });
            else
                auxiliarIdxList.subList(start, end).sort((a, b)->{
                    Vector3 v0 = getTrianguleCentroid(a);
                    Vector3 v1 = getTrianguleCentroid(b);
                    return Double.compare(v0.Z(), v1.Z());
                });

            int leftIdx = recursiveBVHConstruction(start, candidateIdx);
            int rightIdx = recursiveBVHConstruction(candidateIdx, end);

            node.setLeftChild(leftIdx).setRightChild(rightIdx);

            return nodeIdx;
        }
    }

    private Vector3 getTrianguleCentroid(int index){
        Vector3 v0 =  vertexList.get(
            vertIdxList.get(
                index * 3
            )
        );

        v0 = v0.add(
            vertexList.get(
                vertIdxList.get(
                    index * 3 + 1
                )
            )
        );

        v0 = v0.add(
            vertexList.get(
                vertIdxList.get(
                    index * 3 + 2
                )
            )
        );
        return v0.scale(1.0/3.0);
    }

    private AABB constructAABB(int start, int end){
        AABB aabb = new AABB( 
           vertexList.get(
                vertIdxList.get(
                    auxiliarIdxList.get(start) * 3
                )
            ) , 
            vertexList.get(
                vertIdxList.get(
                    auxiliarIdxList.get(start) * 3
                )
            )
        );

        for(int i = start; i < end; i++){
            aabb = aabb.extendToVertex(
                vertexList.get(
                    vertIdxList.get(
                        auxiliarIdxList.get(i) * 3
                    )
                )
            );
            
            aabb = aabb.extendToVertex(
                vertexList.get(
                    vertIdxList.get(
                        auxiliarIdxList.get(i) * 3 + 1
                    )
                )
            );

            aabb = aabb.extendToVertex(
                vertexList.get(
                    vertIdxList.get(
                        auxiliarIdxList.get(i) * 3 + 2
                    )
                )
            );
        }


        return aabb;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        ArrayList<Vector3> vertex;
        ArrayList<Integer> vertIdxList;
            
        ArrayList<Vector3> normalList = new ArrayList<>();
        ArrayList<Integer> normalIdxList = new ArrayList<>();
        RGBColor color;

        public Builder vertexList(ArrayList<Vector3> v){
            this.vertex = v;
            return this;
        }

        public Builder vertexIdxList(ArrayList<Integer> idxList){
            this.vertIdxList = idxList;
            return this;
        }

        public Builder normalList(ArrayList<Vector3> normList){
            this.normalList = normList;
            return this;
        }

        public Builder normalIdxList(ArrayList<Integer> normIdxList){
            this.normalIdxList = normIdxList;
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

            object.normalList = this.normalList;
            object.normalIdxList = this.normalIdxList;

            object.color = this.color;

            return object;
        }
    }
}
