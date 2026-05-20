package com.github.donovan_dead;

import java.io.File;
import java.util.ArrayList;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.ObjObject;
import com.github.donovan_dead.Objects.Plane;
import com.github.donovan_dead.Objects.Structures.Material;
import com.github.donovan_dead.Physics.BaseLightSource;
import com.github.donovan_dead.Physics.LightSource;
import com.github.donovan_dead.Physics.SpotLight;
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
                    System.out.println("[OBJ] Reading: " + file.getName());
                    list.add(ObjReader.ReadObjectFile(file));
                    System.out.println("[OBJ] Done: " + file.getName());
                }
            } catch (Exception e) {
                System.out.println("[OBJ] Error: " + e.getMessage());
            }
        }

        System.out.println("[OBJ] All files loaded");

        int count = 0;
        for(ObjObject obj : list){
            if(count == 1){
                obj.translate(
                    new Vector3(6, 0, 0)
                );
                obj.scale(1.1);
            }
            else if(count == 0){
                obj.translate(
                    new Vector3(-10, 3, 0)
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
                obj.translate(new Vector3(0 , 0, -3));
                obj.scale(50);
            }

            obj.constructBVH();
            System.out.println("[BVH] Object BVH built: " + obj.BVHTree.size() + " nodes.");
            count++;
        }

        System.out.println("[BVH] All objects BVH finished");
        return list;
    }
    public static void main(String[] args) throws Exception {
        Camera cam = new Camera(Vector3.builder().X(0).Y(0).Z(0).build(), 1, 1000);
        cam.translate(new Vector3(0, 12, 10));
        cam.rotateZ(Math.toRadians(180));
        cam.rotateX(Math.toRadians(-40));

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
                new Vector3(0, 0, -30),
                Material
                    .builder()
                    .Ka(new Vector3(0.02, 0.02, 0.02))
                    .Kd(new Vector3(0.02, 0.02, 0.02))
                    .Ks(new Vector3(1.0,  1.0,  1.0 ))
                    .Ns(1000.0)
                    .opacity(1.0)
                    .Ni(100.0)
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

        scene.addLightSource(new LightSource(
            new Vector3(20, 0, -80),
            new RGBColor(255, 255, 255),
            10000
        ));

        scene.addLightSource(new LightSource(
            new Vector3(0, 20, 50),
            new RGBColor(180, 200, 220),
            10000
        ));

        scene.addLightSource(new SpotLight(
            new Vector3(0, 50, 15),
            new Vector3(0, -1, -0.3),
            new RGBColor(255, 255, 240),
            5000,
            Math.toRadians(15),
            Math.toRadians(25)
        ));

        scene.addLightSource(new SpotLight(
            new Vector3(0, 4, 25),
            new Vector3(0, 0, -1),
            new RGBColor(255, 255, 240),
            9000,
            Math.toRadians(45),
            Math.toRadians(85)
        ));

        Raytracer raytracer = new Raytracer(cam, scene);

        File tempDir = new File("raytracer_output");
        tempDir.delete();
        tempDir.mkdir();

        double dt  = 1;
        int count = 0;
        ArrayList<BaseLightSource> lights = scene.getLights();
        
        // for(double t = 0; t < 120 * 6; t+=dt){
        for(double t = 0; t < 1; t+=dt){
            long start = System.nanoTime();
            if(t < 330){
                if (lights.get(0) instanceof LightSource) {
                    LightSource light = (LightSource) lights.get(0);
                    lights.set(0,
                        new LightSource(
                            light.origin().add(new Vector3(dt /  2, 0, 0)),
                            light.lightColor(),
                            light.intensity()
                        )
                    );
                }
            }

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

            File outputFile = new File(tempDir, "render_" + count + ".jpg");
            raytracer.Render(outputFile);
            count++;

            long end = System.nanoTime();
            long elapsed = end - start;
            System.out.println("[Render] Frame " + count + " — " + (elapsed / 1_000_000.0) + " ms");
        }

        System.out.println("[Render] Output: " + tempDir.getAbsolutePath());
    }
}