"""Blender script: Render an animation as a sequence of PNG frames.

Usage from MCP: run via blender_execute_script or as standalone script.
Expects JSON args after '--': {
    "output_dir": "path/to/frames/",
    "frame_start": 1,
    "frame_end": 32,
    "resolution": 512,
    "engine": "BLENDER_EEVEE",
    "samples": 64,
    "use_gpu": true,
    "transparent_bg": true
}
"""
import bpy
import json
import os
import sys

args = json.loads(sys.argv[sys.argv.index("--") + 1])

output_dir = args["output_dir"]
frame_start = args.get("frame_start", 1)
frame_end = args.get("frame_end", 32)
resolution = args.get("resolution", 512)
engine = args.get("engine", "BLENDER_EEVEE")
samples = args.get("samples", 64)
use_gpu = args.get("use_gpu", True)
transparent_bg = args.get("transparent_bg", True)

os.makedirs(output_dir, exist_ok=True)

scene = bpy.context.scene
scene.render.engine = engine
scene.render.resolution_x = resolution
scene.render.resolution_y = resolution
scene.render.image_settings.file_format = "PNG"
scene.render.image_settings.color_mode = "RGBA" if transparent_bg else "RGB"
scene.render.film_transparent = transparent_bg

# Use Standard color management for vivid neon colors (not Filmic which desaturates)
scene.view_settings.view_transform = 'Standard'
scene.view_settings.look = 'None'

if engine == "CYCLES":
    scene.cycles.samples = samples
    if use_gpu:
        scene.cycles.device = "GPU"
        prefs = bpy.context.preferences.addons["cycles"].preferences
        prefs.compute_device_type = "CUDA"
        prefs.get_devices()
        for device in prefs.devices:
            device.use = device.type != "CPU"
elif engine == "BLENDER_EEVEE":
    scene.eevee.taa_render_samples = samples

scene.frame_start = frame_start
scene.frame_end = frame_end

rendered_files = []
for frame in range(frame_start, frame_end + 1):
    scene.frame_set(frame)
    filepath = os.path.join(output_dir, f"frame_{frame:04d}.png")
    scene.render.filepath = filepath
    bpy.ops.render.render(write_still=True)
    rendered_files.append(filepath)

print("## OUTPUT ##")
print(f"Rendered {len(rendered_files)} frames to: {output_dir}")
print(f"Engine: {engine}, Resolution: {resolution}x{resolution}")
print(f"Frames: {frame_start}-{frame_end}")
