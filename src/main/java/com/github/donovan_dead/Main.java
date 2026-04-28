package com.github.donovan_dead;

import java.io.File;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Sphere;
import com.github.donovan_dead.Objects.Triangule;
import com.github.donovan_dead.Physics.LightSource;
import com.github.donovan_dead.Raytracer.Camera;
import com.github.donovan_dead.Raytracer.Raytracer;
import com.github.donovan_dead.Raytracer.Scene;

public class Main {
    public static void main(String[] args) throws Exception {
        Camera cam = new Camera(Vector3.builder().X(0).Y(0).Z(1).build(), 1.2, 10);

        Scene scene = new Scene();

        scene.addObject(new Sphere(
            Vector3.builder().X(0).Y(0).Z(-3).build(),
            1,
            new RGBColor(100, 100, 100)
        ));
        scene.addObject(new Sphere(
            Vector3.builder().X(-2).Y(0.5).Z(-5).build(),
            1.5,
            new RGBColor(255, 0, 100)
        ));
        scene.addObject(new Sphere(
            Vector3.builder().X(-2).Y(0.5).Z(-10).build(),
            5,
            new RGBColor(255, 0, 255)
        ));
        scene.addObject(new Sphere(
            Vector3.builder().X(5).Y(-0.5).Z(-7).build(),
            1,
            new RGBColor(0, 255, 0)
        ));

        Triangule t1 = new Triangule(
            new Vector3(1, 2, -4),
            new Vector3(3, -1, -4),
            new Vector3(0.5, -1, -4),
            new RGBColor(0, 200, 255)
        );
        t1.translate(new Vector3(5, 1, 10));
        scene.addObject(t1);
        Triangule t2 = new Triangule(
            new Vector3(-0.5, -1, -3.5),
            new Vector3(1, -2.5, -3.5),
            new Vector3(2, -0.5, -3.5),
            new RGBColor(255, 200, 0)
        );
        // t2.translate(new Vector3(0,0,0));
        scene.addObject(t2);

        scene.addLightSource(new LightSource(
            new Vector3(2, 0, -2),
            new RGBColor(1, 1, 1)
        ));
        scene.addLightSource(new LightSource(
            new Vector3(2, 0, -2),
            new RGBColor(0.2, 0.2, 1)
        ));

        // scene.addLightSource(new LightSource(
        //     new Vector3(5, 0, 0),
        //     new RGBColor(1, 1, 1)
        // ));

        Raytracer raytracer = new Raytracer(cam, scene);

        File tempDir = new File("raytracer_output");
        tempDir.delete();
        tempDir.mkdir();
        for(int t = 0; t < 1; t++){
            t2.scale(1.01);
            t2.translate(new Vector3(Math.cos(Math.toRadians(t *20)) ,  1, -Math.sin(Math.toRadians(t *20))));
            File outputFile = new File(tempDir, "render_" + t + ".jpg");
            raytracer.Render(outputFile);
        }

        System.out.println("Rendered to: " + tempDir.getAbsolutePath());
    }
}