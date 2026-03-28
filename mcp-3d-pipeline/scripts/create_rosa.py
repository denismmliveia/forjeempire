import bpy
import math

bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete(use_global=False)

# --- Phase 1: Stem ---
mat_stem = bpy.data.materials.new("Stem_Green")
mat_stem.use_nodes = True
bsdf = mat_stem.node_tree.nodes["Principled BSDF"]
bsdf.inputs["Base Color"].default_value = (0.1, 0.4, 0.08, 1.0)
bsdf.inputs["Roughness"].default_value = 0.6

# Main stem
bpy.ops.mesh.primitive_cylinder_add(radius=0.04, depth=2.5, location=(0, 0, 1.25))
stem = bpy.context.active_object
stem.name = "Rose_Stem_p1"
stem.data.materials.append(mat_stem)

# Slight curve with a second segment
bpy.ops.mesh.primitive_cylinder_add(radius=0.035, depth=0.5, location=(0.05, 0, 2.55), rotation=(0, math.radians(5), 0))
stem2 = bpy.context.active_object
stem2.name = "Rose_StemTop_p1"
stem2.data.materials.append(mat_stem)

# --- Phase 2: Leaves ---
mat_leaf = bpy.data.materials.new("Leaf_Green")
mat_leaf.use_nodes = True
bsdf_l = mat_leaf.node_tree.nodes["Principled BSDF"]
bsdf_l.inputs["Base Color"].default_value = (0.15, 0.5, 0.1, 1.0)
bsdf_l.inputs["Roughness"].default_value = 0.5

leaf_positions = [
    (0.0, 0, 0.8, 30, 0),
    (0.0, 0, 1.3, -25, 90),
    (0.0, 0, 1.8, 35, 180),
    (0.0, 0, 0.5, -30, 270),
]

for i, (x, y, z, tilt, rot) in enumerate(leaf_positions):
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.2, location=(x, y, z))
    leaf = bpy.context.active_object
    leaf.name = f"Rose_Leaf{i}_p2"
    leaf.scale = (0.15, 1.0, 0.05)
    bpy.ops.object.transform_apply(scale=True)
    leaf.rotation_euler = (math.radians(tilt), 0, math.radians(rot))
    leaf.location = (
        0.2 * math.cos(math.radians(rot)),
        0.2 * math.sin(math.radians(rot)),
        z
    )
    leaf.data.materials.append(mat_leaf)

    # Leaf stem
    bpy.ops.mesh.primitive_cylinder_add(
        radius=0.01, depth=0.2,
        location=(
            0.1 * math.cos(math.radians(rot)),
            0.1 * math.sin(math.radians(rot)),
            z
        ),
        rotation=(math.radians(tilt), 0, math.radians(rot))
    )
    lstem = bpy.context.active_object
    lstem.name = f"Rose_LeafStem{i}_p2"
    lstem.data.materials.append(mat_stem)

# --- Phase 3: Bud base (sepal / calyx) ---
mat_sepal = bpy.data.materials.new("Sepal_DarkGreen")
mat_sepal.use_nodes = True
bsdf_sp = mat_sepal.node_tree.nodes["Principled BSDF"]
bsdf_sp.inputs["Base Color"].default_value = (0.08, 0.3, 0.05, 1.0)
bsdf_sp.inputs["Roughness"].default_value = 0.55

# Receptacle
bpy.ops.mesh.primitive_uv_sphere_add(radius=0.15, location=(0.08, 0, 2.75))
recept = bpy.context.active_object
recept.name = "Rose_Receptacle_p3"
recept.scale = (1.0, 1.0, 0.7)
bpy.ops.object.transform_apply(scale=True)
recept.data.materials.append(mat_sepal)

