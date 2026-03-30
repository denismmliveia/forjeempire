"""Blender script: Get detailed scene information."""
import bpy
import json

info = {
    "file": bpy.data.filepath or "unsaved",
    "objects": [],
    "summary": {
        "total_objects": len(bpy.data.objects),
        "meshes": len(bpy.data.meshes),
        "materials": len(bpy.data.materials),
        "textures": len(bpy.data.images),
        "cameras": 0,
        "lights": 0,
        "armatures": len(bpy.data.armatures),
        "animations": len(bpy.data.actions),
        "collections": len(bpy.data.collections),
    },
}

for obj in bpy.data.objects:
    obj_info = {
        "name": obj.name,
        "type": obj.type,
        "location": list(obj.location),
        "scale": list(obj.scale),
        "visible": obj.visible_get(),
    }
    if obj.type == "MESH" and obj.data:
        obj_info["vertices"] = len(obj.data.vertices)
        obj_info["faces"] = len(obj.data.polygons)
        obj_info["materials"] = [m.name for m in obj.data.materials if m]
    elif obj.type == "CAMERA":
        info["summary"]["cameras"] += 1
    elif obj.type == "LIGHT":
        info["summary"]["lights"] += 1
        obj_info["light_type"] = obj.data.type
        obj_info["energy"] = obj.data.energy

    info["objects"].append(obj_info)

print("## OUTPUT ##")
print(json.dumps(info, indent=2))
