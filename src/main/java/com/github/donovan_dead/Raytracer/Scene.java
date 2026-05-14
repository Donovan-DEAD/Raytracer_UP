package com.github.donovan_dead.Raytracer;

import java.util.ArrayList;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Objects.Object3D;
import com.github.donovan_dead.Objects.Structures.AABB;
import com.github.donovan_dead.Objects.Structures.BVHNode;
import com.github.donovan_dead.Physics.BaseLightSource;
import com.github.donovan_dead.Physics.Intersection;
import com.github.donovan_dead.Physics.Ray;

public class Scene {
    protected ArrayList < Object3D > objects = new ArrayList<>();
    protected ArrayList < Integer > objectsIdx = new ArrayList<>();

    protected ArrayList < BaseLightSource > lights = new ArrayList<>();

    public ArrayList<BVHNode> BVHTree;

    public RGBColor background = new RGBColor(0,0,0);

    public void addObject(Object3D o){
        objects.add(o);
    }

    public void addLightSource(BaseLightSource l){
        lights.add(l);
    }

    public ArrayList< Object3D > getObjects(){
        return this.objects;
    }

    public ArrayList<BaseLightSource> getLights(){
        return this.lights;
    }

    public Intersection calculateIntersection(Ray ray){
        // System.out.println(BVHTree.isEmpty());
        if(BVHTree.isEmpty()) return null;
        return recursiveIntersection(ray, 0);
    }

    private Intersection recursiveIntersection(Ray ray, int nodeIdx){
        BVHNode node = BVHTree.get(nodeIdx);
        if(node.isLeaf()){
            Intersection ans = null;
            Intersection candidate = null;

            for(int i = node.getStart(); i < node.getStart() + node.getCount(); i++){
                
                candidate = objects.get(i).calculateIntersection(ray);
                if(candidate == null) continue;
                if (ans == null){
                    ans = candidate;
                    continue;
                } else {
                    ans = (candidate.t() < ans.t() ) ? candidate : ans; 
                }                
            }
            
            return ans;
        } else {
            if (node.intersectsBox(ray)){ 
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

    public void constructBVH(){
        
        objectsIdx = new ArrayList<>(objects.size());
        for(int i = 0; i < objects.size(); i++){
            objectsIdx.add(i);
        }

        BVHTree = new ArrayList<>();
        recursiveBVHConstruction(0, objects.size());

        ArrayList< Object3D > newObjectList = new ArrayList<>();

        for (Integer i : objectsIdx){
            newObjectList.add(
                objects.get(i)
            );
        }

        objects = newObjectList;
    }

    private int recursiveBVHConstruction(int start, int end){
        BVHNode node = new BVHNode();
        int nodeIdx = BVHTree.size();
        BVHTree.add(node);

        if(end - start <=  BVHNode.minLeafSize){
            
            node.setCount(end-start)
                .setStart(start)
                .setBox(constructAABB(start, end));

        } else {

            node.setCount(0)
                .setStart(0)
                .setBox(constructAABB(start, end));


            Result res = new Result();

            // Sort by X axis
            objectsIdx.subList(start, end).sort((a, b) ->
                Double.compare(
                    objects.get(a).getBox().getCentroid().X(),
                    objects.get(b).getBox().getCentroid().X()
                )
            );
            evaluateSAHSplits(start, end, 0, res);

            // Sort by Y axis
            objectsIdx.subList(start, end).sort((a, b) ->
                Double.compare(
                    objects.get(a).getBox().getCentroid().Y(),
                    objects.get(b).getBox().getCentroid().Y()
                )
            );
            evaluateSAHSplits(start, end, 1, res);

            // Sort by Z axis
            objectsIdx.subList(start, end).sort((a, b) ->
                Double.compare(
                    objects.get(a).getBox().getCentroid().Z(),
                    objects.get(b).getBox().getCentroid().Z()
                )
            );
            evaluateSAHSplits(start, end, 2, res);

            // Sort one final time by the best axis
            if(res.candidateAxis == 0)
                objectsIdx.subList(start, end).sort((a, b) ->
                    Double.compare(
                        objects.get(a).getBox().getCentroid().X(),
                        objects.get(b).getBox().getCentroid().X()
                    )
                );
            else if (res.candidateAxis == 1)
                objectsIdx.subList(start, end).sort((a, b) ->
                    Double.compare(
                        objects.get(a).getBox().getCentroid().Y(),
                        objects.get(b).getBox().getCentroid().Y()
                    )
                );
            else
                objectsIdx.subList(start, end).sort((a, b) ->
                    Double.compare(
                        objects.get(a).getBox().getCentroid().Z(),
                        objects.get(b).getBox().getCentroid().Z()
                    )
                );

            int leftIdx = recursiveBVHConstruction(start, res.candidateIdx);
            int rightIdx = recursiveBVHConstruction(res.candidateIdx, end);

            node.setLeftChild(leftIdx).setRightChild(rightIdx);


        }

        return nodeIdx;
    }

    private void evaluateSAHSplits(int start, int end, int axis, Result res) {
        int n = end - start;

        AABB[] leftPrefix = new AABB[n];
        AABB first = objects.get(objectsIdx.get(start)).getBox();
        leftPrefix[0] = new AABB(first.min(), first.max());
        for (int i = 1; i < n; i++) {
            leftPrefix[i] = new AABB(leftPrefix[i - 1].min(), leftPrefix[i - 1].max());
            leftPrefix[i].extendToAABB(objects.get(objectsIdx.get(start + i)).getBox());
        }

        AABB[] rightPrefix = new AABB[n];
        AABB last = objects.get(objectsIdx.get(end - 1)).getBox();
        rightPrefix[n - 1] = new AABB(last.min(), last.max());
        for (int i = n - 2; i >= 0; i--) {
            rightPrefix[i] = new AABB(rightPrefix[i + 1].min(), rightPrefix[i + 1].max());
            rightPrefix[i].extendToAABB(objects.get(objectsIdx.get(start + i)).getBox());
        }

        for (int k = 1; k < n; k++) {
            double costTemp = leftPrefix[k - 1].getSurfaceArea() * k
                            + rightPrefix[k].getSurfaceArea() * (n - k);
            res.compareToOther(costTemp, start + k, axis);
        }
    }

    private AABB constructAABB(int start, int end){
        AABB aabb = objects.get(
            objectsIdx.get(start)
        ).getBox();

        AABB groupBox = new AABB(aabb.min(), aabb.max());

        for(int i = start; i < end; i++){
            groupBox.extendToAABB(
                objects.get(
                    objectsIdx.get(i)
                ).getBox()
            );
        }

        return groupBox;
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
