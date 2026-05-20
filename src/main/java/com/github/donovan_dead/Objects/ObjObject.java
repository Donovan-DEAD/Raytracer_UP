package com.github.donovan_dead.Objects;

import java.util.ArrayList;
import java.awt.Color;

import com.github.donovan_dead.Math.BarycentricCoordinates;
import com.github.donovan_dead.Math.UV;
import com.github.donovan_dead.Math.Utils;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Structures.AABB;
import com.github.donovan_dead.Objects.Structures.BVHNode;
import com.github.donovan_dead.Objects.Structures.Material;
import com.github.donovan_dead.Physics.Intersection;
import com.github.donovan_dead.Physics.Ray;


public class ObjObject extends Object3D {
    
    private static final double EPSILON = 1e-8;
    
    ArrayList<Vector3> vertexList = new ArrayList<>();
    ArrayList<Integer> vertIdxList = new ArrayList<>();

    ArrayList<Vector3> normalList = new ArrayList<>();
    ArrayList<Integer> normalIdxList = new ArrayList<>();

    ArrayList<UV> uvList = new ArrayList<>();
    ArrayList<Integer> uvIdxList = new ArrayList<>();

    ArrayList<Material> materialList = new ArrayList<>();
    ArrayList<Integer> materialIdxList = new ArrayList<>();

    public ArrayList<BVHNode> BVHTree;
    private ArrayList<Integer> auxiliarIdxList;

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

                BarycentricCoordinates b = Utils.calculateBarycentricCoordinates(ray.getPos(t), v0, v1, v2);

                // UV independiente de las normales
                UV triUV0 = null, triUV1 = null, triUV2 = null;
                boolean hasUV = uvIdxList.get(i * 3) != -1 && uvIdxList.get(i * 3 + 1) != -1 && uvIdxList.get(i * 3 + 2) != -1;
                UV uv;
                if (hasUV) {
                    triUV0 = uvList.get(uvIdxList.get(i * 3));
                    triUV1 = uvList.get(uvIdxList.get(i * 3 + 1));
                    triUV2 = uvList.get(uvIdxList.get(i * 3 + 2));
                    uv = new UV(
                        triUV0.getU() * b.alpha() + triUV1.getU() * b.beta() + triUV2.getU() * b.gamma(),
                        triUV0.getV() * b.alpha() + triUV1.getV() * b.beta() + triUV2.getV() * b.gamma()
                    );
                } else {
                    uv = new UV(0.5, 0.5);
                }

                // Normal: interpolar si hay definidas, cross product si no
                Vector3 normal;
                if (normalIdxList.get(i * 3) == -1 || normalIdxList.get(i * 3 + 1) == -1 || normalIdxList.get(i * 3 + 2) == -1) {
                    normal = Utils.crossProduct(edge1, edge2).normalize();
                } else {
                    Vector3 n0 = normalList.get(normalIdxList.get(i * 3));
                    Vector3 n1 = normalList.get(normalIdxList.get(i * 3 + 1));
                    Vector3 n2 = normalList.get(normalIdxList.get(i * 3 + 2));
                    normal = Vector3.builder().X(0).Y(0).Z(0).build()
                        .add(n0.scale(b.alpha()))
                        .add(n1.scale(b.beta()))
                        .add(n2.scale(b.gamma()))
                        .normalize();
                }

                // Make the transformation from the tangent space in the normal map to the world space
                Material mat = materialList.get(materialIdxList.get(i));
                if (hasUV && mat.getNormalTexture() != null) {
                    double dU1 = triUV1.getU() - triUV0.getU();
                    double dV1 = triUV1.getV() - triUV0.getV();
                    double dU2 = triUV2.getU() - triUV0.getU();
                    double dV2 = triUV2.getV() - triUV0.getV();

                    // Determinant for the calculus of the inverse matrix with adjugate matrix
                    double det = dU1 * dV2 - dU2 * dV1;

                    Vector3 T = (Math.abs(det) < 1e-8)
                        ? new Vector3(1, 0, 0)
                        : edge1.scale(dV2).subtract(edge2.scale(dV1)).scale(1.0 / det);

                    // Ensure T is perpendicular with the normal
                    Vector3 T_orth = T.subtract(normal.scale(Utils.dotProduct(normal, T))).normalize();
                    
                    // TBN are ortoganl vectors so we use T and N with the cross product to calculate B
                    Vector3 B = Utils.crossProduct(normal, T_orth);

                    int rgb = mat.getNormalTexture().getPixel(uv);
                    double bm = mat.getBumpMultiplier();
                    Color color = new Color(rgb);
                    double nx = color.getRed()   / 255.0 * 2.0 - 1.0;
                    double ny = color.getGreen() / 255.0 * 2.0 - 1.0;
                    double nz = color.getBlue()  / 255.0 * 2.0 - 1.0;

                    normal = T_orth.scale(nx * bm).add(B.scale(ny * bm)).add(normal.scale(nz)).normalize();
                }

