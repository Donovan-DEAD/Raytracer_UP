package com.github.donovan_dead.Objects.Structures;

import com.github.donovan_dead.Math.Vector3;
import java.io.IOException;

public class Material {

    private static final Material DEFAULT_MATERIAL = new Material();
    private static final Material BLACK_MATERIAL = new Material();

    static {
        BLACK_MATERIAL.Ka = new Vector3(0.0, 0.0, 0.0);
        BLACK_MATERIAL.Kd = new Vector3(0.0, 0.0, 0.0);
        BLACK_MATERIAL.Ks = new Vector3(0.0, 0.0, 0.0);
        BLACK_MATERIAL.Ns = 0.0;
    }

    private Texture ambientTexture;
    private Texture diffuseTexture;
    private Texture specularTexture;
    private Texture normalTexture;
    private Texture nsTexture;

    private double opacity;

    private Vector3 Ka;
    private Vector3 Kd;
    private Vector3 Ks;
    private double Ns;
    private double Ni;

    public Material() {
        this.Ka = new Vector3(0.05, 0.05, 0.05);
        this.Kd = new Vector3(0.4, 0.4, 0.4);
        this.Ks = new Vector3(0.9, 0.9, 0.9);
        this.Ns = 96.0;
        this.Ni = 1.0;
        this.opacity = 1.0;
    }

    public static Material getBlackMaterial() {
        return BLACK_MATERIAL;
    }

    public static Material getDefaultMaterial() {
        return DEFAULT_MATERIAL;
    }

    public Texture getAmbientTexture() {
        return ambientTexture;
    }

    public void setAmbientTexture(Texture ambientTexture) {
        this.ambientTexture = ambientTexture;
    }

    public Texture getDiffuseTexture() {
        return diffuseTexture;
    }

    public void setDiffuseTexture(Texture diffuseTexture) {
        this.diffuseTexture = diffuseTexture;
    }

    public Texture getSpecularTexture() {
        return specularTexture;
    }

    public void setSpecularTexture(Texture specularTexture) {
        this.specularTexture = specularTexture;
    }

    public Texture getNormalTexture() {
        return normalTexture;
    }

    public void setNormalTexture(Texture normalTexture) {
        this.normalTexture = normalTexture;
    }

    public Texture getNsTexture() {
        return nsTexture;
    }

    public void setNsTexture(Texture nsTexture) {
        this.nsTexture = nsTexture;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public Vector3 getKa() {
        return Ka;
    }

    public void setKa(Vector3 Ka) {
        this.Ka = Ka;
    }

    public Vector3 getKd() {
        return Kd;
    }

    public void setKd(Vector3 Kd) {
        this.Kd = Kd;
    }

    public Vector3 getKs() {
        return Ks;
    }

    public void setKs(Vector3 Ks) {
        this.Ks = Ks;
    }

    public double getNs() {
        return Ns;
    }

    public void setNs(double Ns) {
        this.Ns = Ns;
    }

    public double getNi() {
        return Ni;
    }

    public void setNi(double Ni) {
        this.Ni = Ni;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Texture ambientTexture;
        private Texture diffuseTexture;
        private Texture specularTexture;
        private Texture normalTexture;
        private Texture nsTexture;
        private double opacity = 1.0;
        private Vector3 Ka = new Vector3(0.2, 0.2, 0.2);
        private Vector3 Kd = new Vector3(0.8, 0.8, 0.8);
        private Vector3 Ks = new Vector3(1.0, 1.0, 1.0);
        private double Ns = 32.0;
        private double Ni = 1.0;

        public Builder fromVector(Vector3 color) {
            this.Ka = color.scale(0.2);
            this.Kd = color;
            this.Ks = new Vector3(0.0, 0.0, 0.0);
            this.Ns = 32.0;
            this.opacity = 1.0;
            return this;
        }

        public Builder ambientTexture(String filePath) throws IOException {
            this.ambientTexture = Texture.builder().fromFile(filePath).build();
            return this;
        }

        public Builder ambientTexture(Texture texture) {
            this.ambientTexture = texture;
            return this;
        }

        public Builder diffuseTexture(String filePath) throws IOException {
            this.diffuseTexture = Texture.builder().fromFile(filePath).build();
            return this;
        }

        public Builder diffuseTexture(Texture texture) {
            this.diffuseTexture = texture;
            return this;
        }

        public Builder specularTexture(String filePath) throws IOException {
            this.specularTexture = Texture.builder().fromFile(filePath).build();
            return this;
        }

        public Builder specularTexture(Texture texture) {
            this.specularTexture = texture;
            return this;
        }

        public Builder normalTexture(String filePath) throws IOException {
            this.normalTexture = Texture.builder().fromFile(filePath).build();
            return this;
        }

        public Builder normalTexture(Texture texture) {
            this.normalTexture = texture;
            return this;
        }

        public Builder nsTexture(String filePath) throws IOException {
            this.nsTexture = Texture.builder().fromFile(filePath).build();
            return this;
        }

        public Builder nsTexture(Texture texture) {
            this.nsTexture = texture;
            return this;
        }

        public Builder opacity(double opacity) {
            this.opacity = opacity;
            return this;
        }

        public Builder Ka(Vector3 Ka) {
            this.Ka = Ka;
            return this;
        }

        public Builder Kd(Vector3 Kd) {
            this.Kd = Kd;
            return this;
        }

        public Builder Ks(Vector3 Ks) {
            this.Ks = Ks;
            return this;
        }

        public Builder Ns(double Ns) {
            this.Ns = Ns;
            return this;
        }

        public Builder Ni(double Ni) {
            this.Ni = Ni;
            return this;
        }

        public Builder clean() {
            this.ambientTexture = null;
            this.diffuseTexture = null;
            this.specularTexture = null;
            this.normalTexture = null;
            this.nsTexture = null;
            this.opacity = 1.0;
            this.Ka = new Vector3(0.2, 0.2, 0.2);
            this.Kd = new Vector3(0.8, 0.8, 0.8);
            this.Ks = new Vector3(1.0, 1.0, 1.0);
            this.Ns = 32.0;
            this.Ni = 1.0;
            return this;
        }

        public Material build() {
            Material material = new Material();
            material.setAmbientTexture(this.ambientTexture);
            material.setDiffuseTexture(this.diffuseTexture);
            material.setSpecularTexture(this.specularTexture);
            material.setNormalTexture(this.normalTexture);
            material.setNsTexture(this.nsTexture);
            material.setOpacity(this.opacity);
            material.setKa(this.Ka);
            material.setKd(this.Kd);
            material.setKs(this.Ks);
            material.setNs(this.Ns);
            material.setNi(this.Ni);
            return material;
        }
    }
}
