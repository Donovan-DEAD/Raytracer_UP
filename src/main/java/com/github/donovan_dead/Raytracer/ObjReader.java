package com.github.donovan_dead.Raytracer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.github.donovan_dead.Colors.RGBColor;
import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.ObjObject;

public class ObjReader {
    public static RGBColor defaultColor = new RGBColor(255,255,255);

    public static ObjObject ReadObjectFile(File file) throws Exception {
        
        if(!file.exists() || file.isDirectory() || !file.getName().endsWith(".obj")) throw new Exception("The file doesn't exist or either is a directory or is the incorrect extension.");

        BufferedReader fileReader = new BufferedReader(new FileReader(file));

        ArrayList<Vector3> vertexList = new ArrayList<>();
        ArrayList<Integer> vertIdxList = new ArrayList<>();
        
        ArrayList<double[]> textureList = new ArrayList<>();
        
        ArrayList<Vector3> normalList = new ArrayList<>();
        ArrayList<Integer> normalIdxList = new ArrayList<>();

        String line;
        StringTokenizer tokenizer;
        while ((line = fileReader.readLine()) != null) {
            
            
            tokenizer = new StringTokenizer(line);
            String type = tokenizer.nextToken();

            if(type == "s"){
                // while () {
                    
                // }
                System.out.println("Smooth shading group founded");
            }

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
                    ArrayList<Integer> tempNormVertList = new ArrayList<>();

                    while (tokenizer.hasMoreTokens()) {
                        String token =  tokenizer.nextToken();
                        String[] numbers = token.split("/");

                        tempVertList.add(Integer.parseInt(numbers[0])-1);

                        Integer norm = -1;
                        if (numbers.length > 2 && !numbers[2].isEmpty()) {
                            norm = Integer.parseInt(numbers[2]) - 1;
                        }
                        tempNormVertList.add(norm);
                    }

                    if(tempVertList.size() <= 3) {
                        vertIdxList.addAll(tempVertList);
                        normalIdxList.addAll(tempNormVertList);
                    }
                    else {
                        Integer v0 = tempVertList.get(0);
                        Integer n0 = tempNormVertList.get(0);

                        for(int i = 1; i < tempVertList.size() - 1; i++){
                            vertIdxList.add(v0);
                            normalIdxList.add(n0);

                            vertIdxList.add(tempVertList.get(i));
                            normalIdxList.add(tempNormVertList.get(i));
                            
                            vertIdxList.add( tempVertList.get(i+1));
                            normalIdxList.add(tempNormVertList.get(i+1));
                        }
                    }
                    
                    break;
                case "vn":
                    normalList.add(
                        new Vector3(
                            Double.parseDouble(tokenizer.nextToken()),
                            Double.parseDouble(tokenizer.nextToken()),
                            Double.parseDouble(tokenizer.nextToken())
                        )
                    );
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

        return ObjObject.builder().vertexIdxList(vertIdxList).vertexList(vertexList).normalList(normalList).normalIdxList(normalIdxList).color(defaultColor).build();
    }

}
