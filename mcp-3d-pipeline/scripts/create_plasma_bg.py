"""Create plasma_bg.blend - vivid animated plasma/nebula background texture."""
import bpy
import os

# Clear scene
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete()

# Plane
bpy.ops.mesh.primitive_plane_add(size=2)
plane = bpy.context.active_object

# Material with vivid neon colors
mat = bpy.data.materials.new(name="Plasma")
mat.use_nodes = True
nt = mat.node_tree
nt.nodes.clear()

out_node = nt.nodes.new('ShaderNodeOutputMaterial')
out_node.location = (1000, 0)

emit = nt.nodes.new('ShaderNodeEmission')
emit.location = (800, 0)
emit.inputs['Strength'].default_value = 1.2  # Moderate - Standard color mgmt doesn't need high values
nt.links.new(emit.outputs[0], out_node.inputs[0])

# Mix two noise layers for richer texture
mix = nt.nodes.new('ShaderNodeMix')
mix.data_type = 'RGBA'
mix.location = (600, 0)
mix.inputs['Factor'].default_value = 0.5
nt.links.new(mix.outputs['Result'], emit.inputs['Color'])

# Layer 1: Large flowing noise
noise1 = nt.nodes.new('ShaderNodeTexNoise')
noise1.location = (-200, 200)
noise1.noise_dimensions = '4D'
noise1.inputs['Scale'].default_value = 2.5
noise1.inputs['Detail'].default_value = 8.0
noise1.inputs['Roughness'].default_value = 0.6
noise1.inputs['Distortion'].default_value = 3.0

ramp1 = nt.nodes.new('ShaderNodeValToRGB')
ramp1.location = (200, 200)
# Dark dominant with blue-cyan highlights (cool tech aura)
ramp1.color_ramp.elements[0].position = 0.0
ramp1.color_ramp.elements[0].color = (0.0, 0.01, 0.03, 1.0)  # Near black
ramp1.color_ramp.elements[1].position = 0.5
ramp1.color_ramp.elements[1].color = (0.0, 0.05, 0.15, 1.0)  # Very dark blue
e1 = ramp1.color_ramp.elements.new(0.7)
e1.color = (0.0, 0.2, 0.5, 1.0)  # Mid blue
e2 = ramp1.color_ramp.elements.new(0.9)
e2.color = (0.0, 0.7, 0.9, 1.0)  # Bright cyan (only peaks)

nt.links.new(noise1.outputs['Fac'], ramp1.inputs['Fac'])
nt.links.new(ramp1.outputs['Color'], mix.inputs['A'])

# Layer 2: Voronoi cells for energy feel
voronoi = nt.nodes.new('ShaderNodeTexVoronoi')
voronoi.location = (-200, -150)
voronoi.voronoi_dimensions = '4D'
voronoi.inputs['Scale'].default_value = 4.0
voronoi.inputs['Randomness'].default_value = 1.0

ramp2 = nt.nodes.new('ShaderNodeValToRGB')
ramp2.location = (200, -150)
ramp2.color_ramp.elements[0].position = 0.0
ramp2.color_ramp.elements[0].color = (0.0, 0.01, 0.04, 1.0)  # Near black
ramp2.color_ramp.elements[1].position = 0.6
ramp2.color_ramp.elements[1].color = (0.0, 0.1, 0.3, 1.0)  # Dark blue
e3 = ramp2.color_ramp.elements.new(0.85)
e3.color = (0.0, 0.5, 0.8, 1.0)  # Electric blue accent

nt.links.new(voronoi.outputs['Distance'], ramp2.inputs['Fac'])
nt.links.new(ramp2.outputs['Color'], mix.inputs['B'])

plane.data.materials.append(mat)

# Animate W: 0 -> 2 over 32 frames for seamless loop
scene = bpy.context.scene
scene.frame_start = 1
scene.frame_end = 32

noise1.inputs['W'].default_value = 0.0
noise1.inputs['W'].keyframe_insert(data_path="default_value", frame=1)
noise1.inputs['W'].default_value = 2.0
noise1.inputs['W'].keyframe_insert(data_path="default_value", frame=33)

voronoi.inputs['W'].default_value = 0.0
voronoi.inputs['W'].keyframe_insert(data_path="default_value", frame=1)
voronoi.inputs['W'].default_value = 1.4
voronoi.inputs['W'].keyframe_insert(data_path="default_value", frame=33)

# Blender 5.1: set linear interpolation via layers/strips/channelbags
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

# Black world (NOT transparent - we want the dark background baked in)
scene.world = bpy.data.worlds.new("W")
scene.world.use_nodes = True
bg = scene.world.node_tree.nodes['Background']
bg.inputs['Color'].default_value = (0.0, 0.0, 0.02, 1.0)
bg.inputs['Strength'].default_value = 1.0

save_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "output", "plasma_bg.blend")
bpy.ops.wm.save_as_mainfile(filepath=save_path)
print("## OUTPUT ##")
print(f"Saved: {save_path}")
