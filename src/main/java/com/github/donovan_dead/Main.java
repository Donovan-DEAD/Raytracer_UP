package com.github.donovan_dead;

import java.io.File;
import java.util.ArrayList;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.ObjObject;
import com.github.donovan_dead.Physics.BaseLightSource;
import com.github.donovan_dead.Physics.DirectionalLight;
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

        int count = 0;
        for(ObjObject obj : list){
            if(count == 1){
                obj.translate(
                    new Vector3(5, 0, 0)
                );
                obj.scale(5);
            }
            else {
                obj.translate(
                    new Vector3(-5, -2, 0)
                );
            }
            
            count++;
        }

        return list;
    }
    public static void main(String[] args) throws Exception {
        Camera cam = new Camera(Vector3.builder().X(0).Y(0).Z(0).build(), 1, 100);
        cam.translate(new Vector3(0, 5, 15));
        cam.rotateZ(Math.toRadians(180));
        Scene scene = new Scene();
        
        ArrayList<ObjObject> objList = readObjectsFromDir("objects");
        
        if(objList == null) throw new Exception("No objects where found");
        for(ObjObject obj : objList) scene.addObject(obj);
        

        scene.addLightSource(new LightSource(
            new Vector3(-4, -5, -10),
            new RGBColor(1, 1, 1)
        ));

        // scene.addLightSource(new LightSource(
        //     new Vector3(0, 0, 0),
        //     new RGBColor(1 , 1, 1)
        // ));

        scene.addLightSource(new LightSource(
            new Vector3(0, -10, 15),
            new RGBColor(1 , 1, 1)
        ));

        // scene.addLightSource(new LightSource(
        //     new Vector3(0, 50, 10),
        //     new RGBColor(0.6 , 0, 0)
        // ));

        // scene.addLightSource(new DirectionalLight(
        //     new Vector3(1, -1, -1).normalize(),
        //     new RGBColor(0.8, 0.8, 0.8)
        // ));

        // scene.addLightSource(new DirectionalLight(
        //     new Vector3(0, -1, 0).normalize(),
        //     new RGBColor(0.8, 0.8, 0.8)
        // ));

        Raytracer raytracer = new Raytracer(cam, scene);

        File tempDir = new File("raytracer_output");
        tempDir.delete();
        tempDir.mkdir();

        double dt  = 1;
        int count = 0;
        ArrayList<BaseLightSource> lights = scene.getLights();
        
        for(double t = 0; t < 1; t+=dt){

            if (lights.get(0) instanceof LightSource) {
                LightSource light = (LightSource) lights.get(0);
                lights.set(0,
                    new LightSource(
                        light.origin().add(new Vector3(dt, 0, 0)),
                        light.lightColor()
                    )
                );
            }

            File outputFile = new File(tempDir, "render_" + count + ".jpg");
            raytracer.Render(outputFile);
            count++;
        }

        System.out.println("Rendered to: " + tempDir.getAbsolutePath());
    }
}