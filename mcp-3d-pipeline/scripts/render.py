"""Blender script: Render a scene to an image file."""
import bpy
import json
import sys

args = json.loads(sys.argv[sys.argv.index("--") + 1])

scene = bpy.context.scene
scene.render.engine = args.get("engine", "CYCLES")
scene.render.resolution_x = args.get("resolution_x", 1920)
scene.render.resolution_y = args.get("resolution_y", 1080)
scene.render.filepath = args["output_path"]
scene.render.image_settings.file_format = "PNG"

if scene.render.engine == "CYCLES":
    scene.cycles.samples = args.get("samples", 128)
    if args.get("use_gpu", True):
        scene.cycles.device = "GPU"
        prefs = bpy.context.preferences.addons["cycles"].preferences
        prefs.compute_device_type = "CUDA"
        prefs.get_devices()
        for device in prefs.devices:
            device.use = device.type != "CPU"

bpy.ops.render.render(write_still=True)

print("## OUTPUT ##")
print(f"Rendered successfully to: {args['output_path']}")
print(f"Engine: {scene.render.engine}")
print(f"Resolution: {scene.render.resolution_x}x{scene.render.resolution_y}")
