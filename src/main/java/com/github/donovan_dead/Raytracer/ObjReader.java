package com.github.donovan_dead.Raytracer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeSet;

import com.github.donovan_dead.Math.Utils;

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
        ArrayList<Integer> smoothingGroupList = new ArrayList<>();

        ArrayList<double[]> textureList = new ArrayList<>();

        ArrayList<Vector3> normalList = new ArrayList<>();
        ArrayList<Integer> normalIdxList = new ArrayList<>();

        TreeSet<Integer> smoothingGroupSet = new TreeSet<>();
        int currentSmoothingGroup = 0;

        String line;
        StringTokenizer tokenizer;
        
        while ((line = fileReader.readLine()) != null) {
            if(line.trim().isEmpty()) continue;

            tokenizer = new StringTokenizer(line);
            if(!tokenizer.hasMoreTokens()) continue;

            String type = tokenizer.nextToken();

            if(type.equals("s")){
                if(tokenizer.hasMoreTokens()){
                    String groupStr = tokenizer.nextToken();
                    if(groupStr.equals("off") || groupStr.equals("0")){
                        currentSmoothingGroup = 0;
                        System.out.println("Smoothing group OFF");
                    } else {
                        try {
                            currentSmoothingGroup = Integer.parseInt(groupStr);
                            smoothingGroupSet.add(currentSmoothingGroup);
                            System.out.println("Smoothing group: " + currentSmoothingGroup);
                        } catch(NumberFormatException e){
                            currentSmoothingGroup = 0;
                        }
                    }
                }
                continue;
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
                        if (numbers.length > 2 && !numbers[2].isEmpty() && currentSmoothingGroup != 0) {
                            norm = Integer.parseInt(numbers[2]) - 1;
                        }
                        tempNormVertList.add(norm);
                    }

                    if(tempVertList.size() <= 3) {
                        vertIdxList.addAll(tempVertList);
                        normalIdxList.addAll(tempNormVertList);
                        for(int i = 0; i < tempVertList.size(); i++){
                            smoothingGroupList.add(currentSmoothingGroup);
                        }
                    }
                    else {
                        Integer v0 = tempVertList.get(0);
                        Integer n0 = tempNormVertList.get(0);

                        for(int i = 1; i < tempVertList.size() - 1; i++){
                            vertIdxList.add(v0);
                            normalIdxList.add(n0);
                            smoothingGroupList.add(currentSmoothingGroup);

                            vertIdxList.add(tempVertList.get(i));
                            normalIdxList.add(tempNormVertList.get(i));
                            smoothingGroupList.add(currentSmoothingGroup);

                            vertIdxList.add( tempVertList.get(i+1));
                            normalIdxList.add(tempNormVertList.get(i+1));
                            smoothingGroupList.add(currentSmoothingGroup);
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

        for(Integer smoothGroup : smoothingGroupSet){
            TreeSet<Integer> vertexIndicesInGroup = new TreeSet<>();

            for(int i = 0; i < smoothingGroupList.size(); i += 3){
                if(
                    smoothingGroupList.get(i).equals(smoothGroup) &&
                    smoothingGroupList.get(i + 1).equals(smoothGroup) &&
                    smoothingGroupList.get(i + 2).equals(smoothGroup)
                )   {
                        vertexIndicesInGroup.add(
                            vertIdxList.get(i)
                        );
                        vertexIndicesInGroup.add(
                            vertIdxList.get(i + 1)
                        );
                        vertexIndicesInGroup.add(
                            vertIdxList.get(i + 2)
                        );
                    }

            }
            for(Integer vertIdx : vertexIndicesInGroup){
                Vector3 normalMean = new Vector3(0, 0, 0);
                int triangleCount = 0;

                for(int i = 0; i < smoothingGroupList.size(); i += 3){
                    if(i + 2 < smoothingGroupList.size()){
                        if(smoothingGroupList.get(i).equals(smoothGroup) &&
                           smoothingGroupList.get(i + 1).equals(smoothGroup) &&
                           smoothingGroupList.get(i + 2).equals(smoothGroup)){

                            Integer v0Idx = vertIdxList.get(i);
                            Integer v1Idx = vertIdxList.get(i + 1);
                            Integer v2Idx = vertIdxList.get(i + 2);

                            if(v0Idx.equals(vertIdx) || v1Idx.equals(vertIdx) || v2Idx.equals(vertIdx)){
                                Vector3 v0 = vertexList.get(v0Idx);
                                Vector3 v1 = vertexList.get(v1Idx);
                                Vector3 v2 = vertexList.get(v2Idx);

                                Vector3 edge1 = v1.subtract(v0);
                                Vector3 edge2 = v2.subtract(v0);
                                Vector3 triangleNormal = Utils.crossProduct(edge1, edge2).normalize();

                                normalMean = normalMean.add(triangleNormal);
                                triangleCount++;
                            }
                        }
                    }
                }

                if(triangleCount > 0){
                    normalMean = normalMean.scale(1.0 / triangleCount);
                    int normalIndex = normalList.size();
                    normalList.add(normalMean);

                    for(int i = 0; i < smoothingGroupList.size(); i++){
                        if(smoothingGroupList.get(i).equals(smoothGroup) &&
                           vertIdxList.get(i).equals(vertIdx)){
                            normalIdxList.set(i, normalIndex);
                        }
                    }
                }
            }
        }

        return ObjObject.builder()
            .vertexIdxList(vertIdxList)
            .vertexList(vertexList)
            .normalList(normalList)
            .normalIdxList(normalIdxList)
            .color(defaultColor)
            .build();
    }

}
