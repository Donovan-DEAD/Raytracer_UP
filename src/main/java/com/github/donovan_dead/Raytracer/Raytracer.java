package com.github.donovan_dead.Raytracer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.github.donovan_dead.Math.Utils;
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

    public static int width = 1960 * 8 / 4 ; // default
    public static double aspect_ratio = 16.0 / 9.0;
    
    final static int MAX_DEPTH = 12;
    final static double EPS = 1e-6;
    final static double IOR_OF_AIR = 1.0;

    public static void setWidth(int w) { width = w; }
    public static void setAspectRatio(double ratio) { aspect_ratio = ratio; }

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
                    Vector3 finalColor = launchRay(r, i, 1, 0, IOR_OF_AIR); 

                    Color color = new Color(
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
            ImageIO.write(img, "png", outputFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private Vector3 launchRay(Ray ray, Intersection farplane, double residualEnergy, double depth, double medium_ior){
        
        // If max depth reached only calculate the standard ilumination model
        if (depth == MAX_DEPTH || residualEnergy < 1e-4){
            
            Intersection objectIn = scene.calculateIntersection(ray);
            Intersection i = farplane;
            
            if(objectIn != null) 
                i = (i.t() < objectIn.t()) ? i: objectIn ;

            
            return getLightsContribution(i, ray, residualEnergy);

        } else {
               
            Intersection objectIn = scene.calculateIntersection(ray);
            Intersection i = farplane;

            if(objectIn != null)
                i = (i.t() < objectIn.t()) ? i: objectIn ;

            double shadowInNormal = -Utils.dotProduct(i.normal(), ray.direction());
            boolean hitFromInside = shadowInNormal < 0;
            Vector3 hitNormal = hitFromInside ? i.normal().scale(-1) : i.normal();
            if (hitFromInside) shadowInNormal = -shadowInNormal;

            double reflectionCoeficient =  Math.pow((medium_ior - i.material().getNi() )/(i.material().getNi() + medium_ior), 2) * residualEnergy;
            if (reflectionCoeficient < EPS) reflectionCoeficient = 0;

            double diffuseCoeficient = (1 - reflectionCoeficient) * i.material().getOpacity() * residualEnergy;
            double refractionCoeficient = (1 - reflectionCoeficient) * (1 - i.material().getOpacity()) * residualEnergy;

            Vector3 reflectionColor = Vector3.Zero();
            if (reflectionCoeficient != 0 )
                 reflectionColor = reflectionColor.add(
                    launchRay(
                        new Ray(
                            ray.getPos(i.t()).add(hitNormal.scale(EPS)),
                            ray.direction().subtract(hitNormal.scale(-2 * shadowInNormal)).normalize()
                        ),
                        farplane, reflectionCoeficient, depth + 1, medium_ior)
                    );

            double nextIor = hitFromInside ? IOR_OF_AIR : i.material().getNi();
            double discriminant = 1.0 - Math.pow(medium_ior / nextIor, 2) * (1.0 - Math.pow(shadowInNormal, 2));

            Vector3 refractionColor = Vector3.Zero();
            if(discriminant > 0)
                refractionColor = refractionColor.add(
                    launchRay(
                        new Ray(
                            ray.getPos(i.t()).add(hitNormal.scale(-EPS)),
                            ray.direction()
                                .scale(medium_ior / nextIor)
                                .add(
                                    hitNormal.scale(medium_ior / nextIor * shadowInNormal - Math.sqrt(discriminant))
                                ).normalize()
                        ),
                        farplane, refractionCoeficient, depth + 1, nextIor)
                );
            
            Vector3 diffuseColor = getLightsContribution(i, ray, diffuseCoeficient);

            return diffuseColor.add(reflectionColor).add(refractionColor);
        }
    }

    private Vector3 getLightsContribution(Intersection i,Ray ray, double residualEnergy){

        Vector3 finalColor = new Vector3(0,0,0);

        Vector3 hitPoint  = ray.getPos(i.t());
        Vector3 hitNormal = i.normal();

        for (BaseLightSource l : scene.getLights()) {
            Vector3 lightContribution;

            if (l instanceof LightSource) {
                LightSource light = (LightSource) l;
                lightContribution = hasAnObstacle(light.origin(), hitPoint, hitNormal)
                    ? Vector3.Zero()
                    : l.getLightContribution(hitPoint, hitNormal, i.material(), ray.origin(), i.uv());

            } else if (l instanceof SpotLight) {
                
                SpotLight spotlight = (SpotLight) l;
                lightContribution = hasAnObstacle(spotlight.origin(), hitPoint, hitNormal)
                    ? Vector3.Zero()
                    : l.getLightContribution(hitPoint, hitNormal, i.material(), ray.origin(), i.uv());

            } else {
                lightContribution = l.getLightContribution(hitPoint, hitNormal, i.material(), ray.origin(), i.uv());
            }

            finalColor = finalColor.add(lightContribution);
        }

        return finalColor.scale(residualEnergy);
    }

    private boolean hasAnObstacle(Vector3 lightOrigin, Vector3 hitPoint, Vector3 hitNormal) {
        Ray shadowRay = new Ray(
            hitPoint.add(hitNormal.normalize().scale(1e-6)),
            lightOrigin.subtract(hitPoint).normalize()
        );
        Intersection shadowHit = scene.calculateIntersection(shadowRay);
        double distToLight = lightOrigin.subtract(hitPoint).getMagnitude();
        return shadowHit != null && shadowHit.t() < distToLight;
    }
}
