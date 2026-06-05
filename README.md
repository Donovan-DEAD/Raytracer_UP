# Raytracer v1.0

A production-quality ray tracing renderer implemented in Java with support for physically-based rendering (PBR), advanced lighting, and real-time optimization techniques.

## Overview

This raytracer progresses from basic ray tracing fundamentals to sophisticated computer graphics algorithms, implementing features such as:

- **Spatial Acceleration**: Bounding Volume Hierarchy (BVH) with Surface Area Heuristic (SAH)
- **Physically-Based Materials**: Cook-Torrance microfacet BRDF model with normal mapping
- **Advanced Lighting**: Area lights (circle, rectangle, triangle) with soft shadows
- **Optical Effects**: Refraction, reflection, Fresnel equations, and depth of field
- **Performance Optimization**: Virtual threads, prefix array optimization, parallel rendering

## Project Evolution

The raytracer evolved through 16 major versions (v0.1 → v1.0) over approximately one month:

| Version | Date       | Key Features |
|---------|------------|--------------|
| v0.1    | 2026-04-21 | Core ray tracing framework |
| v0.1.2  | 2026-04-27 | Triangle primitives, parallelism |
| v0.3    | 2026-04-29 | OBJ file loading |
| v0.5    | 2026-05-07 | Smooth shading |
| v0.5.2  | 2026-05-11 | BVH with SAH |
| v0.7    | 2026-05-13 | Shadows, spot lights |
| v0.7.1  | 2026-05-14 | Prefix array optimization |
| v0.8.2  | 2026-05-18 | PNG texture mapping |
| v0.8.3  | 2026-05-20 | Refraction, Fresnel |
| v0.9.1  | 2026-05-20 | Cook-Torrance, normal maps |
| v0.9.2  | 2026-05-25 | Area lights, soft shadows |
| v1.0    | 2026-05-28 | Depth of field |

## Key Features

### Acceleration Structures
- **BVH with Surface Area Heuristic**: O(n log n) construction using prefix arrays
- **Hierarchical BVH**: Top-level acceleration for multi-object scenes
- **Optimized Ray-Box Intersection**: Slab method with efficient traversal

### Material System
- **Phong Reflection Model**: Classic specular/diffuse shading
- **Cook-Torrance BRDF**: Microfacet model with physical accuracy
- **Texture Support**: Color, normal, roughness, and metallic maps
- **Normal Mapping**: Per-pixel surface detail with TBN transformation

### Lighting
- **Point Lights**: Simple omnidirectional illumination
- **Spot Lights**: Directional lights with cone attenuation
- **Area Lights**: 
  - Circular (disk) with disc sampling
  - Rectangular with stratified sampling
  - Triangular with barycentric sampling
- **Soft Shadows**: Stratified sampling for realistic penumbra

### Optical Phenomena
- **Refraction**: Snell's law with refractive index support
- **Fresnel Equations**: Schlick approximation for reflection/refraction balance
- **Total Internal Reflection**: Automatic detection and handling
- **Medium IOR**: Support for materials in different media
- **Depth of Field**: Aperture simulation with focal distance control

### Performance Optimizations
- **Virtual Threads**: Java Project Loom for parallel rendering
- **Conditional Sampling**: Intelligent area light sampling with DOF
- **Prefix Array Optimization**: Reduces BVH construction 

- **Cache-Friendly Layout**: Optimized memory access patterns

## Technical Requirements

### System Requirements
- **Java**: JDK 19 or later (for virtual threads support)
- **Maven**: 3.6+
- **Memory**: 4GB minimum (8GB recommended)
- **Storage**: 500MB for project and dependencies

### Build Dependencies
- **Image I/O**: Java standard library for PNG/JPG loading
- **Math Library**: Custom Vector3 and Matrix implementations

## Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/Donovan-DEAD/Raytracer_UP
cd cg_raytracer
```

### 2. Build the Project
```bash
# Standard Maven build
mvn clean install

