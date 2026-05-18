package com.github.donovan_dead.Objects.Structures;

import com.github.donovan_dead.Math.UV;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Texture {
    private int[] pixels;
    private int width;
    private int height;

    public Texture(int[] pixels, int width, int height) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
    }

    public int getPixel(UV uv) {
        return getPixel(uv.getU(), uv.getV());
    }

    public int getPixel(double u, double v) {
        int x = (int) (u * width);
        int y = (int) (v * height);

        x = Math.max(0, Math.min(x, width - 1));
        y = Math.max(0, Math.min(y, height - 1));

        int index = y * width + x;
        return pixels[index];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getPixels() {
        return pixels;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int[] pixels;
        private int width;
        private int height;

        public Builder fromFile(String filePath) throws IOException {
            File file = new File(filePath);
            BufferedImage image = ImageIO.read(file);

            if (image == null) {
                throw new IOException("No se pudo cargar la imagen: " + filePath);
            }

            this.width = image.getWidth();
            this.height = image.getHeight();
            this.pixels = new int[width * height];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    this.pixels[y * width + x] = rgb;
                }
            }

            return this;
        }

        public Builder fromBufferedImage(BufferedImage image) {
            if (image == null) {
                throw new IllegalArgumentException("BufferedImage no puede ser null");
            }

            this.width = image.getWidth();
            this.height = image.getHeight();
            this.pixels = new int[width * height];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    this.pixels[y * width + x] = rgb;
                }
            }

            return this;
        }

        public Builder pixels(int[] pixels, int width, int height) {
            this.pixels = pixels;
            this.width = width;
            this.height = height;
            return this;
        }

        public Texture build() {
            if (pixels == null || width <= 0 || height <= 0) {
                throw new IllegalStateException("Texture no tiene dimensiones válidas o pixels");
            }
            return new Texture(pixels, width, height);
        }
    }
}