                if (ans == null) {
                    ans = new Intersection(normal, t, mat, uv);
                } else if (ans.t() > t) {
                    ans = new Intersection(normal, t, mat, uv);
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

    public void rotateX(double angleRadians){
        Vector3 centroid = new Vector3(0,0,0);
        
        for(Vector3 vertex : vertexList) centroid = centroid.add(vertex);
        centroid = centroid.scale(1.0 / (double)vertexList.size());
        
        for(int i = 0; i < vertexList.size(); i++){
            vertexList.set(
                i, 
                vertexList.get(i).subtract(centroid).rotateX(angleRadians).add(centroid)
            );
        }
    }

    public void rotateY(double angleRadians){
        Vector3 centroid = new Vector3(0,0,0);
        
        for(Vector3 vertex : vertexList) centroid = centroid.add(vertex);
        centroid = centroid.scale(1.0 / (double)vertexList.size());
        
        for(int i = 0; i < vertexList.size(); i++){
            vertexList.set(
                i, 
                vertexList.get(i).subtract(centroid).rotateY(angleRadians).add(centroid)
            );
        }
    }

    public void rotateZ(double angleRadians){
        Vector3 centroid = new Vector3(0,0,0);
        
        for(Vector3 vertex : vertexList) centroid = centroid.add(vertex);
        centroid = centroid.scale(1.0 / (double)vertexList.size());
        
        for(int i = 0; i < vertexList.size(); i++){
            vertexList.set(
                i, 
                vertexList.get(i).subtract(centroid).rotateZ(angleRadians).add(centroid)
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

        // Arrays for the new positioins of the normals, vertexes, uvs and materials
        ArrayList<Integer> newIndexes = new ArrayList<>();
        ArrayList<Integer> newNormalIndexes = new ArrayList<>();
        ArrayList<Integer> newUvIndexes = new ArrayList<>();
        ArrayList<Integer> newMaterialIndexes = new ArrayList<>();

        for(Integer idx : auxiliarIdxList){
            newIndexes.add(vertIdxList.get(idx * 3));
            newIndexes.add(vertIdxList.get(idx * 3 + 1));
            newIndexes.add(vertIdxList.get(idx * 3 + 2));

            newNormalIndexes.add(normalIdxList.get(idx * 3));
            newNormalIndexes.add(normalIdxList.get(idx * 3 + 1));
            newNormalIndexes.add(normalIdxList.get(idx * 3 + 2));

            newUvIndexes.add(uvIdxList.get(idx * 3));
            newUvIndexes.add(uvIdxList.get(idx * 3 + 1));
            newUvIndexes.add(uvIdxList.get(idx * 3 + 2));

            newMaterialIndexes.add(materialIdxList.get(idx));
        }

        vertIdxList = newIndexes;
        normalIdxList = newNormalIndexes;
        uvIdxList = newUvIndexes;
        materialIdxList = newMaterialIndexes;
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

            Result res = new Result();

            // Sort by X axis
            auxiliarIdxList.subList(start, end).sort((a, b)->{
                Vector3 v0 = getTrianguleCentroid(a);
                Vector3 v1 = getTrianguleCentroid(b);
                return Double.compare(v0.X(), v1.X());
            });
            evaluateSAHSplits(start, end, 0, res);

            // Sort by Y axis
            auxiliarIdxList.subList(start, end).sort((a, b)->{
                Vector3 v0 = getTrianguleCentroid(a);
                Vector3 v1 = getTrianguleCentroid(b);
                return Double.compare(v0.Y(), v1.Y());
            });
            evaluateSAHSplits(start, end, 1, res);

            // Sort by Z axis
            auxiliarIdxList.subList(start, end).sort((a, b)->{
                Vector3 v0 = getTrianguleCentroid(a);
                Vector3 v1 = getTrianguleCentroid(b);
                return Double.compare(v0.Z(), v1.Z());
            });
            evaluateSAHSplits(start, end, 2, res);
            // Sort one final time by the best axis
            if(res.candidateAxis == 0)
                auxiliarIdxList.subList(start, end).sort((a, b)->{
                    Vector3 v0 = getTrianguleCentroid(a);
                    Vector3 v1 = getTrianguleCentroid(b);
                    return Double.compare(v0.X(), v1.X());
                });
            else if (res.candidateAxis == 1)
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

            int leftIdx = recursiveBVHConstruction(start, res.candidateIdx);
            int rightIdx = recursiveBVHConstruction(res.candidateIdx, end);

            node.setLeftChild(leftIdx).setRightChild(rightIdx);

            return nodeIdx;
        }
    }

    private void evaluateSAHSplits(int start, int end, int axis, Result res) {
        int n = end - start;

        // leftPrefix[i] = AABB acumulado de [start, start+i]
        AABB[] leftPrefix = new AABB[n];
        leftPrefix[0] = getTriangleAABB(start);
        for (int i = 1; i < n; i++) {
            leftPrefix[i] = new AABB(leftPrefix[i - 1].min(), leftPrefix[i - 1].max());
            leftPrefix[i].extendToAABB(getTriangleAABB(start + i));
        }

        // rightPrefix[i] = AABB acumulado de [start+i, end)
        AABB[] rightPrefix = new AABB[n];
        rightPrefix[n - 1] = getTriangleAABB(end - 1);
        for (int i = n - 2; i >= 0; i--) {
            rightPrefix[i] = new AABB(rightPrefix[i + 1].min(), rightPrefix[i + 1].max());
            rightPrefix[i].extendToAABB(getTriangleAABB(start + i));
        }

        for (int k = 1; k < n; k++) {
            double costTemp = leftPrefix[k - 1].getSurfaceArea() * k
                            + rightPrefix[k].getSurfaceArea() * (n - k);
            res.compareToOther(costTemp, start + k, axis);
        }
    }

    private AABB getTriangleAABB(int pos) {
        int triIdx = auxiliarIdxList.get(pos);
        Vector3 v0 = vertexList.get(vertIdxList.get(triIdx * 3));
        Vector3 v1 = vertexList.get(vertIdxList.get(triIdx * 3 + 1));
        Vector3 v2 = vertexList.get(vertIdxList.get(triIdx * 3 + 2));
        AABB aabb = new AABB(v0, v0);
        aabb.extendToVertex(v1);
        aabb.extendToVertex(v2);
        return aabb;
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

    public AABB getBox(){
        return BVHTree.get(0).getBox();
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        ArrayList<Vector3> vertex;
        ArrayList<Integer> vertIdxList;

        ArrayList<Vector3> normalList = new ArrayList<>();
        ArrayList<Integer> normalIdxList = new ArrayList<>();

        ArrayList<UV> uvList = new ArrayList<>();
        ArrayList<Integer> uvIdxList = new ArrayList<>();

        ArrayList<Material> materialList = new ArrayList<>();
        ArrayList<Integer> materialIdxList = new ArrayList<>();

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

        public Builder uvList(ArrayList<UV> uvs){
            this.uvList = uvs;
            return this;
        }

        public Builder uvIdxList(ArrayList<Integer> uvIdxs){
            this.uvIdxList = uvIdxs;
            return this;
        }

        public Builder materialList(ArrayList<Material> mtlList){
            this.materialList = mtlList;
            return this;
        }

        public Builder materialIdxList(ArrayList<Integer> mtlIdxList){
            this.materialIdxList = mtlIdxList;
            return this;
        }

        public ObjObject build() {
            ObjObject object = new ObjObject();

            object.vertexList = this.vertex;
            object.vertIdxList = this.vertIdxList;

            object.normalList = this.normalList;
            object.normalIdxList = this.normalIdxList;

            object.uvList = this.uvList;
            object.uvIdxList = this.uvIdxList;

            object.materialList = this.materialList;
            object.materialIdxList = this.materialIdxList;

            return object;
        }
    }

    private static class Result {
        public double candidateCost = Double.MAX_VALUE;
        public int candidateIdx = 0;
        public int candidateAxis = 0;
        
        public void compareToOther(double cost, int idx, int axis){
            if(candidateCost > cost)
            {
                candidateCost = cost;
                candidateIdx = idx;
                candidateAxis = axis;
            }
        }
    }
}
