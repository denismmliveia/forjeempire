import bpy
import math

bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete(use_global=False)

# --- Phase 1: Torso (core body) ---
bpy.ops.mesh.primitive_cube_add(size=1, location=(0, 0, 1.2))
torso = bpy.context.active_object
torso.name = "Soldier_Torso_p1"
torso.scale = (0.5, 0.3, 0.6)
bpy.ops.object.transform_apply(scale=True)

mat_armor = bpy.data.materials.new("Armor_Blue")
mat_armor.use_nodes = True
bsdf = mat_armor.node_tree.nodes["Principled BSDF"]
bsdf.inputs["Base Color"].default_value = (0.1, 0.15, 0.35, 1.0)
bsdf.inputs["Metallic"].default_value = 0.8
bsdf.inputs["Roughness"].default_value = 0.3
torso.data.materials.append(mat_armor)

# Waist
bpy.ops.mesh.primitive_cube_add(size=1, location=(0, 0, 0.7))
waist = bpy.context.active_object
waist.name = "Soldier_Waist_p1"
waist.scale = (0.4, 0.25, 0.2)
bpy.ops.object.transform_apply(scale=True)
waist.data.materials.append(mat_armor)

# --- Phase 2: Head and helmet ---
mat_visor = bpy.data.materials.new("Visor_Cyan")
mat_visor.use_nodes = True
bsdf_v = mat_visor.node_tree.nodes["Principled BSDF"]
bsdf_v.inputs["Base Color"].default_value = (0.0, 0.8, 1.0, 1.0)
bsdf_v.inputs["Emission Color"].default_value = (0.0, 0.6, 1.0, 1.0)
bsdf_v.inputs["Emission Strength"].default_value = 2.0
bsdf_v.inputs["Metallic"].default_value = 0.9

# Head
bpy.ops.mesh.primitive_uv_sphere_add(radius=0.22, location=(0, 0, 1.75))
head = bpy.context.active_object
head.name = "Soldier_Head_p2"
head.data.materials.append(mat_armor)

# Helmet
bpy.ops.mesh.primitive_uv_sphere_add(radius=0.26, location=(0, 0, 1.8))
helmet = bpy.context.active_object
helmet.name = "Soldier_Helmet_p2"
helmet.scale = (1.0, 0.9, 0.85)
bpy.ops.object.transform_apply(scale=True)

mat_helmet = bpy.data.materials.new("Helmet_Dark")
mat_helmet.use_nodes = True
bsdf_h = mat_helmet.node_tree.nodes["Principled BSDF"]
bsdf_h.inputs["Base Color"].default_value = (0.08, 0.1, 0.2, 1.0)
bsdf_h.inputs["Metallic"].default_value = 0.9
bsdf_h.inputs["Roughness"].default_value = 0.2
helmet.data.materials.append(mat_helmet)

# Visor
bpy.ops.mesh.primitive_cube_add(size=1, location=(0, -0.22, 1.75))
visor = bpy.context.active_object
visor.name = "Soldier_Visor_p2"
visor.scale = (0.35, 0.05, 0.1)
bpy.ops.object.transform_apply(scale=True)
visor.data.materials.append(mat_visor)

# --- Phase 3: Legs ---
mat_leg = bpy.data.materials.new("Leg_Armor")
mat_leg.use_nodes = True
bsdf_l = mat_leg.node_tree.nodes["Principled BSDF"]
bsdf_l.inputs["Base Color"].default_value = (0.08, 0.1, 0.25, 1.0)
bsdf_l.inputs["Metallic"].default_value = 0.7
bsdf_l.inputs["Roughness"].default_value = 0.35

