package com.github.donovan_dead.Objects.Structures;

import com.github.donovan_dead.Math.UV;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Represents a 2D texture image with support for UV-coordinate sampling.
 * Stores pixel data as ARGB integers and provides bilinear-style clamped lookups.
 */
public class Texture {
    private int[] pixels;
    private int width;
    private int height;

    /**
     * Constructs a Texture with the given pixel data and dimensions.
     *
     * @param pixels array of ARGB pixel values (length = width * height)
     * @param width texture width in pixels
     * @param height texture height in pixels
     */
    public Texture(int[] pixels, int width, int height) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
    }

    /**
     * Retrieves the pixel color at the given UV coordinate.
     *
     * @param uv the texture coordinate
     * @return the ARGB color value at the clamped UV position
     */
    public int getPixel(UV uv) {
        return getPixel(uv.getU(), uv.getV());
    }

    /**
     * Retrieves the pixel color at the given UV coordinates with clamping.
     * UV coordinates are clamped to [0, 1] range and mapped to pixel indices.
     *
     * @param u the horizontal texture coordinate [0, 1]
     * @param v the vertical texture coordinate [0, 1]
     * @return the ARGB color value at the clamped position
     */
    public int getPixel(double u, double v) {
        int x = (int) (u * width);
        int y = (int) ((1.0 - v) * height);

        x = Math.max(0, Math.min(x, width - 1));
        y = Math.max(0, Math.min(y, height - 1));

        int index = y * width + x;
        return pixels[index];
    }

    /**
     * Gets the texture width in pixels.
     *
     * @return texture width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the texture height in pixels.
     *
     * @return texture height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the raw pixel array.
     *
     * @return ARGB pixel array
     */
    public int[] getPixels() {
        return pixels;
    }

    /**
     * Creates a new Texture builder.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for constructing Texture instances from files or BufferedImage objects.
     */
    public static class Builder {
        private int[] pixels;
        private int width;
        private int height;

        /**
         * Loads texture from a PNG/JPG file.
         *
         * @param filePath path to the image file
         * @return this builder for chaining
         * @throws IOException if file cannot be read
         */
        public Builder fromFile(String filePath) throws IOException {
            File file = new File(filePath);
            System.out.println("[Texture] " + (file.exists() ? "FOUND" : "NOT FOUND") + ": " + file.getAbsolutePath());
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

        /**
         * Loads texture from a BufferedImage.
         *
         * @param image the BufferedImage source
         * @return this builder for chaining
         * @throws IllegalArgumentException if image is null
         */
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

        /**
         * Sets the pixel data and dimensions directly.
         *
         * @param pixels array of ARGB pixel values
         * @param width texture width
         * @param height texture height
         * @return this builder for chaining
         */
        public Builder pixels(int[] pixels, int width, int height) {
            this.pixels = pixels;
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * Builds and returns a Texture instance.
         *
         * @return a new Texture with the configured pixel data
         * @throws IllegalStateException if dimensions or pixels are invalid
         */
        public Texture build() {
            if (pixels == null || width <= 0 || height <= 0) {
                throw new IllegalStateException("Texture no tiene dimensiones válidas o pixels");
            }
            return new Texture(pixels, width, height);
        }
    }
}
