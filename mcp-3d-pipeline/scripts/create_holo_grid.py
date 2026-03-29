"""Create holo_grid.blend - vivid holographic grid with animated scan line."""
import bpy
import os

# Clear scene
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete()

scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 24

# --- Grid lines using wireframe on subdivided plane ---
bpy.ops.mesh.primitive_grid_add(x_subdivisions=16, y_subdivisions=16, size=2)
grid = bpy.context.active_object
grid.name = "HoloGrid"

# Wireframe modifier
wire = grid.modifiers.new(name="Wire", type='WIREFRAME')
wire.thickness = 0.012
wire.use_replace = True  # Only show wireframe, no solid fill

# Grid material: bright cyan emission
mat_grid = bpy.data.materials.new(name="GridMat")
mat_grid.use_nodes = True
nt = mat_grid.node_tree
nt.nodes.clear()

out_node = nt.nodes.new('ShaderNodeOutputMaterial')
out_node.location = (600, 0)
emit = nt.nodes.new('ShaderNodeEmission')
emit.location = (400, 0)
emit.inputs['Color'].default_value = (0.0, 0.94, 1.0, 1.0)  # NeonCyan
emit.inputs['Strength'].default_value = 4.0
nt.links.new(emit.outputs[0], out_node.inputs[0])

grid.data.materials.append(mat_grid)

# --- Scan line: a thin plane that sweeps vertically ---
bpy.ops.mesh.primitive_plane_add(size=2.2, location=(0, 0, 0.01))
scan = bpy.context.active_object
scan.name = "ScanLine"
scan.scale = (1.0, 0.03, 1.0)  # Very thin horizontal band

mat_scan = bpy.data.materials.new(name="ScanMat")
mat_scan.use_nodes = True
snt = mat_scan.node_tree
snt.nodes.clear()

s_out = snt.nodes.new('ShaderNodeOutputMaterial')
s_out.location = (600, 0)
s_emit = snt.nodes.new('ShaderNodeEmission')
s_emit.location = (400, 0)
s_emit.inputs['Color'].default_value = (0.8, 1.0, 1.0, 1.0)  # Bright white-cyan
s_emit.inputs['Strength'].default_value = 15.0  # Very bright scan line
snt.links.new(s_emit.outputs[0], s_out.inputs[0])

# Make scan line transparent at edges using gradient
s_mix = snt.nodes.new('ShaderNodeMixShader')
s_mix.location = (400, -100)
s_transp = snt.nodes.new('ShaderNodeBsdfTransparent')
s_transp.location = (200, -200)
s_grad = snt.nodes.new('ShaderNodeTexGradient')
s_grad.location = (0, -100)
s_coord = snt.nodes.new('ShaderNodeTexCoord')
s_coord.location = (-200, -100)

# Reconnect: mix transparent + emission via gradient
snt.links.new(s_coord.outputs['Generated'], s_grad.inputs['Vector'])
snt.links.new(s_grad.outputs['Fac'], s_mix.inputs['Fac'])
snt.links.new(s_transp.outputs[0], s_mix.inputs[1])
snt.links.new(s_emit.outputs[0], s_mix.inputs[2])

# Relink output to mix shader instead of pure emission
snt.links.clear()
snt.links.new(s_coord.outputs['Generated'], s_grad.inputs['Vector'])
snt.links.new(s_grad.outputs['Fac'], s_mix.inputs['Fac'])
snt.links.new(s_transp.outputs[0], s_mix.inputs[1])
snt.links.new(s_emit.outputs[0], s_mix.inputs[2])
snt.links.new(s_mix.outputs[0], s_out.inputs[0])

mat_scan.blend_method = 'BLEND' if hasattr(mat_scan, 'blend_method') else None
scan.data.materials.append(mat_scan)

# Animate scan line position: sweep from bottom to top
scan.location = (0, -1.1, 0.01)
scan.keyframe_insert(data_path="location", frame=1)
scan.location = (0, 1.1, 0.01)
scan.keyframe_insert(data_path="location", frame=25)  # frame 25 = frame 1 for loop

# Linear interpolation
for action in bpy.data.actions:
    for layer in action.layers:
        for strip in layer.strips:
            for cbag in strip.channelbags:
                for fc in cbag.fcurves:
                    for kp in fc.keyframe_points:
                        kp.interpolation = 'LINEAR'

# Ortho camera
bpy.ops.object.camera_add(location=(0, 0, 2))
cam = bpy.context.active_object
cam.data.type = 'ORTHO'
cam.data.ortho_scale = 2.0
scene.camera = cam

# Transparent world
scene.world = bpy.data.worlds.new("W")
scene.world.use_nodes = True
bg = scene.world.node_tree.nodes['Background']
bg.inputs['Strength'].default_value = 0.0

save_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "output", "holo_grid.blend")
bpy.ops.wm.save_as_mainfile(filepath=save_path)
print("## OUTPUT ##")
print(f"Saved: {save_path}")
