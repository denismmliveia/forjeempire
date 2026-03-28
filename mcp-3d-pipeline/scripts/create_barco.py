import bpy
import math

bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete(use_global=False)

# --- Phase 1: Hull ---
bpy.ops.mesh.primitive_cube_add(size=1, location=(0, 0, 0.3))
hull = bpy.context.active_object
hull.name = "Ship_Hull_p1"
hull.scale = (2.5, 0.8, 0.4)
bpy.ops.object.transform_apply(scale=True)

# Taper the hull at bow and stern using edit mode
bpy.ops.object.mode_set(mode='EDIT')
bpy.ops.mesh.select_all(action='DESELECT')
bpy.ops.object.mode_set(mode='OBJECT')

mat_hull = bpy.data.materials.new("Hull_Wood")
mat_hull.use_nodes = True
bsdf = mat_hull.node_tree.nodes["Principled BSDF"]
bsdf.inputs["Base Color"].default_value = (0.35, 0.2, 0.08, 1.0)
bsdf.inputs["Roughness"].default_value = 0.7
hull.data.materials.append(mat_hull)

# Hull bottom keel
bpy.ops.mesh.primitive_cube_add(size=1, location=(0, 0, 0.05))
keel = bpy.context.active_object
keel.name = "Ship_Keel_p1"
keel.scale = (2.6, 0.15, 0.1)
bpy.ops.object.transform_apply(scale=True)

mat_keel = bpy.data.materials.new("Keel_Dark")
mat_keel.use_nodes = True
bsdf_k = mat_keel.node_tree.nodes["Principled BSDF"]
bsdf_k.inputs["Base Color"].default_value = (0.15, 0.08, 0.03, 1.0)
bsdf_k.inputs["Roughness"].default_value = 0.8
keel.data.materials.append(mat_keel)

# --- Phase 2: Deck ---
mat_deck = bpy.data.materials.new("Deck_Light")
mat_deck.use_nodes = True
bsdf_d = mat_deck.node_tree.nodes["Principled BSDF"]
bsdf_d.inputs["Base Color"].default_value = (0.55, 0.4, 0.2, 1.0)
bsdf_d.inputs["Roughness"].default_value = 0.6

bpy.ops.mesh.primitive_cube_add(size=1, location=(0, 0, 0.52))
deck = bpy.context.active_object
deck.name = "Ship_Deck_p2"
deck.scale = (2.3, 0.7, 0.04)
bpy.ops.object.transform_apply(scale=True)
deck.data.materials.append(mat_deck)

# Raised rear deck (poop deck)
bpy.ops.mesh.primitive_cube_add(size=1, location=(1.5, 0, 0.75))
poop = bpy.context.active_object
poop.name = "Ship_RearDeck_p2"
poop.scale = (0.6, 0.65, 0.04)
bpy.ops.object.transform_apply(scale=True)
poop.data.materials.append(mat_deck)

# Rear deck supports
for y_off in [-0.55, 0.55]:
    bpy.ops.mesh.primitive_cube_add(size=1, location=(1.5, y_off, 0.63))
    sup = bpy.context.active_object
    sup.name = "Ship_DeckSupport_p2"
    sup.scale = (0.55, 0.04, 0.2)
    bpy.ops.object.transform_apply(scale=True)
    sup.data.materials.append(mat_hull)

# --- Phase 3: Masts ---
mat_mast = bpy.data.materials.new("Mast_Wood")
mat_mast.use_nodes = True
bsdf_m = mat_mast.node_tree.nodes["Principled BSDF"]
bsdf_m.inputs["Base Color"].default_value = (0.4, 0.25, 0.1, 1.0)
bsdf_m.inputs["Roughness"].default_value = 0.75

mast_positions = [(-0.8, 0, "Fore"), (0.3, 0, "Main"), (1.2, 0, "Mizzen")]
mast_heights = [2.2, 2.8, 2.0]

