"""Blender script: Import a 3D model, process it, and save as .blend."""
import bpy
import json
import sys
from pathlib import Path

args = json.loads(sys.argv[sys.argv.index("--") + 1])
input_file = args["input_file"]
output_blend = args["output_blend"]
operations = json.loads(args.get("operations", "{}")) if args.get("operations") else {}

# Clear default scene
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete()

# Import based on file extension
ext = Path(input_file).suffix.lower()
importers = {
    ".obj": lambda f: bpy.ops.wm.obj_import(filepath=f),
    ".fbx": lambda f: bpy.ops.import_scene.fbx(filepath=f),
    ".glb": lambda f: bpy.ops.import_scene.gltf(filepath=f),
    ".gltf": lambda f: bpy.ops.import_scene.gltf(filepath=f),
    ".stl": lambda f: bpy.ops.wm.stl_import(filepath=f),
    ".ply": lambda f: bpy.ops.wm.ply_import(filepath=f),
    ".usd": lambda f: bpy.ops.wm.usd_import(filepath=f),
    ".usda": lambda f: bpy.ops.wm.usd_import(filepath=f),
    ".usdc": lambda f: bpy.ops.wm.usd_import(filepath=f),
}

print("## OUTPUT ##")

if ext not in importers:
    print(f"ERROR: Unsupported format '{ext}'. Supported: {', '.join(importers.keys())}")
    sys.exit(1)

importers[ext](input_file)
imported_objects = [o for o in bpy.data.objects if o.type == "MESH"]
print(f"Imported {len(imported_objects)} mesh objects from {input_file}")

# Apply operations
if operations.get("center", False):
    bpy.ops.object.select_all(action='SELECT')
    bpy.ops.object.origin_set(type='ORIGIN_GEOMETRY', center='BOUNDS')
    for obj in bpy.data.objects:
        obj.location = (0, 0, 0)
    print("Centered objects at origin")

if "scale" in operations:
    scale = float(operations["scale"])
    for obj in bpy.data.objects:
        obj.scale = (scale, scale, scale)
    bpy.ops.object.select_all(action='SELECT')
    bpy.ops.object.transform_apply(scale=True)
    print(f"Scaled objects by {scale}")

if operations.get("smooth", False):
    for obj in imported_objects:
        bpy.context.view_layer.objects.active = obj
        obj.select_set(True)
        bpy.ops.object.shade_smooth()
        obj.select_set(False)
    print("Applied smooth shading")

if "decimate_ratio" in operations:
    ratio = float(operations["decimate_ratio"])
    for obj in imported_objects:
        mod = obj.modifiers.new(name="Decimate", type='DECIMATE')
        mod.ratio = ratio
        bpy.context.view_layer.objects.active = obj
        bpy.ops.object.modifier_apply(modifier="Decimate")
    print(f"Applied decimation (ratio: {ratio})")

# Add camera and light if none exist
if not any(o.type == "CAMERA" for o in bpy.data.objects):
    bpy.ops.object.camera_add(location=(3, -3, 2))
    cam = bpy.context.active_object
    cam.rotation_euler = (1.1, 0, 0.78)
    bpy.context.scene.camera = cam

if not any(o.type == "LIGHT" for o in bpy.data.objects):
    bpy.ops.object.light_add(type='SUN', location=(5, 5, 10))
    bpy.context.active_object.data.energy = 3

# Save
bpy.ops.wm.save_as_mainfile(filepath=output_blend)
print(f"Saved to: {output_blend}")

total_verts = sum(len(o.data.vertices) for o in imported_objects)
total_faces = sum(len(o.data.polygons) for o in imported_objects)
print(f"Total vertices: {total_verts}, Total faces: {total_faces}")
