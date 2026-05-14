package com.github.donovan_dead.Raytracer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Plane;
import com.github.donovan_dead.Physics.BaseLightSource;
import com.github.donovan_dead.Physics.Intersection;
import com.github.donovan_dead.Physics.LightSource;
import com.github.donovan_dead.Physics.Ray;
import com.github.donovan_dead.Physics.SpotLight;

public class Raytracer {
    private Camera cam;
    private Scene scene;

    public static int width = 1960 * 4; // 1080 * 4
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
                    Intersection objectIn = scene.calculateIntersection(r);
                    
                    if(objectIn != null) 
                     i = (i.t() < objectIn.t()) ? i: objectIn ;

                    Color color;
                    Vector3 baseColor = Vector3.builder().X(i.color().R()).Y(i.color().G()).Z(i.color().B()).build();
                    Vector3 finalColor = new Vector3(0, 0, 0);

                    Vector3 hitPoint  = r.getPos(i.t());
                    Vector3 hitNormal = i.normal();

                    for (BaseLightSource l : scene.getLights()) {
                        Vector3 lightContribution;

                        if (l instanceof LightSource) {
                            LightSource light = (LightSource) l;
                            lightContribution = hasAnObstacle(light.origin(), hitPoint, hitNormal)
                                ? Vector3.Zero()
                                : l.getLightContribution(hitPoint, hitNormal, baseColor);

                        } else if (l instanceof SpotLight) {
                            SpotLight spotlight = (SpotLight) l;
                            lightContribution = hasAnObstacle(spotlight.origin(), hitPoint, hitNormal)
                                ? Vector3.Zero()
                                : l.getLightContribution(hitPoint, hitNormal, baseColor);

                        } else {
                            lightContribution = l.getLightContribution(hitPoint, hitNormal, baseColor);
                        }

                        finalColor = finalColor.add(lightContribution);
                    }

                    color = new Color(
                        (int)Math.min(finalColor.X(), 255),
                        (int)Math.min(finalColor.Y(), 255),
                        (int)Math.min(finalColor.Z(), 255)
                    );
                    

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

    private boolean hasAnObstacle(Vector3 lightOrigin, Vector3 hitPoint, Vector3 hitNormal) {
        Ray shadowRay = new Ray(
            hitPoint.add(hitNormal.normalize().scale(1e-8)),
            lightOrigin.subtract(hitPoint).normalize()
        );
        Intersection shadowHit = scene.calculateIntersection(shadowRay);
        double distToLight = lightOrigin.subtract(hitPoint).getMagnitude();
        return shadowHit != null && shadowHit.t() < distToLight;
    }
}