# Sepals (5 pointed leaves around base of bloom)
for i in range(5):
    angle = i * 72
    x = 0.08 + 0.12 * math.cos(math.radians(angle))
    y = 0.12 * math.sin(math.radians(angle))
    bpy.ops.mesh.primitive_cone_add(radius1=0.06, depth=0.2, location=(x, y, 2.8))
    sepal = bpy.context.active_object
    sepal.name = f"Rose_Sepal{i}_p3"
    sepal.rotation_euler = (math.radians(-30 + 15 * math.cos(math.radians(angle))), 0, math.radians(angle))
    sepal.data.materials.append(mat_sepal)

# --- Phase 4: Inner petals (bud core) ---
mat_petal = bpy.data.materials.new("Petal_Red")
mat_petal.use_nodes = True
bsdf_p = mat_petal.node_tree.nodes["Principled BSDF"]
bsdf_p.inputs["Base Color"].default_value = (0.85, 0.05, 0.1, 1.0)
bsdf_p.inputs["Roughness"].default_value = 0.4
bsdf_p.inputs["Metallic"].default_value = 0.05

# Inner tight petals
for i in range(5):
    angle = i * 72 + 36  # offset from sepals
    r = 0.08
    x = 0.08 + r * math.cos(math.radians(angle))
    y = r * math.sin(math.radians(angle))
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.12, location=(x, y, 2.95))
    petal = bpy.context.active_object
    petal.name = f"Rose_InnerPetal{i}_p4"
    petal.scale = (0.3, 0.8, 1.0)
    bpy.ops.object.transform_apply(scale=True)
    petal.rotation_euler = (math.radians(20), 0, math.radians(angle))
    petal.data.materials.append(mat_petal)

# --- Phase 5: Outer petals (full bloom) ---
mat_outer = bpy.data.materials.new("Petal_DeepRed")
mat_outer.use_nodes = True
bsdf_o = mat_outer.node_tree.nodes["Principled BSDF"]
bsdf_o.inputs["Base Color"].default_value = (0.7, 0.02, 0.08, 1.0)
bsdf_o.inputs["Roughness"].default_value = 0.35

# Outer petals - larger, more open
for i in range(7):
    angle = i * (360 / 7)
    r = 0.2
    x = 0.08 + r * math.cos(math.radians(angle))
    y = r * math.sin(math.radians(angle))
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.18, location=(x, y, 2.85))
    petal = bpy.context.active_object
    petal.name = f"Rose_OuterPetal{i}_p5"
    petal.scale = (0.2, 0.9, 1.0)
    bpy.ops.object.transform_apply(scale=True)
    petal.rotation_euler = (math.radians(45), 0, math.radians(angle))
    petal.data.materials.append(mat_outer)

# Second ring even more open
for i in range(7):
    angle = i * (360 / 7) + 25
    r = 0.3
    x = 0.08 + r * math.cos(math.radians(angle))
    y = r * math.sin(math.radians(angle))
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.2, location=(x, y, 2.8))
    petal = bpy.context.active_object
    petal.name = f"Rose_OuterPetal2_{i}_p5"
    petal.scale = (0.15, 0.85, 0.9)
    bpy.ops.object.transform_apply(scale=True)
    petal.rotation_euler = (math.radians(65), 0, math.radians(angle))
    petal.data.materials.append(mat_outer)

# --- Phase 6: Details (thorns, dewdrops, pollen center) ---
# Thorns along stem
mat_thorn = bpy.data.materials.new("Thorn")
mat_thorn.use_nodes = True
bsdf_th = mat_thorn.node_tree.nodes["Principled BSDF"]
bsdf_th.inputs["Base Color"].default_value = (0.25, 0.15, 0.05, 1.0)
bsdf_th.inputs["Roughness"].default_value = 0.5

