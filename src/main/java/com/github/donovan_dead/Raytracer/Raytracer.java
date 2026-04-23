package com.github.donovan_dead.Raytracer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Utils;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Object3D;
import com.github.donovan_dead.Objects.Sphere;
import com.github.donovan_dead.Physics.Intersection;
import com.github.donovan_dead.Physics.LightSource;
import com.github.donovan_dead.Physics.Ray;

public class Raytracer {
    public static Camera cam;
    public static Scene scene;

    public static int width = 1960 * 4;
    public static double aspect_ratio = 16.0/9.0; // width / height
    public static BufferedImage img;

    public static void Run(){
        Raytracer.InitializeCamera();
        Raytracer.InitializeScene();
        Raytracer.Render();
    }

    public static void InitializeCamera(){
        Raytracer.cam = new Camera( Vector3.builder().X(0).Y(0).Z(1).build(), 1.2);
    }

    public static void InitializeScene(){
        Raytracer.scene = new Scene();

        scene.addObject(
           new Sphere(
            Vector3.builder().X(0).Y(0).Z(-3).build(),
            1,
            new RGBColor(100,100,100)
           )
        );

        scene.addObject(
           new Sphere(
            Vector3.builder().X(-2).Y(0.5).Z(-5).build(),
            1.5,
            new RGBColor(255,0,100)
           )
        );

        scene.addObject(
           new Sphere(
            Vector3.builder().X(-2).Y(0.5).Z(-10).build(),
            5,
            new RGBColor(255,0,255)
           )
        );

        scene.addObject(
           new Sphere(
            Vector3.builder().X(2.5).Y(-0.5).Z(-7).build(),
            1,
            new RGBColor(0,255,0)
           )
        );
        
        scene.addLightSource(
            new LightSource(
                new Vector3(2,0,-2),
                new RGBColor(1, 1, 1)
            )
        );

        scene.addLightSource(
            new LightSource(
                new Vector3(2,0,-2),
                new RGBColor(0.2, 0.2, 1)
            )
        );

    }

    public static void Render(){
        Raytracer.img = new BufferedImage(width, (int)(width / aspect_ratio), BufferedImage.TYPE_INT_RGB);

        int height = (int)(width / aspect_ratio);
        for(int h=0 ; h < height ; h++){
            for(int w=0; w < width; w++){
                double u = (double)w / (width - 1);
                double v = (double)h / (height - 1);

                Ray r = cam.getRay(u, v);


                Intersection i = null;    
                for(Object3D o : scene.getObjects()){
                    Intersection temp = o.calculateIntersection(r);

                    if(i == null){
                        i = temp;
                        continue;
                    }

                    if(temp != null){
                        i = (temp.t() < i.t()) ? temp : i;
                    }
                }

                if(i != null){
                    Vector3 baseColor = Vector3.builder().X(i.color().R()).Y(i.color().G()).Z(i.color().B()).build();
                    Vector3 finalColor = new Vector3(0,0,0);

                    for(LightSource l :  scene.getLights()){
                        
                        Vector3 vecToLight = l.origin().subtract(r.getPos(i.t())).normalize();
                        Double resultDot = Math.max(0, Utils.dotProduct(vecToLight, i.normal().normalize()));
                        
                        finalColor = finalColor.add(
                            Vector3.builder()
                            .X(baseColor.X() * l.lightColor().R() * resultDot)
                            .Y(baseColor.Y() * l.lightColor().G() * resultDot)
                            .Z(baseColor.Z() * l.lightColor().B() * resultDot)
                            .build()
                        );
                    }

                    Color color = new Color(
                        (int)Math.min(finalColor.X(), 255),
                        (int)Math.min(finalColor.Y(), 255),
                        (int)Math.min(finalColor.Z(), 255)
                    );

                    img.setRGB(w, h, color.getRGB());
                } else {
                    Color color = new Color(
                        (int)scene.background.R(),
                        (int)scene.background.G(),
                        (int)scene.background.B()
                    );
                    img.setRGB(w, h, color.getRGB());
                }
            }
        }

        try { 
            ImageIO.write(img, "jpg", new File("base2.jpg"));   
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
