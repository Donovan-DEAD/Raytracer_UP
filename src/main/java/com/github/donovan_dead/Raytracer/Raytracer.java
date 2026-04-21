package com.github.donovan_dead.Raytracer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Object3D;
import com.github.donovan_dead.Objects.Sphere;
import com.github.donovan_dead.Physics.Intersection;
import com.github.donovan_dead.Physics.Ray;

public class Raytracer {
    public static Camera cam;
    public static Scene scene;

    public static int width = 1960;
    public static double aspect_ratio = 16.0/9.0; // width / height
    public static BufferedImage img;

    public static void Run(){
        Raytracer.InitializeCamera();
        Raytracer.InitializeScene();
        Raytracer.Render();
    }

    public static void InitializeCamera(){
        Raytracer.cam = new Camera( Vector3.builder().X(0).Y(0).Z(0).build(), 1);
        // Raytracer.cam.rotateZ(Math.toRadians(315));
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
            Vector3.builder().X(2.5).Y(-0.5).Z(-7).build(),
            1,
            new RGBColor(0,255,0)
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
                    Color color = new Color(
                        (int)i.color().R(),
                        (int)i.color().G(),
                        (int)i.color().B()
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
            ImageIO.write(img, "jpg", new File("temp.jpg"));   
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