thorn_heights = [0.4, 0.7, 1.1, 1.5, 1.9, 2.2]
for i, h in enumerate(thorn_heights):
    angle = i * 60 + 30
    bpy.ops.mesh.primitive_cone_add(
        radius1=0.02, depth=0.1,
        location=(
            0.05 * math.cos(math.radians(angle)),
            0.05 * math.sin(math.radians(angle)),
            h
        ),
        rotation=(0, math.radians(60), math.radians(angle))
    )
    thorn = bpy.context.active_object
    thorn.name = f"Rose_Thorn{i}_p6"
    thorn.data.materials.append(mat_thorn)

# Dewdrops
mat_dew = bpy.data.materials.new("Dewdrop")
mat_dew.use_nodes = True
bsdf_dw = mat_dew.node_tree.nodes["Principled BSDF"]
bsdf_dw.inputs["Base Color"].default_value = (0.8, 0.9, 1.0, 1.0)
bsdf_dw.inputs["Alpha"].default_value = 0.4
bsdf_dw.inputs["Roughness"].default_value = 0.0
bsdf_dw.inputs["IOR"].default_value = 1.33
bsdf_dw.inputs["Metallic"].default_value = 0.0
mat_dew.blend_method = "BLEND" if hasattr(mat_dew, 'blend_method') else None

dew_positions = [(0.15, 0.1, 2.9), (-0.05, 0.2, 2.85), (0.2, -0.1, 2.95)]
for i, (x, y, z) in enumerate(dew_positions):
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.025, location=(x, y, z))
    dew = bpy.context.active_object
    dew.name = f"Rose_Dewdrop{i}_p6"
    dew.data.materials.append(mat_dew)

# Pollen center
mat_pollen = bpy.data.materials.new("Pollen_Gold")
mat_pollen.use_nodes = True
bsdf_po = mat_pollen.node_tree.nodes["Principled BSDF"]
bsdf_po.inputs["Base Color"].default_value = (0.9, 0.75, 0.1, 1.0)
bsdf_po.inputs["Roughness"].default_value = 0.8
bsdf_po.inputs["Emission Color"].default_value = (0.9, 0.75, 0.1, 1.0)
bsdf_po.inputs["Emission Strength"].default_value = 0.5

for i in range(8):
    angle = i * 45
    r = 0.04
    x = 0.08 + r * math.cos(math.radians(angle))
    y = r * math.sin(math.radians(angle))
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.015, location=(x, y, 3.05))
    pol = bpy.context.active_object
    pol.name = f"Rose_Pollen{i}_p6"
    pol.data.materials.append(mat_pollen)

# --- Environment ---
bpy.ops.object.light_add(type='SUN', location=(5, -3, 8))
sun = bpy.context.active_object
sun.name = "ENV_Sun"
sun.data.energy = 3.5

bpy.ops.object.light_add(type='AREA', location=(-2, 3, 4))
fill = bpy.context.active_object
fill.name = "ENV_Fill"
fill.data.energy = 40.0

bpy.ops.mesh.primitive_plane_add(size=15, location=(0, 0, -0.05))
ground = bpy.context.active_object
ground.name = "ENV_Ground"
mat_gnd = bpy.data.materials.new("Ground_Soft")
mat_gnd.use_nodes = True
bsdf_gnd = mat_gnd.node_tree.nodes["Principled BSDF"]
bsdf_gnd.inputs["Base Color"].default_value = (0.18, 0.15, 0.12, 1.0)
bsdf_gnd.inputs["Roughness"].default_value = 0.95
ground.data.materials.append(mat_gnd)

bpy.ops.object.camera_add(location=(4, -3, 3.5))
cam = bpy.context.active_object
cam.name = "ENV_Camera"
cam.rotation_euler = (math.radians(55), 0, math.radians(50))
bpy.context.scene.camera = cam

output_path = r"C:/Users/denis/Documents/Proyectos/3_TapGames/mcp-3d-pipeline/output/rosa.blend"
bpy.ops.wm.save_as_mainfile(filepath=output_path)
print("## OUTPUT ##")
print(f"Saved: {output_path}")
print("Objects:", [o.name for o in bpy.data.objects])