for x_off in [-0.18, 0.18]:
    # Upper leg
    bpy.ops.mesh.primitive_cube_add(size=1, location=(x_off, 0, 0.4))
    uleg = bpy.context.active_object
    uleg.name = f"Soldier_UpperLeg_p3"
    uleg.scale = (0.14, 0.16, 0.3)
    bpy.ops.object.transform_apply(scale=True)
    uleg.data.materials.append(mat_leg)

    # Lower leg
    bpy.ops.mesh.primitive_cube_add(size=1, location=(x_off, 0, 0.1))
    lleg = bpy.context.active_object
    lleg.name = f"Soldier_LowerLeg_p3"
    lleg.scale = (0.12, 0.14, 0.25)
    bpy.ops.object.transform_apply(scale=True)
    lleg.data.materials.append(mat_leg)

    # Boot
    bpy.ops.mesh.primitive_cube_add(size=1, location=(x_off, -0.05, -0.05))
    boot = bpy.context.active_object
    boot.name = f"Soldier_Boot_p3"
    boot.scale = (0.14, 0.2, 0.08)
    bpy.ops.object.transform_apply(scale=True)
    boot.data.materials.append(mat_armor)

# --- Phase 4: Arms and shoulders ---
mat_shoulder = bpy.data.materials.new("Shoulder_Plate")
mat_shoulder.use_nodes = True
bsdf_s = mat_shoulder.node_tree.nodes["Principled BSDF"]
bsdf_s.inputs["Base Color"].default_value = (0.12, 0.15, 0.4, 1.0)
bsdf_s.inputs["Metallic"].default_value = 0.85
bsdf_s.inputs["Roughness"].default_value = 0.2

for x_off in [-0.45, 0.45]:
    # Shoulder pad
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.15, location=(x_off, 0, 1.45))
    sp = bpy.context.active_object
    sp.name = "Soldier_Shoulder_p4"
    sp.scale = (1.0, 0.8, 0.7)
    bpy.ops.object.transform_apply(scale=True)
    sp.data.materials.append(mat_shoulder)

    # Upper arm
    bpy.ops.mesh.primitive_cube_add(size=1, location=(x_off, 0, 1.15))
    ua = bpy.context.active_object
    ua.name = "Soldier_UpperArm_p4"
    ua.scale = (0.1, 0.12, 0.25)
    bpy.ops.object.transform_apply(scale=True)
    ua.data.materials.append(mat_armor)

    # Forearm
    bpy.ops.mesh.primitive_cube_add(size=1, location=(x_off, 0, 0.85))
    fa = bpy.context.active_object
    fa.name = "Soldier_Forearm_p4"
    fa.scale = (0.09, 0.11, 0.22)
    bpy.ops.object.transform_apply(scale=True)
    fa.data.materials.append(mat_leg)

    # Hand
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.07, location=(x_off, 0, 0.68))
    hand = bpy.context.active_object
    hand.name = "Soldier_Hand_p4"
    hand.data.materials.append(mat_armor)

# --- Phase 5: Weapon (plasma rifle) ---
mat_weapon = bpy.data.materials.new("Weapon_Gray")
mat_weapon.use_nodes = True
bsdf_wp = mat_weapon.node_tree.nodes["Principled BSDF"]
bsdf_wp.inputs["Base Color"].default_value = (0.3, 0.3, 0.32, 1.0)
bsdf_wp.inputs["Metallic"].default_value = 0.9
bsdf_wp.inputs["Roughness"].default_value = 0.2

# Rifle body
bpy.ops.mesh.primitive_cube_add(size=1, location=(0.45, -0.25, 0.9))
rifle = bpy.context.active_object
rifle.name = "Soldier_Rifle_p5"
rifle.scale = (0.06, 0.5, 0.08)
bpy.ops.object.transform_apply(scale=True)
rifle.data.materials.append(mat_weapon)

# Rifle barrel
bpy.ops.mesh.primitive_cylinder_add(radius=0.03, depth=0.3, location=(0.45, -0.55, 0.9), rotation=(math.pi/2, 0, 0))
barrel = bpy.context.active_object
barrel.name = "Soldier_Barrel_p5"
barrel.data.materials.append(mat_weapon)

