"""Create energy_burst.blend - vivid energy explosion for phase transitions."""
import bpy
import math
import os

# Clear scene
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete()

scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 16

# --- Central burst sphere ---
bpy.ops.mesh.primitive_uv_sphere_add(radius=0.3, segments=32, ring_count=16)
sphere = bpy.context.active_object
sphere.name = "BurstCore"

mat_core = bpy.data.materials.new(name="CoreMat")
mat_core.use_nodes = True
nt = mat_core.node_tree
nt.nodes.clear()
out_n = nt.nodes.new('ShaderNodeOutputMaterial')
emit_n = nt.nodes.new('ShaderNodeEmission')
nt.links.new(emit_n.outputs[0], out_n.inputs[0])

# Animate scale: rapid expansion
sphere.scale = (0.2, 0.2, 0.2)
sphere.keyframe_insert(data_path="scale", frame=1)
sphere.scale = (1.0, 1.0, 1.0)
sphere.keyframe_insert(data_path="scale", frame=3)
sphere.scale = (2.5, 2.5, 2.5)
sphere.keyframe_insert(data_path="scale", frame=16)

# Animate emission: intense white -> cyan -> fade out
emit_n.inputs['Color'].default_value = (1.0, 1.0, 1.0, 1.0)
emit_n.inputs['Color'].keyframe_insert(data_path="default_value", frame=1)
emit_n.inputs['Color'].default_value = (0.0, 0.94, 1.0, 1.0)
emit_n.inputs['Color'].keyframe_insert(data_path="default_value", frame=5)
emit_n.inputs['Color'].default_value = (0.12, 0.56, 1.0, 0.0)
emit_n.inputs['Color'].keyframe_insert(data_path="default_value", frame=16)

emit_n.inputs['Strength'].default_value = 30.0  # Very bright start
emit_n.inputs['Strength'].keyframe_insert(data_path="default_value", frame=1)
emit_n.inputs['Strength'].default_value = 15.0
emit_n.inputs['Strength'].keyframe_insert(data_path="default_value", frame=4)
emit_n.inputs['Strength'].default_value = 0.0
emit_n.inputs['Strength'].keyframe_insert(data_path="default_value", frame=16)

sphere.data.materials.append(mat_core)

# --- Expanding ring ---
bpy.ops.mesh.primitive_torus_add(
    major_radius=0.5, minor_radius=0.06,
    major_segments=64, minor_segments=12
)
ring = bpy.context.active_object
ring.name = "BurstRing"

mat_ring = bpy.data.materials.new(name="RingMat")
mat_ring.use_nodes = True
rnt = mat_ring.node_tree
rnt.nodes.clear()
r_out = rnt.nodes.new('ShaderNodeOutputMaterial')
r_emit = rnt.nodes.new('ShaderNodeEmission')
r_emit.inputs['Color'].default_value = (0.0, 0.94, 1.0, 1.0)
rnt.links.new(r_emit.outputs[0], r_out.inputs[0])

ring.scale = (0.5, 0.5, 0.5)
ring.keyframe_insert(data_path="scale", frame=1)
ring.scale = (2.0, 2.0, 0.3)
ring.keyframe_insert(data_path="scale", frame=8)
ring.scale = (4.0, 4.0, 0.1)
ring.keyframe_insert(data_path="scale", frame=16)

r_emit.inputs['Strength'].default_value = 25.0
r_emit.inputs['Strength'].keyframe_insert(data_path="default_value", frame=1)
r_emit.inputs['Strength'].default_value = 10.0
r_emit.inputs['Strength'].keyframe_insert(data_path="default_value", frame=6)
r_emit.inputs['Strength'].default_value = 0.0
r_emit.inputs['Strength'].keyframe_insert(data_path="default_value", frame=14)

ring.data.materials.append(mat_ring)

# --- Second ring (purple, delayed) ---
bpy.ops.mesh.primitive_torus_add(
    major_radius=0.4, minor_radius=0.04,
    major_segments=64, minor_segments=12
)
ring2 = bpy.context.active_object
ring2.name = "BurstRing2"

mat_ring2 = bpy.data.materials.new(name="Ring2Mat")
mat_ring2.use_nodes = True
r2nt = mat_ring2.node_tree
r2nt.nodes.clear()
r2_out = r2nt.nodes.new('ShaderNodeOutputMaterial')
r2_emit = r2nt.nodes.new('ShaderNodeEmission')
r2_emit.inputs['Color'].default_value = (0.55, 0.36, 0.96, 1.0)  # HoloPurple
r2nt.links.new(r2_emit.outputs[0], r2_out.inputs[0])

ring2.scale = (0.1, 0.1, 0.1)
ring2.keyframe_insert(data_path="scale", frame=1)
ring2.scale = (0.3, 0.3, 0.3)
ring2.keyframe_insert(data_path="scale", frame=3)
ring2.scale = (3.0, 3.0, 0.2)
ring2.keyframe_insert(data_path="scale", frame=12)
ring2.scale = (5.0, 5.0, 0.05)
ring2.keyframe_insert(data_path="scale", frame=16)

r2_emit.inputs['Strength'].default_value = 0.0
r2_emit.inputs['Strength'].keyframe_insert(data_path="default_value", frame=1)
r2_emit.inputs['Strength'].default_value = 20.0
r2_emit.inputs['Strength'].keyframe_insert(data_path="default_value", frame=3)
r2_emit.inputs['Strength'].default_value = 0.0
r2_emit.inputs['Strength'].keyframe_insert(data_path="default_value", frame=14)

ring2.data.materials.append(mat_ring2)

# Ortho camera
bpy.ops.object.camera_add(location=(0, 0, 5))
cam = bpy.context.active_object
cam.data.type = 'ORTHO'
cam.data.ortho_scale = 3.0  # Tighter view for more visible burst
scene.camera = cam

# Transparent world (this is an overlay effect)
scene.world = bpy.data.worlds.new("W")
scene.world.use_nodes = True
bg = scene.world.node_tree.nodes['Background']
bg.inputs['Strength'].default_value = 0.0

save_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "output", "energy_burst.blend")
bpy.ops.wm.save_as_mainfile(filepath=save_path)
print("## OUTPUT ##")
print(f"Saved: {save_path}")
