"""Blender script: Export scene to various 3D formats."""
import bpy
import json
import sys

args = json.loads(sys.argv[sys.argv.index("--") + 1])
output_path = args["output_path"]
fmt = args.get("format", "GLB").upper()

exporters = {
    "GLB": lambda p: bpy.ops.export_scene.gltf(filepath=p, export_format="GLB"),
    "GLTF": lambda p: bpy.ops.export_scene.gltf(filepath=p, export_format="GLTF_SEPARATE"),
    "FBX": lambda p: bpy.ops.export_scene.fbx(filepath=p),
    "OBJ": lambda p: bpy.ops.wm.obj_export(filepath=p),
    "STL": lambda p: bpy.ops.wm.stl_export(filepath=p),
    "PLY": lambda p: bpy.ops.wm.ply_export(filepath=p),
    "USD": lambda p: bpy.ops.wm.usd_export(filepath=p),
}

print("## OUTPUT ##")
if fmt in exporters:
    exporters[fmt](output_path)
    print(f"Exported successfully to: {output_path}")
    print(f"Format: {fmt}")
else:
    print(f"ERROR: Unsupported format '{fmt}'. Supported: {', '.join(exporters.keys())}")
