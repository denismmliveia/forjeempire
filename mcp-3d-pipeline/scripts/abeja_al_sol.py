"""Blender script: Create a bee in the sun scene with phase-tagged objects.

Naming convention:
  {Model}_{Part}_p{MinPhase}  — mesh objects, visible from phase N onward
  ENV_{Name}                  — environment objects, always visible
"""
import bpy
import math
from mathutils import Vector

# Clear scene
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete()
for mesh in bpy.data.meshes:
    bpy.data.meshes.remove(mesh)
for mat in bpy.data.materials:
    bpy.data.materials.remove(mat)

def make_mat(name, color, metallic=0.0, roughness=0.5, emission=0.0):
    mat = bpy.data.materials.new(name)
    mat.use_nodes = True
    bsdf = mat.node_tree.nodes["Principled BSDF"]
    bsdf.inputs["Base Color"].default_value = color
    bsdf.inputs["Metallic"].default_value = metallic
    bsdf.inputs["Roughness"].default_value = roughness
    if emission > 0:
        bsdf.inputs["Emission Color"].default_value = color
        bsdf.inputs["Emission Strength"].default_value = emission
    return mat

mat_yellow = make_mat("BeeYellow", (1.0, 0.75, 0.0, 1), roughness=0.4)
mat_black = make_mat("BeeBlack", (0.02, 0.02, 0.02, 1), roughness=0.3)
mat_wing = make_mat("BeeWing", (0.8, 0.9, 1.0, 0.3), roughness=0.1, metallic=0.2)
mat_eye = make_mat("BeeEye", (0.01, 0.01, 0.05, 1), metallic=0.8, roughness=0.1)
mat_sun = make_mat("Sun", (1.0, 0.9, 0.3, 1), emission=15.0)
mat_ground = make_mat("Ground", (0.15, 0.5, 0.1, 1), roughness=0.9)
mat_petal = make_mat("Petal", (0.9, 0.2, 0.5, 1), roughness=0.6)
mat_fcenter = make_mat("FlowerCenter", (0.8, 0.6, 0.0, 1), roughness=0.8)
mat_stem = make_mat("Stem", (0.1, 0.35, 0.05, 1), roughness=0.8)

# ═══════════════════════════════════════
# PHASE 1: Core body silhouette
# ═══════════════════════════════════════

# Thorax
bpy.ops.mesh.primitive_uv_sphere_add(radius=0.35, location=(0, 0, 1.5), segments=32, ring_count=16)
thorax = bpy.context.active_object
thorax.name = "Bee_Thorax_p1"
thorax.scale = (1.0, 0.8, 0.75)
bpy.ops.object.transform_apply(scale=True)
thorax.data.materials.append(mat_yellow)

# Abdomen
bpy.ops.mesh.primitive_uv_sphere_add(radius=0.45, location=(-0.55, 0, 1.4), segments=32, ring_count=16)
abdomen = bpy.context.active_object
abdomen.name = "Bee_Abdomen_p1"
abdomen.scale = (1.4, 0.8, 0.7)
bpy.ops.object.transform_apply(scale=True)
abdomen.data.materials.append(mat_yellow)

# ═══════════════════════════════════════
# PHASE 2: Head + Legs (insect shape)
# ═══════════════════════════════════════

# Head
bpy.ops.mesh.primitive_uv_sphere_add(radius=0.25, location=(0.4, 0, 1.55), segments=32, ring_count=16)
head = bpy.context.active_object
head.name = "Bee_Head_p2"
head.scale = (0.9, 0.85, 0.85)
bpy.ops.object.transform_apply(scale=True)
head.data.materials.append(mat_black)

