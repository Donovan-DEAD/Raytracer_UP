package com.github.donovan_dead;

import java.io.File;
import java.util.ArrayList;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.ObjObject;
import com.github.donovan_dead.Objects.Plane;
import com.github.donovan_dead.Objects.Structures.Material;
import com.github.donovan_dead.Physics.areaLights.CircleAreaLight;
import com.github.donovan_dead.Raytracer.Camera;
import com.github.donovan_dead.Raytracer.ObjReader;
import com.github.donovan_dead.Raytracer.Raytracer;
import com.github.donovan_dead.Raytracer.Scene;

/**
 * Main entry point for the raytracer application.
 * Handles scene setup, object loading, light configuration, and rendering.
 */
public class Main {

    /**
     * Loads all OBJ files from a directory and applies transformations and BVH construction.
     * Constructs BVH trees in parallel using virtual threads for performance.
     *
     * @param dir the directory path containing OBJ files
     * @return an ArrayList of loaded and processed ObjObject instances, or null if directory doesn't exist
     */
    public static ArrayList<ObjObject> readObjectsFromDir(String dir){
        File directory = new File(dir);

        if(!directory.exists() || !directory.isDirectory()) return null;

        ArrayList<ObjObject> list = new ArrayList<>();

        for(File file : directory.listFiles()){
            try {
                if (file.isFile() && file.getName().endsWith(".obj")){
                    System.out.println("[OBJ] Reading: " + file.getName());
                    list.add(ObjReader.ReadObjectFile(file));
                    System.out.println("[OBJ] Done: " + file.getName());
                }
            } catch (Exception e) {
                System.out.println("[OBJ] Error: " + e.getMessage());
            }
        }

        System.out.println("[OBJ] All files loaded");

        ArrayList<Thread> bvhThreads = new ArrayList<>();
        int count = 0;
        for(ObjObject obj : list){
            if(count == 1){
                obj.translate(
                    new Vector3(8, 0, 3)
                );
                obj.scale(1.3);
            }
            else if(count == 0){
                obj.translate(
                    new Vector3(-10, 5, 6)
                );

                obj.rotateX(Math.toRadians(0));

                obj.scale(1.1);
            } else if( count == 2) {

                obj.translate(
                    new Vector3(20, 0, -5)
                );
                obj.rotateY(Math.toRadians(0));
                obj.scale(120);
            }else {
                obj.translate(new Vector3(5 , 2, -19));
                obj.scale(100);
                obj.rotateX(Math.toRadians(55));
            }

            Thread t = Thread.ofVirtual().start(() -> {
                obj.constructBVH();
                System.out.println("[BVH] Object BVH built: " + obj.BVHTree.size() + " nodes.");
            });
            bvhThreads.add(t);
            count++;
        }

        for(Thread t : bvhThreads){
            try {
                t.join();
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("[BVH] All objects BVH finished");
        return list;
    }
    /**
     * Main method that initializes the raytracer, loads scenes, configures lighting, and renders frames.
     * Sets up a camera with depth of field, creates scene geometry (OBJ objects and planes),
     * adds area lights, and renders to PNG files in the "raytracer_output" directory.
     *
     * @param args command-line arguments (currently unused)
     * @throws Exception if object loading or rendering fails
     */
    public static void main(String[] args) throws Exception {
        Camera cam = new Camera(Vector3.builder().X(0).Y(0).Z(0).build(), 1, 1000, 0.2, 23);
        cam.translate(new Vector3(0, 9, 10));
        cam.rotateZ(Math.toRadians(180));
        cam.rotateX(Math.toRadians(-25));

        Scene scene = new Scene();
        
        ArrayList<ObjObject> objList = readObjectsFromDir("objects");
        
        if(objList == null) throw new Exception("No objects where found");
        for(ObjObject obj : objList) scene.addObject(obj);

        Material.builder().clean();
        scene.addObject(
            new Plane(
                new Vector3( 0, 1,0 ),
                new Vector3(0, -10, 0),
                Material
                    .builder()
                    .Ka(new Vector3(0.02, 0.02, 0.02))
                    .Kd(new Vector3(0.02, 0.02, 0.02))
                    .opacity(1.0)
                    .Ni(100.0)
                    .build()
            )
        );

        Material.builder().clean();
        scene.addObject(
            new Plane(
                new Vector3( 0, 0,1 ),
                new Vector3(0, 0, -100),
                Material
                    .builder()
                    .Ka(new Vector3(0.02, 0.02, 0.02))
                    .Kd(new Vector3(0.9, 0.9, 0.2))
                    .Ks(new Vector3(1.0,  1.0,  1.0 ))
                    .Ns(1000.0)
                    .opacity(1.0)
                    .Ni(1.0)
                    .build()
            )
        );

        Material.builder().clean();
        scene.addObject(
            new Plane(
                new Vector3( 0, -1,0 ),
                new Vector3(0, 100, 0),
                Material
                    .builder()
                    .Ka(new Vector3(0.02, 0.02, 0.02))
                    .Kd(new Vector3(0.9, 0.9, 0.1))
                    .Ks(new Vector3(1.0,  1.0,  1.0 ))
                    .Ns(1000.0)
                    .opacity(1.0)
                    .Ni(1.0)
                    .build()
            )
        );

        System.out.println("[BVH] Top-level scene BVH started");
        scene.constructBVH();
        System.out.println("[BVH] Top-level scene BVH finished");

        scene.addLightSource(new CircleAreaLight(
            new Vector3(0, 5, 32),
           1.75,
            new RGBColor(255, 255, 255),
            1400
        ));
        
        // scene.addLightSource(new LightSource(
        //     new Vector3(0, 5, 32),
        //     new RGBColor(255, 255, 255),
        //     1400
        // ));

        CircleAreaLight cl = new CircleAreaLight(
            new Vector3(0, 45, -15),
           2,
            new RGBColor(255, 255, 255),
            9000
        );
        cl.rotateX(Math.toRadians(180));
        scene.addLightSource(cl);
        
        // scene.addLightSource(new LightSource(
        //     new Vector3(0, 45, -15),
        //     new RGBColor(255, 255, 255),
        //     9000
        // ));


        // scene.addLightSource(new SpotLight(
        //     new Vector3(0, 50, -15),
        //     new Vector3(0, -1, 0),
        //     new RGBColor(255, 255, 240),
        //     8000,
        //     Math.toRadians(45),
        //     Math.toRadians(65)
        // ));

        Raytracer raytracer = new Raytracer(cam, scene);

        File tempDir = new File("raytracer_output");
        tempDir.delete();
        tempDir.mkdir();

        double dt  = 1;
        int count = 0;

        // for(double t = 0; t < 120 * 6; t+=dt){
        for(double t = 0; t < 1; t+=dt){
            long start = System.nanoTime();

            if(t > 360){
            Vector3 orgCenter = new Vector3(0, 9 , 10);
            cam.translate(cam.center.scale(-1));
            cam.translate(new Vector3(
                    10 * Math.cos(Math.toRadians(t)),
                    9,
                    10 * Math.sin(Math.toRadians(t))
                ).add(orgCenter)
            );

            cam.rotateY(Math.toRadians(dt));
            }

            File outputFile = new File(tempDir, "render_" + count + ".png");
            raytracer.Render(outputFile);
            count++;

            long end = System.nanoTime();
            long elapsed = end - start;
            System.out.println("[Render] Frame " + count + " — " + (elapsed / 1_000_000.0) + " ms");
        }

        System.out.println("[Render] Output: " + tempDir.getAbsolutePath());
    }
}