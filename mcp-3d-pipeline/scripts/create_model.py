"""Blender script: Create a procedural 3D model.
This script receives a description and creates basic 3D models using Blender primitives.
For complex models, the MCP server should generate specific Blender Python code instead.
"""
import bpy
import json
import sys
from pathlib import Path

args = json.loads(sys.argv[sys.argv.index("--") + 1])
description = args.get("description", "")
output_path = args.get("output_path", "")
fmt = args.get("format", "blend").lower()

# Clear default objects
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete()

# Add camera and light
bpy.ops.object.camera_add(location=(5, -5, 4))
cam = bpy.context.active_object
cam.rotation_euler = (1.1, 0, 0.78)
bpy.context.scene.camera = cam

bpy.ops.object.light_add(type='SUN', location=(5, 5, 10))
bpy.context.active_object.data.energy = 3

bpy.ops.object.light_add(type='AREA', location=(-3, -3, 5))
bpy.context.active_object.data.energy = 50

# Set render settings
bpy.context.scene.render.engine = 'CYCLES'
bpy.context.scene.cycles.samples = 128
bpy.context.scene.render.resolution_x = 1920
bpy.context.scene.render.resolution_y = 1080

# Set world background
world = bpy.data.worlds.get("World") or bpy.data.worlds.new("World")
bpy.context.scene.world = world
world.use_nodes = True
bg = world.node_tree.nodes.get("Background")
if bg:
    bg.inputs[0].default_value = (0.05, 0.05, 0.08, 1.0)

print("## OUTPUT ##")
print(f"Base scene created for: {description}")
print(f"Note: This creates an empty scene with camera and lighting.")
print(f"For actual model creation, use blender_execute_script with specific Python code.")
print(f"The scene is ready for adding objects via blender_execute_script.")

# Save
if fmt == "blend":
    bpy.ops.wm.save_as_mainfile(filepath=output_path)
else:
    bpy.ops.wm.save_as_mainfile(filepath=output_path.replace(f".{fmt}", ".blend"))
    exporters = {
        "glb": lambda p: bpy.ops.export_scene.gltf(filepath=p, export_format="GLB"),
        "fbx": lambda p: bpy.ops.export_scene.fbx(filepath=p),
        "obj": lambda p: bpy.ops.wm.obj_export(filepath=p),
        "stl": lambda p: bpy.ops.wm.stl_export(filepath=p),
    }
    if fmt in exporters:
        exporters[fmt](output_path)

print(f"Saved to: {output_path}")