# Legs
leg_positions = [
    (0.15, 0.28, 1.2), (0.15, -0.28, 1.2),
    (-0.1, 0.3, 1.2), (-0.1, -0.3, 1.2),
    (-0.35, 0.25, 1.25), (-0.35, -0.25, 1.25),
]
for i, pos in enumerate(leg_positions):
    side = 1 if pos[1] > 0 else -1
    bpy.ops.mesh.primitive_cylinder_add(radius=0.02, depth=0.5, location=pos)
    leg = bpy.context.active_object
    leg.name = f"Bee_Leg{i}_p2"
    leg.rotation_euler = (side * 0.8, 0.3 - (i // 2) * 0.15, 0)
    leg.data.materials.append(mat_black)

# ═══════════════════════════════════════
# PHASE 3: Wings (flying bee)
# ═══════════════════════════════════════

for side in [1, -1]:
    label = "R" if side > 0 else "L"

    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.5, location=(0.1, side * 0.5, 1.85), segments=16, ring_count=8)
    w = bpy.context.active_object
    w.name = f"Bee_Wing{label}_p3"
    w.scale = (1.2, 0.15, 0.5)
    w.rotation_euler = (side * 0.15, 0.2, side * 0.3)
    bpy.ops.object.transform_apply(scale=True)
    w.data.materials.append(mat_wing)

    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.35, location=(-0.1, side * 0.4, 1.8), segments=16, ring_count=8)
    w2 = bpy.context.active_object
    w2.name = f"Bee_Wing{label}2_p3"
    w2.scale = (0.9, 0.12, 0.4)
    w2.rotation_euler = (side * 0.1, 0.15, side * 0.25)
    bpy.ops.object.transform_apply(scale=True)
    w2.data.materials.append(mat_wing)

# ═══════════════════════════════════════
# PHASE 4: Eyes, stinger, flower base
# ═══════════════════════════════════════

# Eyes
for i, side in enumerate([1, -1]):
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.1, location=(0.52, side * 0.15, 1.6), segments=16, ring_count=8)
    eye = bpy.context.active_object
    eye.name = f"Bee_Eye{i}_p4"
    eye.scale = (0.8, 0.7, 0.9)
    bpy.ops.object.transform_apply(scale=True)
    eye.data.materials.append(mat_eye)

# Stinger
bpy.ops.mesh.primitive_cone_add(radius1=0.05, radius2=0.0, depth=0.2, location=(-1.15, 0, 1.35))
stinger = bpy.context.active_object
stinger.name = "Bee_Stinger_p4"
stinger.rotation_euler = (0, math.radians(90), 0)
stinger.data.materials.append(mat_black)

# Flower stem
bpy.ops.mesh.primitive_cylinder_add(radius=0.05, depth=1.5, location=(0.3, 0, 0.4))
fstem = bpy.context.active_object
fstem.name = "Flower_Stem_p4"
fstem.data.materials.append(mat_stem)

# Flower center
bpy.ops.mesh.primitive_uv_sphere_add(radius=0.2, location=(0.3, 0, 1.1), segments=16, ring_count=8)
fc = bpy.context.active_object
fc.name = "Flower_Center_p4"
fc.scale = (1, 1, 0.5)
bpy.ops.object.transform_apply(scale=True)
fc.data.materials.append(mat_fcenter)

# ═══════════════════════════════════════
# PHASE 5: Stripes + petals (decoration)
# ═══════════════════════════════════════

# Stripes
for i, xp in enumerate([-0.35, -0.55, -0.75]):
    bpy.ops.mesh.primitive_torus_add(
        major_radius=0.28 - i * 0.03,
        minor_radius=0.06,
        location=(xp, 0, 1.4),
        rotation=(0, math.radians(90), 0),
    )
    s = bpy.context.active_object
    s.name = f"Bee_Stripe{i}_p5"
    s.scale = (1, 0.8, 0.7)
    bpy.ops.object.transform_apply(scale=True)
    s.data.materials.append(mat_black)

# Petals
for i in range(8):
    angle = i * (2 * math.pi / 8)
    px = 0.3 + math.cos(angle) * 0.35
    py = math.sin(angle) * 0.35
    bpy.ops.mesh.primitive_uv_sphere_add(
        radius=0.18, location=(px, py, 1.08), segments=12, ring_count=6
    )
    p = bpy.context.active_object
    p.name = f"Flower_Petal{i}_p5"
    p.scale = (0.8, 0.5, 0.2)
    p.rotation_euler = (0, 0, angle)
    bpy.ops.object.transform_apply(scale=True)
    p.data.materials.append(mat_petal)

# ═══════════════════════════════════════
# PHASE 6: Antennae + leaves (final details)
# ═══════════════════════════════════════

# Antennae
for i, side in enumerate([1, -1]):
    bpy.ops.mesh.primitive_cylinder_add(radius=0.015, depth=0.4, location=(0.55, side * 0.08, 1.8))
    ant = bpy.context.active_object
    ant.name = f"Bee_Antenna{i}_p6"
    ant.rotation_euler = (0.3 * side, 0.5, 0.2 * side)
    ant.data.materials.append(mat_black)

    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.03, location=(0.65, side * 0.2, 2.0))
    tip = bpy.context.active_object
    tip.name = f"Bee_AntTip{i}_p6"
    tip.data.materials.append(mat_black)

