"""Blender script: Render 6 phase images by progressively showing _pN objects.

Usage: blender --background model.blend --python render_phases.py -- '{"output_dir": "...", "resolution": 512}'

Phase 1: only _p1 objects visible
Phase 2: _p1 + _p2 visible
...
Phase 6: all _p1 through _p6 visible (complete model)
"""
import bpy
import json
import math
import os
import sys
from mathutils import Vector

args = json.loads(sys.argv[sys.argv.index("--") + 1])
output_dir = args["output_dir"]
resolution = args.get("resolution", 512)

os.makedirs(output_dir, exist_ok=True)

# Classify objects by phase
def get_phase(obj):
    name = obj.name
    for p in range(1, 7):
        if f"_p{p}" in name:
            return p
    return 0  # ENV or untagged

env_objects = []
phase_objects = {i: [] for i in range(1, 7)}

for obj in bpy.data.objects:
    p = get_phase(obj)
    if p == 0:
        env_objects.append(obj)
    else:
        phase_objects[p].append(obj)

# Compute center from all mesh objects
mesh_objects = [o for o in bpy.data.objects if o.type == "MESH" and not o.name.startswith("ENV_")]
if mesh_objects:
    center = Vector((
        sum(o.location.x for o in mesh_objects) / len(mesh_objects),
        sum(o.location.y for o in mesh_objects) / len(mesh_objects),
        sum(o.location.z for o in mesh_objects) / len(mesh_objects),
    ))
else:
    center = Vector((0, 0, 0))

# Camera setup
camera = None
for obj in bpy.data.objects:
    if obj.type == "CAMERA":
        camera = obj
        break
if not camera:
    cam_data = bpy.data.cameras.new("PhaseCam")
    camera = bpy.data.objects.new("PhaseCam", cam_data)
    bpy.context.scene.collection.objects.link(camera)
bpy.context.scene.camera = camera

# Position camera
radius = 6.0
cam_angle = math.radians(-45)
cam_z = center.z + 2.5
cx = center.x + radius * math.cos(cam_angle)
cy = center.y + radius * math.sin(cam_angle)
camera.location = (cx, cy, cam_z)
direction = Vector((center.x - cx, center.y - cy, center.z - cam_z + 1.0))
camera.rotation_euler = direction.to_track_quat("-Z", "Y").to_euler()

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

print("## OUTPUT ##")
print(f"Rendering 6 phases at {resolution}x{resolution}")
print(f"Center: {center}")
print(f"Phase object counts: {', '.join(f'p{k}={len(v)}' for k, v in phase_objects.items())}")

for phase in range(1, 7):
    # Show ENV objects always
    for obj in env_objects:
        obj.hide_render = False
        obj.hide_viewport = False

    # Show objects up to current phase, hide the rest
    for p in range(1, 7):
        visible = p <= phase
        for obj in phase_objects[p]:
            obj.hide_render = not visible
            obj.hide_viewport = not visible

    out_path = os.path.join(output_dir, f"phase_{phase}.png")
    scene.render.filepath = out_path
    bpy.ops.render.render(write_still=True)
    print(f"  phase_{phase} -> {out_path}")

print("Done!")
