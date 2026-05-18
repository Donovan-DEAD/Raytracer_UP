package com.github.donovan_dead.Raytracer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.github.donovan_dead.Math.Vector3;
import com.github.donovan_dead.Objects.Structures.Material;

public class MtlReader {

    public static MtlReaderResult ReadMtlFile(File file) throws Exception{

        if(!file.exists() || file.isDirectory() || !file.getName().endsWith(".mtl"))
            throw new Exception("The file doesn't exist or either is a directory or is the incorrect extension.");

        BufferedReader fileReader = new BufferedReader(new FileReader(file));
        ArrayList<Material> mtlList = new ArrayList<>();
        TreeMap<String, Integer> nameToIdx = new TreeMap<>();

        mtlList.add(Material.getDefaultMaterial());

        Material.Builder materialBuilder = Material.builder();
        String currentMaterialName = null;
        String line;
        StringTokenizer tokenizer;

        while ((line = fileReader.readLine()) != null) {
            if (line.trim().isEmpty() || line.trim().startsWith("#")) continue;

            tokenizer = new StringTokenizer(line);
            if (!tokenizer.hasMoreTokens()) continue;

            String type = tokenizer.nextToken();

            if (type.equals("newmtl")) {
                if (currentMaterialName != null) {
                    Material material = materialBuilder.build();
                    nameToIdx.put(currentMaterialName, mtlList.size());
                    mtlList.add(material);
                    materialBuilder.clean();
                }

                if (tokenizer.hasMoreTokens()) {
                    currentMaterialName = tokenizer.nextToken();
                }
                continue;
            }

            switch (type) {
                case "Ka":
                    if (tokenizer.countTokens() >= 3) {
                        materialBuilder.Ka(new Vector3(
                            Double.parseDouble(tokenizer.nextToken()),
                            Double.parseDouble(tokenizer.nextToken()),
                            Double.parseDouble(tokenizer.nextToken())
                        ));
                    }
                    break;

                case "Kd":
                    if (tokenizer.countTokens() >= 3) {
                        materialBuilder.Kd(new Vector3(
                            Double.parseDouble(tokenizer.nextToken()),
                            Double.parseDouble(tokenizer.nextToken()),
                            Double.parseDouble(tokenizer.nextToken())
                        ));
                    }
                    break;

                case "Ks":
                    if (tokenizer.countTokens() >= 3) {
                        materialBuilder.Ks(new Vector3(
                            Double.parseDouble(tokenizer.nextToken()),
                            Double.parseDouble(tokenizer.nextToken()),
                            Double.parseDouble(tokenizer.nextToken())
                        ));
                    }
                    break;

                case "Ns":
                    if (tokenizer.hasMoreTokens()) {
                        materialBuilder.Ns(Double.parseDouble(tokenizer.nextToken()));
                    }
                    break;

                case "d":
                case "Tr":
                    if (tokenizer.hasMoreTokens()) {
                        materialBuilder.opacity(Double.parseDouble(tokenizer.nextToken()));
                    }
                    break;

                case "map_Ka":
                    if (tokenizer.hasMoreTokens()) {
                        try {
                            materialBuilder.ambientTexture(tokenizer.nextToken());
                        } catch (Exception e) {
                            System.err.println("Error cargando textura ambient: " + e.getMessage());
                        }
                    }
                    break;

                case "map_Kd":
                    if (tokenizer.hasMoreTokens()) {
                        try {
                            materialBuilder.diffuseTexture(tokenizer.nextToken());
                        } catch (Exception e) {
                            System.err.println("Error cargando textura diffuse: " + e.getMessage());
                        }
                    }
                    break;

                case "map_Ks":
                    if (tokenizer.hasMoreTokens()) {
                        try {
                            materialBuilder.specularTexture(tokenizer.nextToken());
                        } catch (Exception e) {
                            System.err.println("Error cargando textura specular: " + e.getMessage());
                        }
                    }
                    break;

                case "map_Bump":
                case "bump":
                    if (tokenizer.hasMoreTokens()) {
                        try {
                            materialBuilder.normalTexture(tokenizer.nextToken());
                        } catch (Exception e) {
                            System.err.println("Error cargando textura normal: " + e.getMessage());
                        }
                    }
                    break;

                default:
                    break;
            }
        }

        if (currentMaterialName != null) {
            Material material = materialBuilder.build();
            mtlList.add(material);
            nameToIdx.put(currentMaterialName, mtlList.size() - 1);
        }

        fileReader.close();
        System.out.println("Finishing reading the mtl file");
        return new MtlReaderResult(mtlList, nameToIdx);
    }

    public static class MtlReaderResult {

        private ArrayList<Material> mtlList = new ArrayList<>();
        private TreeMap<String, Integer> nameToIdx = new TreeMap<>();

        public MtlReaderResult(ArrayList<Material> mtls, TreeMap<String, Integer> traductor) {
            this.mtlList = mtls;
            this.nameToIdx = traductor;
        }

        public TreeMap<String, Integer> getNameToIdx() {
            return this.nameToIdx;
        }

        public ArrayList<Material> getMtlList() {
            return this.mtlList;
        }
    }
}
