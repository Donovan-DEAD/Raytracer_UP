package com.github.donovan_dead.Raytracer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.github.donovan_dead.Math.Utils;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Object3D;
import com.github.donovan_dead.Objects.Plane;
import com.github.donovan_dead.Physics.Intersection;
import com.github.donovan_dead.Physics.LightSource;
import com.github.donovan_dead.Physics.Ray;

public class Raytracer {
    private Camera cam;
    private Scene scene;

    public static int width = 1960  /  2;
    public static double aspect_ratio = 16.0 / 9.0;

    public Raytracer(Camera cam, Scene scene) {
        this.cam = cam;
        this.scene = scene;
    }

    public void Render(File outputFile) {
        if(cam == null || scene == null ) return;
        
        BufferedImage img = new BufferedImage(width, (int)(width / aspect_ratio), BufferedImage.TYPE_INT_RGB);
        
        Plane farPlane = cam.getFarPlane();
        farPlane.setColor(scene.background);

        int height = (int)(width / aspect_ratio);
        ArrayList<Thread> threads = new ArrayList<>();

        for (int h = 0; h < height; h++) {
            final int row = h;
            Thread t = Thread.ofVirtual().start(() -> {
                for (int w = 0; w < width; w++) {
                    double u = (double)w / (width - 1);
                    double v = (double)row / (height - 1);

                    Ray r = cam.getRay(u, v);

                    Intersection i = farPlane.calculateIntersection(r);
                    for (Object3D o : scene.getObjects()) {
                        Intersection temp = o.calculateIntersection(r);

                        if (i == null) {
                            i = temp;
                            continue;
                        }

                        if (temp != null) {
                            i = (temp.t() < i.t()) ? temp : i;
                        }
                    }

                    Color color;
                    if (i != null) {
                        Vector3 baseColor = Vector3.builder().X(i.color().R()).Y(i.color().G()).Z(i.color().B()).build();
                        Vector3 finalColor = new Vector3(0, 0, 0);

                        for (LightSource l : scene.getLights()) {
                            Vector3 vecToLight = l.origin().subtract(r.getPos(i.t())).normalize();
                            double resultDot = Math.max(0, Utils.dotProduct(vecToLight, i.normal().normalize()));

                            finalColor = finalColor.add(
                                Vector3.builder()
                                    .X(baseColor.X() * l.lightColor().R() * resultDot)
                                    .Y(baseColor.Y() * l.lightColor().G() * resultDot)
                                    .Z(baseColor.Z() * l.lightColor().B() * resultDot)
                                    .build()
                            );
                        }

                        color = new Color(
                            (int)Math.min(finalColor.X(), 255),
                            (int)Math.min(finalColor.Y(), 255),
                            (int)Math.min(finalColor.Z(), 255)
                        );
                    } else {
                        color = new Color(
                            (int)scene.background.R(),
                            (int)scene.background.G(),
                            (int)scene.background.B()
                        );
                    }

                    synchronized (img) {
                        img.setRGB(w, row, color.getRGB());
                    }
                }
            });
            threads.add(t);
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            ImageIO.write(img, "jpg", outputFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