# Leaves
for i, (side_x, rot) in enumerate([(0.6, 0.5), (-0.3, -0.7)]):
    bpy.ops.mesh.primitive_uv_sphere_add(
        radius=0.2, location=(0.3 + side_x * 0.15, side_x * 0.1, 0.45), segments=8, ring_count=4
    )
    leaf = bpy.context.active_object
    leaf.name = f"Flower_Leaf{i}_p6"
    leaf.scale = (1.5, 0.6, 0.15)
    leaf.rotation_euler = (0, 0, rot)
    bpy.ops.object.transform_apply(scale=True)
    leaf.data.materials.append(mat_stem)

# ═══════════════════════════════════════
# ENVIRONMENT (always visible, ENV_ prefix)
# ═══════════════════════════════════════

# Ground
bpy.ops.mesh.primitive_plane_add(size=20, location=(0, 0, -0.35))
bpy.context.active_object.name = "ENV_Ground"
bpy.context.active_object.data.materials.append(mat_ground)

# Sun sphere
bpy.ops.mesh.primitive_uv_sphere_add(radius=2.0, location=(8, 3, 8), segments=32, ring_count=16)
bpy.context.active_object.name = "ENV_Sun"
bpy.context.active_object.data.materials.append(mat_sun)

# Lights
bpy.ops.object.light_add(type='SUN', location=(5, 3, 8))
sl = bpy.context.active_object
sl.name = "ENV_SunLight"
sl.data.energy = 5
sl.data.color = (1.0, 0.95, 0.8)
sl.rotation_euler = (math.radians(35), math.radians(15), math.radians(-20))

bpy.ops.object.light_add(type='AREA', location=(-3, -2, 3))
fl = bpy.context.active_object
fl.name = "ENV_FillLight"
fl.data.energy = 100
fl.data.color = (1.0, 0.85, 0.6)
fl.data.size = 3

bpy.ops.object.light_add(type='POINT', location=(-2, 2, 3))
rl = bpy.context.active_object
rl.name = "ENV_RimLight"
rl.data.energy = 80
rl.data.color = (0.9, 0.95, 1.0)

# Camera
bpy.ops.object.camera_add(location=(3.5, -2.5, 2.5))
cam = bpy.context.active_object
cam.name = "ENV_Camera"
direction = Vector((0, 0, 1.5)) - cam.location
rot_quat = direction.to_track_quat('-Z', 'Y')
cam.rotation_euler = rot_quat.to_euler()
cam.data.lens = 60
bpy.context.scene.camera = cam

# World (sky)
world = bpy.data.worlds.get("World") or bpy.data.worlds.new("World")
bpy.context.scene.world = world
world.use_nodes = True
tree = world.node_tree
tree.nodes.clear()
bg = tree.nodes.new('ShaderNodeBackground')
bg.inputs[0].default_value = (0.4, 0.7, 1.0, 1.0)
bg.inputs[1].default_value = 1.0
out = tree.nodes.new('ShaderNodeOutputWorld')
tree.links.new(bg.outputs[0], out.inputs[0])

# ═══════════════════════════════════════
# RENDER SETTINGS
# ═══════════════════════════════════════
scene = bpy.context.scene
scene.render.engine = 'CYCLES'
scene.cycles.samples = 128
scene.cycles.device = 'GPU'
scene.render.resolution_x = 1920
scene.render.resolution_y = 1080
prefs = bpy.context.preferences.addons['cycles'].preferences
prefs.compute_device_type = 'CUDA'
prefs.get_devices()
for d in prefs.devices:
    d.use = d.type != 'CPU'

# Smooth shading on bee parts
for obj in bpy.data.objects:
    if "_p" in obj.name and obj.type == 'MESH':
        bpy.context.view_layer.objects.active = obj
        obj.select_set(True)
        bpy.ops.object.shade_smooth()
        obj.select_set(False)

# Save
output_path = r"C:\Users\denis\Documents\Proyectos\3_TapGames\mcp-3d-pipeline\output\abeja_al_sol.blend"
bpy.ops.wm.save_as_mainfile(filepath=output_path)

total = len([o for o in bpy.data.objects if o.type == 'MESH'])
verts = sum(len(o.data.vertices) for o in bpy.data.objects if o.type == 'MESH')
print("## OUTPUT ##")
print(f"Scene created: {total} meshes, {verts} vertices")
print(f"Saved: {output_path}")
