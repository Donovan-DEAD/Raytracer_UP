package com.github.donovan_dead.Colors;

public record RGBColor (double R, double G, double B) {
    
    public static class Builder {
        private double r, g, b;

        public Builder R(double r) { this.r = Math.min(Math.max(r, 0), 255); return this; }
        public Builder G(double g) { this.g = Math.min(Math.max(g, 0), 255); return this; }
        public Builder B(double b) { this.b = Math.min(Math.max(b, 0), 255); return this; }

        public RGBColor build() {
            return new RGBColor(r, g, b);
        }
    }
}
