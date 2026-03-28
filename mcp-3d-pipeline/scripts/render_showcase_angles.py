"""Blender script: Render 12 showcase angles of a model.

Usage: blender --background model.blend --python render_showcase_angles.py -- '{"output_dir": "...", "start": 0, "end": 12}'
"""
import bpy
import json
import math
import os
import sys
from mathutils import Vector

args = json.loads(sys.argv[sys.argv.index("--") + 1])
output_dir = args["output_dir"]
start = args.get("start", 0)
end = args.get("end", 12)
resolution = args.get("resolution", 512)

os.makedirs(output_dir, exist_ok=True)

# Unhide everything
for obj in bpy.data.objects:
    obj.hide_render = False
    obj.hide_viewport = False
    try:
        obj.hide_set(False)
    except:
        pass

# Scene centroid
mesh_objects = [o for o in bpy.data.objects if o.type == "MESH"]
if mesh_objects:
    center = Vector((
        sum(o.location.x for o in mesh_objects) / len(mesh_objects),
        sum(o.location.y for o in mesh_objects) / len(mesh_objects),
        sum(o.location.z for o in mesh_objects) / len(mesh_objects),
    ))
else:
    center = Vector((0, 0, 0))

# Camera
camera = None
for obj in bpy.data.objects:
    if obj.type == "CAMERA":
        camera = obj
        break
if not camera:
    cam_data = bpy.data.cameras.new("ShowcaseCam")
    camera = bpy.data.objects.new("ShowcaseCam", cam_data)
    bpy.context.scene.collection.objects.link(camera)
bpy.context.scene.camera = camera

# Render config
scene = bpy.context.scene
scene.render.engine = "CYCLES"
scene.cycles.samples = 32
scene.cycles.device = "GPU"
scene.render.resolution_x = resolution
scene.render.resolution_y = resolution
scene.render.film_transparent = True
scene.render.image_settings.file_format = "PNG"
scene.render.image_settings.color_mode = "RGBA"

# GPU
try:
    prefs = bpy.context.preferences.addons["cycles"].preferences
    prefs.compute_device_type = "CUDA"
    prefs.get_devices()
    for d in prefs.devices:
        d.use = d.type != "CPU"
except:
    pass

radius = 8.0
cam_z = 3.0
look_z = center.z + 1.0

print("## OUTPUT ##")
print(f"Rendering angles {start}-{end-1} at {resolution}x{resolution}")

for i in range(start, end):
    angle = math.radians(i * 30)
    cx = center.x + radius * math.cos(angle)
    cy = center.y + radius * math.sin(angle)
    camera.location = (cx, cy, cam_z)
    direction = Vector((center.x - cx, center.y - cy, look_z - cam_z))
    camera.rotation_euler = direction.to_track_quat("-Z", "Y").to_euler()
    out_path = os.path.join(output_dir, f"angle_{i}.png")
    scene.render.filepath = out_path
    bpy.ops.render.render(write_still=True)
    print(f"  angle_{i} ({i*30} deg) -> {out_path}")

print("Done!")
