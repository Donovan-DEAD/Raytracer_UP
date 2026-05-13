package com.github.donovan_dead;

import java.io.File;
import java.util.ArrayList;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.ObjObject;
import com.github.donovan_dead.Objects.Plane;
import com.github.donovan_dead.Physics.BaseLightSource;
import com.github.donovan_dead.Physics.LightSource;
import com.github.donovan_dead.Raytracer.Camera;
import com.github.donovan_dead.Raytracer.ObjReader;
import com.github.donovan_dead.Raytracer.Raytracer;
import com.github.donovan_dead.Raytracer.Scene;

public class Main {

    public static ArrayList<ObjObject> readObjectsFromDir(String dir){
        File directory = new File(dir);

        if(!directory.exists() || !directory.isDirectory()) return null;

        ArrayList<ObjObject> list = new ArrayList<>();

        for(File file : directory.listFiles()){
            try {
                if (file.isFile() && file.getName().endsWith(".obj")){
                    System.out.println("Reading file...");
                    list.add(ObjReader.ReadObjectFile(file));
                    System.out.println("Finishing reading the file.");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.println("Files readed");

        int count = 0;
        for(ObjObject obj : list){
            if(count == 1){
                obj.translate(
                    new Vector3(4, 0, 0)
                );
                
                obj.scale(1.1);
            }
            else if(count == 0){
                obj.translate(
                    new Vector3(-4, 0, 0)
                );

                obj.scale(1.1);
            } else {
                
                obj.translate(
                    new Vector3(180, -80, -170)
                );

                obj.scale(10);
            }

            obj.constructBVH();
            System.out.println("Object BVH finished with " + obj.BVHTree.size() + " nodes.");
            count++;
        }

        System.out.println("BVH of objects finished");
        return list;
    }
    public static void main(String[] args) throws Exception {
        Camera cam = new Camera(Vector3.builder().X(0).Y(0).Z(0).build(), 1, 1000);
        cam.translate(new Vector3(0, 9, 10));
        cam.rotateZ(Math.toRadians(180));
        cam.rotateX(Math.toRadians(-40));

        Scene scene = new Scene();
        
        ArrayList<ObjObject> objList = readObjectsFromDir("objects");
        
        if(objList == null) throw new Exception("No objects where found");
        for(ObjObject obj : objList) scene.addObject(obj);

        scene.addObject(
            new Plane(
                new Vector3( 0, 1,0 ), 
                new Vector3(0, -150, 0), 
                new RGBColor(255, 255, 0)
            )
        );

        System.out.println("Top level BVH started");
        scene.constructBVH();
        System.out.println("Top level BVH finished");

        scene.addLightSource(new LightSource(
            new Vector3(-30, 50, -3),
            new RGBColor(1, 1, 1),
            1
        ));

        // scene.addLightSource(new LightSource(
        //     new Vector3(0, -10, 15),
        //     new RGBColor(0.5, 0, 0.5),
        //     0.5
        // ));

        Raytracer raytracer = new Raytracer(cam, scene);

        File tempDir = new File("raytracer_output");
        tempDir.delete();
        tempDir.mkdir();

        double dt  = 1;
        int count = 0;
        ArrayList<BaseLightSource> lights = scene.getLights();
        
        // for(double t = 0; t < 120 * 3; t+=dt){
        for(double t = 0; t < 1; t+=dt){
            long start = System.nanoTime();

            if (lights.get(0) instanceof LightSource) {
                LightSource light = (LightSource) lights.get(0);
                lights.set(0,
                    new LightSource(
                        light.origin().add(new Vector3(dt * 1.5, 0, 0)),
                        light.lightColor(),
                        light.intensity()
                    )
                );
            }

            // cam.translate(cam.center.scale(-1));
            // cam.translate(new Vector3(
            //     0,
            //     2 * Math.sin(Math.toRadians(t)) + 9,
            //     10
            // ));

            // cam.rotateY(Math.toRadians(dt));

            File outputFile = new File(tempDir, "render_" + count + ".jpg");
            raytracer.Render(outputFile);
            count++;

            long end = System.nanoTime();
            long elapsed = end - start;
            System.out.println("Tiempo: " + (elapsed / 1_000_000.0) + " ms to render " + count);
        }

        System.out.println("Rendered to: " + tempDir.getAbsolutePath());
    }
}