for (x, y, label), height in zip(mast_positions, mast_heights):
    bpy.ops.mesh.primitive_cylinder_add(radius=0.05, depth=height, location=(x, y, 0.52 + height/2))
    mast = bpy.context.active_object
    mast.name = f"Ship_Mast{label}_p3"
    mast.data.materials.append(mat_mast)

    # Crow's nest platform on main mast
    if label == "Main":
        bpy.ops.mesh.primitive_cylinder_add(radius=0.15, depth=0.04, location=(x, y, 0.52 + height * 0.8))
        crow = bpy.context.active_object
        crow.name = "Ship_CrowsNest_p3"
        crow.data.materials.append(mat_deck)

    # Yard arms (horizontal)
    bpy.ops.mesh.primitive_cylinder_add(radius=0.025, depth=1.2, location=(x, y, 0.52 + height * 0.6), rotation=(0, math.pi/2, 0))
    yard = bpy.context.active_object
    yard.name = f"Ship_Yard{label}_p3"
    yard.rotation_euler = (math.pi/2, 0, 0)
    yard.data.materials.append(mat_mast)

# --- Phase 4: Sails ---
mat_sail = bpy.data.materials.new("Sail_Canvas")
mat_sail.use_nodes = True
bsdf_s = mat_sail.node_tree.nodes["Principled BSDF"]
bsdf_s.inputs["Base Color"].default_value = (0.9, 0.88, 0.8, 1.0)
bsdf_s.inputs["Roughness"].default_value = 0.9

sail_data = [
    (-0.8, 1.5, 0.8, 0.9),   # Fore
    (0.3, 2.0, 1.0, 1.2),    # Main (bigger)
    (1.2, 1.3, 0.7, 0.8),    # Mizzen
]

for (x, mh, w, h), label in zip(sail_data, ["Fore", "Main", "Mizzen"]):
    bpy.ops.mesh.primitive_cube_add(size=1, location=(x, 0, 0.52 + mh * 0.55))
    sail = bpy.context.active_object
    sail.name = f"Ship_Sail{label}_p4"
    sail.scale = (0.03, w/2, h/2)
    bpy.ops.object.transform_apply(scale=True)
    sail.data.materials.append(mat_sail)

# --- Phase 5: Cabin and helm ---
mat_cabin = bpy.data.materials.new("Cabin_Wood")
mat_cabin.use_nodes = True
bsdf_cb = mat_cabin.node_tree.nodes["Principled BSDF"]
bsdf_cb.inputs["Base Color"].default_value = (0.3, 0.18, 0.07, 1.0)
bsdf_cb.inputs["Roughness"].default_value = 0.65

# Captain's cabin
bpy.ops.mesh.primitive_cube_add(size=1, location=(1.5, 0, 0.95))
cabin = bpy.context.active_object
cabin.name = "Ship_Cabin_p5"
cabin.scale = (0.45, 0.5, 0.2)
bpy.ops.object.transform_apply(scale=True)
cabin.data.materials.append(mat_cabin)

# Cabin roof
bpy.ops.mesh.primitive_cube_add(size=1, location=(1.5, 0, 1.08))
cabin_roof = bpy.context.active_object
cabin_roof.name = "Ship_CabinRoof_p5"
cabin_roof.scale = (0.5, 0.55, 0.03)
bpy.ops.object.transform_apply(scale=True)
cabin_roof.data.materials.append(mat_hull)

# Helm (steering wheel)
mat_metal = bpy.data.materials.new("Metal_Brass")
mat_metal.use_nodes = True
bsdf_mt = mat_metal.node_tree.nodes["Principled BSDF"]
bsdf_mt.inputs["Base Color"].default_value = (0.7, 0.55, 0.2, 1.0)
bsdf_mt.inputs["Metallic"].default_value = 0.9
bsdf_mt.inputs["Roughness"].default_value = 0.3

bpy.ops.mesh.primitive_torus_add(major_radius=0.12, minor_radius=0.015, location=(1.1, 0, 0.85))
helm = bpy.context.active_object
helm.name = "Ship_Helm_p5"
helm.rotation_euler = (math.pi/2, 0, 0)
helm.data.materials.append(mat_metal)

# --- Phase 6: Details (figurehead, cannons, railing, flags) ---
# Bowsprit
bpy.ops.mesh.primitive_cylinder_add(radius=0.03, depth=1.0, location=(-2.2, 0, 0.6), rotation=(0, math.radians(-20), 0))
bowsprit = bpy.context.active_object
bowsprit.name = "Ship_Bowsprit_p6"
bowsprit.data.materials.append(mat_mast)

