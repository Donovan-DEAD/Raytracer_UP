package com.github.donovan_dead.Math;

public class UV {
    private double u;
    private double v;

    public UV(double u, double v) {
        this.u = u;
        this.v = v;
    }

    public UV() {
        this(0.0, 0.0);
    }

    public double getU() {
        return u;
    }

    public void setU(double u) {
        this.u = u;
    }

    public double getV() {
        return v;
    }

    public void setV(double v) {
        this.v = v;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private double u = 0.0;
        private double v = 0.0;

        public Builder u(double u) {
            this.u = u;
            return this;
        }

        public Builder v(double v) {
            this.v = v;
            return this;
        }

        public UV build() {
            return new UV(u, v);
        }
    }
}