# Fast incremental builds with Maven daemon
mvnd clean install
```

### 3. Structure Setup
Create the following directory structure:
```
cg_raytracer/
├── objects/              # Place OBJ files here
├── raytracer_output/     # Rendered images (auto-created)
├── src/
└── pom.xml
```

## Usage

### Running the Raytracer

1. **Prepare Scene Objects**
   - Place OBJ files in the `objects/` directory
   - Supported formats: OBJ with MTL material definitions

2. **Configure the Scene** (in `Main.java`)
   ```java
   // Create camera
   Camera cam = new Camera(
       Vector3.builder().X(0).Y(0).Z(0).build(),
       1,          // aperture diameter for DOF
       1000,       // focal distance
       0.2,        // focal length
       23          // samples per pixel
   );
   
   // Add lights
   scene.addLightSource(new CircleAreaLight(
       new Vector3(0, 5, 32),
       1.75,                           // radius
       new RGBColor(255, 255, 255),
       1400                            // intensity
   ));
   
   // Render
   Raytracer raytracer = new Raytracer(cam, scene);
   raytracer.Render(outputFile);
   ```

3. **Run the Application**
   ```bash
   # Using Maven
   mvn exec:java -Dexec.mainClass="com.github.donovan_dead.Main"
   
   # Or compile and run directly
   mvn compile
   java -cp target/classes:target/dependency/* com.github.donovan_dead.Main
   ```

4. **Output**
   - Rendered frames saved to `raytracer_output/` as PNG files
   - Console logs show frame timing and BVH statistics

### Configurable Parameters

In `Main.java`, adjust:

```java
// Camera parameters
new Camera(position, aperture, focalDistance, focalLength, samplesPerPixel)

// Area light parameters
new CircleAreaLight(origin, radius, color, intensity)
new RectangleAreaLight(origin, width, height, color, intensity)
new TriangleAreaLight(origin, v1, v2, v3, color, intensity)

// Material properties
Material.builder()
    .Ka(ambientColor)
    .Kd(diffuseColor)
    .Ks(specularColor)
    .Ns(shininess)
    .Ni(refractiveIndex)
    .opacity(transparency)
    .roughness(roughness)
    .metallic(metallic)
    .build()
```

## Project Structure

```
src/main/java/com/github/donovan_dead/
├── Main.java                          # Entry point
├── Colors/
│   └── RGBColor.java                  # Color representation
├── Math/
│   ├── Vector3.java                   # 3D vector math
│   ├── UV.java                        # Texture coordinates
│   ├── BarycentricCoordinates.java    # Triangle interpolation
│   └── Utils.java                     # Mathematical utilities
├── Objects/
│   ├── ObjObject.java                 # OBJ mesh with BVH
│   ├── Sphere.java                    # Sphere primitive
│   ├── Plane.java                     # Plane primitive
│   └── Structures/
│       ├── Material.java              # BRDF models
│       ├── Texture.java               # Texture sampling
│       ├── AABB.java                  # Bounding boxes
│       └── BVHNode.java               # BVH tree nodes
├── Physics/
│   ├── Ray.java                       # Ray representation
│   ├── Intersection.java              # Ray-surface intersection
│   ├── BaseLightSource.java           # Light abstraction
│   ├── DirectionalLight.java          # Directional light
│   ├── areaLights/
│   │   ├── AreaLight.java             # Area light base
│   │   ├── CircleAreaLight.java       # Disk light
│   │   ├── RectangleAreaLight.java    # Rectangle light
│   │   └── TriangleAreaLight.java     # Triangle light
│   └── Raytracer/
│       ├── Camera.java                # View generation
│       ├── Scene.java                 # Scene management
│       ├── ObjReader.java             # OBJ file parser
│       └── Raytracer.java             # Main rendering loop
```

## Technical Deep Dive

### Acceleration Structure: BVH with SAH

The raytracer uses a Bounding Volume Hierarchy with Surface Area Heuristic for efficient ray-geometry intersection:

**Algorithm:**
1. Sort primitives by split axis (X, Y, Z)
2. Compute prefix and suffix AABB arrays (O(n))
3. Evaluate split costs using SAH formula
4. Select split with minimum cost
5. Recursively construct left and right subtrees

**Performance:**
- Construction time: O(n log² n) with balanced tree
- Traversal: O(log n) ray-box intersections per ray

### Physically-Based Rendering: Cook-Torrance BRDF

The Cook-Torrance model represents surfaces as collections of microscopic mirrors:

```
f_r(l, v) = k_d * (c/π) + k_s * (DFG / (4(n·l)(n·v)))
```

Where:
- **D**: Normal Distribution Function (Beckmann/GGX)
- **F**: Fresnel term (Schlick approximation)
- **G**: Geometric attenuation (Smith shadow-masking)
- **k_d, k_s**: Diffuse and specular contributions

**Key Implementation Details:**
- Clamped dot products to prevent division by zero
- Range-validated intermediate values for numerical stability
- Support for metallic materials and roughness textures
- Medium refractive index for materials in non-air media

### Area Lights & Soft Shadows

Area lights use stratified sampling for efficient soft shadow computation:

```
L_direct = (1/N) Σ L_i · visibility(p, l_i)
```

**Sampling Strategies:**
- Circle: Uniform disk sampling with √r scaling
- Rectangle: Bilinear interpolation
- Triangle: Barycentric coordinates

**DOF Optimization:**
When depth of field is enabled, area light samples are reduced from 23 to 10, as the aperture sampling already reduces noise significantly.

### Depth of Field

Camera DOF simulates photographic aperture:

```
p_aperture = c + r(cos(θ)u + sin(θ)v)
```

Rays from the aperture point focus at the focal plane, creating realistic focus/blur effects.

## Known Limitations & Future Work

## Challenges & Solutions

### Challenge: Cook-Torrance Numerical Stability
**Problem**: Edge cases at grazing angles caused NaN values and energy violations.

**Solution**: Systematic clamping of all dot products and BRDF components with epsilon guards.

### Challenge: BVH Construction Performance
**Problem**: Initial O(n²) implementation.

**Solution**: Prefix array optimization reducing complexity to O(n log n).

## Resources & References

### Academic Materials
- [Physically Based Rendering: From Theory to Implementation](https://www.pbr-book.org/3ed-2018/Primitives_and_Intersection_Acceleration/Bounding_Volume_Hierarchies)
  - Comprehensive BVH construction and SAH theory

- [Scratch a Pixel](https://www.scratchapixel.com/)
  - Ray tracing fundamentals and implementation tutorials

### Video Resources
- [BVH Construction with Prefix Array Optimization](https://www.youtube.com/watch?v=gya7x9H3mV0)
  - Direct solution to SAH performance bottleneck

- [Ray Tracing Series](https://www.youtube.com/watch?v=RRE-F57fbXw)
  - General ray tracing concepts and strategies

## Building & Testing

### Maven Build Commands

```bash
# Full clean build
mvn clean install

# Fast incremental build with Maven daemon
mvnd compile

# Run tests (if available)
mvn test

# Create JAR artifact
mvn package

# Generate JavaDoc
mvn javadoc:javadoc
```

### Development Tools
- **IDE**: VS Code
- **Build System**: Maven with incremental builds (mvnd)
- **Version Control**: Git

## Author

**Donovan Eliam Aguilar Diaz**

- **Institution**: Universidad Panamericana, Guadalajara
- **Degree**: B.S. in Systems and Computer Graphics Engineering
- **Development Period**: April 21 - May 28, 2026

## License

This project is provided as educational material.


---

**Last Updated**: May 28, 2026  
**Project Status**: v1.0 - Complete and optimized
