package com.github.donovan_dead.Objects.Structures;

import com.github.donovan_dead.Physics.Ray;

/**
 * Represents a node in the Bounding Volume Hierarchy (BVH) tree.
 * Nodes are either internal (with left/right children) or leaf nodes (with primitive count).
 */
public class BVHNode {
    AABB box;
    int start;
    int count;
    int leftChildIdx = -1;
    int rightChildIdx = -1;

    /** Minimum number of primitives per leaf node before subdivision is required */
    public final static int minLeafSize = 16 / 4;

    /**
     * Tests if the given ray intersects this node's bounding box.
     *
     * @param ray the ray to test
     * @return true if the ray intersects the AABB, false otherwise
     */
    public boolean intersectsBox(Ray ray) {
        return this.box.intersectsBox(ray);
    }

    /**
     * Checks if this node is a leaf node (contains primitives).
     *
     * @return true if count > 0 (leaf node), false if internal node
     */
    public boolean isLeaf(){
        return count != 0;
    }

    /**
     * Sets the bounding box for this node.
     *
     * @param box the AABB to set
     * @return this node for method chaining
     */
    public BVHNode setBox(AABB box){ this.box = box; return this; }

    /**
     * Gets the bounding box of this node.
     *
     * @return the AABB
     */
    public AABB getBox(){ return this.box; }

    /**
     * Sets the starting index of primitives in this leaf node.
     *
     * @param s the start index
     * @return this node for method chaining
     */
    public BVHNode setStart(int s) { this.start = s; return this; }

    /**
     * Gets the starting index of primitives in this leaf node.
     *
     * @return the start index
     */
    public int getStart(){ return this.start; }

    /**
     * Sets the number of primitives in this leaf node.
     *
     * @param c the primitive count
     * @return this node for method chaining
     */
    public BVHNode setCount(int c) { this.count = c; return this; }

    /**
     * Gets the number of primitives in this leaf node.
     *
     * @return the primitive count
     */
    public int getCount(){ return this.count; }

    /**
     * Sets the left child node index.
     *
     * @param idx the BVH tree index of the left child
     * @return this node for method chaining
     */
    public BVHNode setLeftChild(int idx) { this.leftChildIdx = idx; return this; }

    /**
     * Gets the left child node index.
     *
     * @return the BVH tree index of the left child
     */
    public int getLeftChild(){ return this.leftChildIdx; }

    /**
     * Sets the right child node index.
     *
     * @param idx the BVH tree index of the right child
     * @return this node for method chaining
     */
    public BVHNode setRightChild(int idx) { this.rightChildIdx = idx; return this; }

    /**
     * Gets the right child node index.
     *
     * @return the BVH tree index of the right child
     */
    public int getRightChild(){ return this.rightChildIdx; }
}
