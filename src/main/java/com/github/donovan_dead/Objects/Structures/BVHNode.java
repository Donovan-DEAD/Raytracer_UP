package com.github.donovan_dead.Objects.Structures;

import com.github.donovan_dead.Physics.Ray;

public class BVHNode {
    AABB box;
    int start;
    int count;
    int leftChildIdx = -1;
    int rightChildIdx = -1;

    public final static int minLeafSize = 16 / 4;

    public boolean intersectsBox(Ray ray) {
        return this.box.intersectsBox(ray);
    }

    public boolean isLeaf(){
        return count != 0;
    }

    public BVHNode setBox(AABB box){ this.box = box; return this; }

    public BVHNode setStart(int s) { this.start = s; return this; }
    public int getStart(){ return this.start; }

    public BVHNode setCount(int c) { this.count = c; return this; }
    public int getCount(){ return this.count; }

    public BVHNode setLeftChild(int idx) { this.leftChildIdx = idx; return this; }
    public int getLeftChild(){ return this.leftChildIdx; }

    public BVHNode setRightChild(int idx) { this.rightChildIdx = idx; return this; }
    public int getRightChild(){ return this.rightChildIdx; }
}