# Energy core glow
mat_glow = bpy.data.materials.new("Energy_Glow")
mat_glow.use_nodes = True
bsdf_gl = mat_glow.node_tree.nodes["Principled BSDF"]
bsdf_gl.inputs["Base Color"].default_value = (0.0, 1.0, 0.8, 1.0)
bsdf_gl.inputs["Emission Color"].default_value = (0.0, 1.0, 0.8, 1.0)
bsdf_gl.inputs["Emission Strength"].default_value = 5.0

bpy.ops.mesh.primitive_uv_sphere_add(radius=0.04, location=(0.45, -0.3, 0.9))
core = bpy.context.active_object
core.name = "Soldier_WeaponCore_p5"
core.data.materials.append(mat_glow)

# --- Phase 6: Details (backpack, belt, antenna, knee pads) ---
# Backpack
bpy.ops.mesh.primitive_cube_add(size=1, location=(0, 0.3, 1.25))
pack = bpy.context.active_object
pack.name = "Soldier_Backpack_p6"
pack.scale = (0.3, 0.15, 0.4)
bpy.ops.object.transform_apply(scale=True)
pack.data.materials.append(mat_armor)

# Antenna
bpy.ops.mesh.primitive_cylinder_add(radius=0.01, depth=0.5, location=(0.12, 0.35, 1.7))
antenna = bpy.context.active_object
antenna.name = "Soldier_Antenna_p6"
antenna.data.materials.append(mat_weapon)

# Antenna tip glow
bpy.ops.mesh.primitive_uv_sphere_add(radius=0.025, location=(0.12, 0.35, 1.95))
tip = bpy.context.active_object
tip.name = "Soldier_AntennaTip_p6"
tip.data.materials.append(mat_glow)

# Belt
bpy.ops.mesh.primitive_torus_add(major_radius=0.35, minor_radius=0.03, location=(0, 0, 0.65), rotation=(0, 0, 0))
belt = bpy.context.active_object
belt.name = "Soldier_Belt_p6"
belt.scale = (1.0, 0.7, 1.0)
bpy.ops.object.transform_apply(scale=True)
belt.data.materials.append(mat_weapon)

# Knee pads
for x_off in [-0.18, 0.18]:
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.06, location=(x_off, -0.14, 0.3))
    kp = bpy.context.active_object
    kp.name = "Soldier_KneePad_p6"
    kp.scale = (1.0, 0.5, 1.2)
    bpy.ops.object.transform_apply(scale=True)
    kp.data.materials.append(mat_shoulder)

# --- Environment ---
bpy.ops.object.light_add(type='SUN', location=(5, -5, 10))
sun = bpy.context.active_object
sun.name = "ENV_Sun"
sun.data.energy = 3.0

bpy.ops.object.light_add(type='AREA', location=(-3, 3, 5))
fill = bpy.context.active_object
fill.name = "ENV_Fill"
fill.data.energy = 50.0

bpy.ops.mesh.primitive_plane_add(size=20, location=(0, 0, -0.05))
ground = bpy.context.active_object
ground.name = "ENV_Ground"
mat_gnd = bpy.data.materials.new("Ground")
mat_gnd.use_nodes = True
bsdf_gnd = mat_gnd.node_tree.nodes["Principled BSDF"]
bsdf_gnd.inputs["Base Color"].default_value = (0.12, 0.12, 0.15, 1.0)
bsdf_gnd.inputs["Roughness"].default_value = 0.9
ground.data.materials.append(mat_gnd)

bpy.ops.object.camera_add(location=(4, -3.5, 2.5))
cam = bpy.context.active_object
cam.name = "ENV_Camera"
cam.rotation_euler = (math.radians(70), 0, math.radians(50))
bpy.context.scene.camera = cam

output_path = r"C:/Users/denis/Documents/Proyectos/3_TapGames/mcp-3d-pipeline/output/soldado_futuro.blend"
bpy.ops.wm.save_as_mainfile(filepath=output_path)
print("## OUTPUT ##")
print(f"Saved: {output_path}")
print("Objects:", [o.name for o in bpy.data.objects])
