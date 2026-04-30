package com.github.donovan_dead.Raytracer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Utils;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.ObjObject;

public class ObjReader {
    public static RGBColor defaultColor = new RGBColor(255,255,255);

    // implementation for future algorithm of ear clipping.
    // baricentric optimized way instead of doing cross product to calculate if is inside of a triangule, avoiding sqrt operations
    private static boolean pointInTriangle(Vector3 p, Vector3 v0, Vector3 v1, Vector3 v2) {
        Vector3 e0 = v1.subtract(v0);
        Vector3 e1 = v2.subtract(v0);
        Vector3 v  = p.subtract(v0);

        double d00 = Utils.dotProduct(e0, e0);
        double d01 = Utils.dotProduct(e0, e1);
        double d11 = Utils.dotProduct(e1, e1);
        double d20 = Utils.dotProduct(v,  e0);
        double d21 = Utils.dotProduct(v,  e1);

        double den = d00 * d11 - d01 * d01;

        if (den <= 1e-12) return false;

        double beta  = (d11 * d20 - d01 * d21) / den;
        double gamma = (d00 * d21 - d01 * d20) / den;
        double alpha = 1.0 - beta - gamma;

        double eps = 1e-9;

        return alpha >= -eps && beta >= -eps && gamma >= -eps;
    }


    public static ObjObject ReadObjectFile(File file) throws Exception {
        
        if(!file.exists() || file.isDirectory() || !file.getName().endsWith(".obj")) throw new Exception("The file doesn't exist or either is a directory or is the incorrect extension.");

        BufferedReader fileReader = new BufferedReader(new FileReader(file));

        ArrayList<Vector3> vertexList = new ArrayList<>();
        ArrayList<double[]> textureList = new ArrayList<>();
        ArrayList<Integer> vertIdxList = new ArrayList<>();

        String line;
        StringTokenizer tokenizer;
        while ((line = fileReader.readLine()) != null) {
            tokenizer = new StringTokenizer(line);
            String type = tokenizer.nextToken();

            switch (type) {
                case "v":
                    vertexList.add(
                        new Vector3(
                            Double.parseDouble(tokenizer.nextToken()),
                            Double.parseDouble(tokenizer.nextToken()),
                            Double.parseDouble(tokenizer.nextToken())
                        )
                    );
                    break;

                case "f":
                    ArrayList<Integer> tempVertList = new ArrayList<>();

                    while (tokenizer.hasMoreTokens()) {
                        String token =  tokenizer.nextToken();
                        String[] numbers = token.split("/");

                        tempVertList.add(Integer.parseInt(numbers[0])-1);
                    }

                    if(tempVertList.size() <= 3) vertIdxList.addAll(tempVertList);
                    else {
                        // Estrategia de abanico
                        Integer v0 = tempVertList.get(0);

                        for(int i = 1; i < tempVertList.size() - 1; i++){
                            vertIdxList.add(v0);
                            vertIdxList.add(tempVertList.get(i));
                            vertIdxList.add( tempVertList.get(i+1));
                        }
                    }
                    
                    break;
                case "vn":
                    break;
                case "vt":
                    textureList.add(
                        new double[]{
                            Double.parseDouble(tokenizer.nextToken()),
                            Double.parseDouble(tokenizer.nextToken())
                        }
                    );
                    break;

                default:
                    break;
            }
        
        }
        fileReader.close();

        return ObjObject.builder().vertexIdxList(vertIdxList).vertexList(vertexList).color(defaultColor).build();
    }

}