# Figurehead
mat_gold = bpy.data.materials.new("Gold")
mat_gold.use_nodes = True
bsdf_g = mat_gold.node_tree.nodes["Principled BSDF"]
bsdf_g.inputs["Base Color"].default_value = (0.85, 0.65, 0.1, 1.0)
bsdf_g.inputs["Metallic"].default_value = 1.0
bsdf_g.inputs["Roughness"].default_value = 0.2

bpy.ops.mesh.primitive_uv_sphere_add(radius=0.1, location=(-2.6, 0, 0.45))
figurehead = bpy.context.active_object
figurehead.name = "Ship_Figurehead_p6"
figurehead.scale = (1.5, 0.6, 0.8)
bpy.ops.object.transform_apply(scale=True)
figurehead.data.materials.append(mat_gold)

# Cannons (3 per side)
mat_cannon = bpy.data.materials.new("Cannon_Iron")
mat_cannon.use_nodes = True
bsdf_cn = mat_cannon.node_tree.nodes["Principled BSDF"]
bsdf_cn.inputs["Base Color"].default_value = (0.2, 0.2, 0.22, 1.0)
bsdf_cn.inputs["Metallic"].default_value = 0.8

for i in range(3):
    x = -0.5 + i * 0.7
    for y_sign in [-1, 1]:
        bpy.ops.mesh.primitive_cylinder_add(
            radius=0.04, depth=0.25,
            location=(x, y_sign * 0.7, 0.4),
            rotation=(math.pi/2, 0, 0)
        )
        cannon = bpy.context.active_object
        cannon.name = f"Ship_Cannon_p6"
        cannon.data.materials.append(mat_cannon)

# Flag on main mast
mat_flag = bpy.data.materials.new("Flag_Red")
mat_flag.use_nodes = True
bsdf_f = mat_flag.node_tree.nodes["Principled BSDF"]
bsdf_f.inputs["Base Color"].default_value = (0.8, 0.1, 0.05, 1.0)

bpy.ops.mesh.primitive_cube_add(size=1, location=(0.3, -0.2, 3.3))
flag = bpy.context.active_object
flag.name = "Ship_Flag_p6"
flag.scale = (0.01, 0.25, 0.15)
bpy.ops.object.transform_apply(scale=True)
flag.data.materials.append(mat_flag)

# Railing posts
for i in range(8):
    x = -1.8 + i * 0.5
    for y_sign in [-1, 1]:
        bpy.ops.mesh.primitive_cylinder_add(radius=0.015, depth=0.2, location=(x, y_sign * 0.68, 0.62))
        post = bpy.context.active_object
        post.name = "Ship_Railing_p6"
        post.data.materials.append(mat_mast)

# --- Environment ---
# Water plane
mat_water = bpy.data.materials.new("Water")
mat_water.use_nodes = True
bsdf_w = mat_water.node_tree.nodes["Principled BSDF"]
bsdf_w.inputs["Base Color"].default_value = (0.05, 0.15, 0.35, 1.0)
bsdf_w.inputs["Roughness"].default_value = 0.1
bsdf_w.inputs["Metallic"].default_value = 0.1

bpy.ops.mesh.primitive_plane_add(size=30, location=(0, 0, -0.1))
water = bpy.context.active_object
water.name = "ENV_Water"
water.data.materials.append(mat_water)

bpy.ops.object.light_add(type='SUN', location=(5, -5, 10))
sun = bpy.context.active_object
sun.name = "ENV_Sun"
sun.data.energy = 4.0

bpy.ops.object.light_add(type='AREA', location=(-3, 4, 5))
fill = bpy.context.active_object
fill.name = "ENV_Fill"
fill.data.energy = 60.0

bpy.ops.object.camera_add(location=(5, -5, 4))
cam = bpy.context.active_object
cam.name = "ENV_Camera"
cam.rotation_euler = (math.radians(60), 0, math.radians(45))
bpy.context.scene.camera = cam

output_path = r"C:/Users/denis/Documents/Proyectos/3_TapGames/mcp-3d-pipeline/output/barco.blend"
bpy.ops.wm.save_as_mainfile(filepath=output_path)
print("## OUTPUT ##")
print(f"Saved: {output_path}")
print("Objects:", [o.name for o in bpy.data.objects])
