import bpy
import math

# Clear scene
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete(use_global=False)

# --- Phase 1: Body chassis ---
bpy.ops.mesh.primitive_cube_add(size=1, location=(0, 0, 0.5))
body = bpy.context.active_object
body.name = "Car_Body_p1"
body.scale = (2.2, 1.0, 0.45)
bpy.ops.object.transform_apply(scale=True)

# Body material - red metallic
mat_body = bpy.data.materials.new("CarBody_Red")
mat_body.use_nodes = True
bsdf = mat_body.node_tree.nodes["Principled BSDF"]
bsdf.inputs["Base Color"].default_value = (0.8, 0.1, 0.05, 1.0)
bsdf.inputs["Metallic"].default_value = 0.7
bsdf.inputs["Roughness"].default_value = 0.2
body.data.materials.append(mat_body)

# --- Phase 2: Roof / cabin ---
bpy.ops.mesh.primitive_cube_add(size=1, location=(0.2, 0, 1.05))
roof = bpy.context.active_object
roof.name = "Car_Roof_p2"
roof.scale = (1.2, 0.9, 0.4)
bpy.ops.object.transform_apply(scale=True)

mat_roof = bpy.data.materials.new("CarRoof_DarkRed")
mat_roof.use_nodes = True
bsdf2 = mat_roof.node_tree.nodes["Principled BSDF"]
bsdf2.inputs["Base Color"].default_value = (0.6, 0.08, 0.04, 1.0)
bsdf2.inputs["Metallic"].default_value = 0.6
bsdf2.inputs["Roughness"].default_value = 0.25
roof.data.materials.append(mat_roof)

# --- Phase 3: Hood and trunk ---
bpy.ops.mesh.primitive_cube_add(size=1, location=(-1.4, 0, 0.65))
hood = bpy.context.active_object
hood.name = "Car_Hood_p3"
hood.scale = (0.7, 0.95, 0.2)
bpy.ops.object.transform_apply(scale=True)
hood.data.materials.append(mat_body)

bpy.ops.mesh.primitive_cube_add(size=1, location=(1.5, 0, 0.6))
trunk = bpy.context.active_object
trunk.name = "Car_Trunk_p3"
trunk.scale = (0.5, 0.9, 0.18)
bpy.ops.object.transform_apply(scale=True)
trunk.data.materials.append(mat_body)

# --- Phase 4: Wheels (4 wheels) ---
mat_wheel = bpy.data.materials.new("Wheel_Dark")
mat_wheel.use_nodes = True
bsdf_w = mat_wheel.node_tree.nodes["Principled BSDF"]
bsdf_w.inputs["Base Color"].default_value = (0.05, 0.05, 0.05, 1.0)
bsdf_w.inputs["Roughness"].default_value = 0.8

mat_rim = bpy.data.materials.new("Wheel_Rim")
mat_rim.use_nodes = True
bsdf_r = mat_rim.node_tree.nodes["Principled BSDF"]
bsdf_r.inputs["Base Color"].default_value = (0.8, 0.8, 0.85, 1.0)
bsdf_r.inputs["Metallic"].default_value = 0.9
bsdf_r.inputs["Roughness"].default_value = 0.1

wheel_positions = [
    (-1.2, -0.9, 0.2, "FL"),
    (-1.2, 0.9, 0.2, "FR"),
    (1.0, -0.9, 0.2, "RL"),
    (1.0, 0.9, 0.2, "RR"),
]

for x, y, z, label in wheel_positions:
    # Tire
    bpy.ops.mesh.primitive_torus_add(
        major_radius=0.28, minor_radius=0.1,
        location=(x, y, z),
        rotation=(math.pi/2, 0, 0)
    )
    tire = bpy.context.active_object
    tire.name = f"Car_Wheel_{label}_p4"
    tire.data.materials.append(mat_wheel)

    # Rim
    bpy.ops.mesh.primitive_cylinder_add(
        radius=0.18, depth=0.08,
        location=(x, y, z),
        rotation=(math.pi/2, 0, 0)
    )
    rim = bpy.context.active_object
    rim.name = f"Car_Rim_{label}_p4"
    rim.data.materials.append(mat_rim)

# --- Phase 5: Windows and lights ---
mat_glass = bpy.data.materials.new("Glass_Blue")
mat_glass.use_nodes = True
bsdf_g = mat_glass.node_tree.nodes["Principled BSDF"]
bsdf_g.inputs["Base Color"].default_value = (0.3, 0.5, 0.8, 1.0)
bsdf_g.inputs["Alpha"].default_value = 0.4
bsdf_g.inputs["Roughness"].default_value = 0.05
mat_glass.blend_method = "BLEND" if hasattr(mat_glass, 'blend_method') else None

# Windshield
bpy.ops.mesh.primitive_cube_add(size=1, location=(-0.45, 0, 1.05))
windshield = bpy.context.active_object
windshield.name = "Car_Windshield_p5"
windshield.scale = (0.05, 0.82, 0.35)
bpy.ops.object.transform_apply(scale=True)
windshield.data.materials.append(mat_glass)

# Rear window
bpy.ops.mesh.primitive_cube_add(size=1, location=(0.82, 0, 1.0))
rear_w = bpy.context.active_object
rear_w.name = "Car_RearWindow_p5"
rear_w.scale = (0.05, 0.75, 0.3)
bpy.ops.object.transform_apply(scale=True)
rear_w.data.materials.append(mat_glass)

# Headlights
mat_light = bpy.data.materials.new("Headlight")
mat_light.use_nodes = True
bsdf_l = mat_light.node_tree.nodes["Principled BSDF"]
bsdf_l.inputs["Base Color"].default_value = (1.0, 0.95, 0.7, 1.0)
bsdf_l.inputs["Emission Color"].default_value = (1.0, 0.95, 0.7, 1.0)
bsdf_l.inputs["Emission Strength"].default_value = 3.0

for y_pos in [-0.65, 0.65]:
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.1, location=(-1.75, y_pos, 0.55))
    hl = bpy.context.active_object
    hl.name = f"Car_Headlight_p5"
    hl.data.materials.append(mat_light)

# Taillights
mat_tail = bpy.data.materials.new("Taillight")
mat_tail.use_nodes = True
bsdf_t = mat_tail.node_tree.nodes["Principled BSDF"]
bsdf_t.inputs["Base Color"].default_value = (1.0, 0.0, 0.0, 1.0)
bsdf_t.inputs["Emission Color"].default_value = (1.0, 0.0, 0.0, 1.0)
bsdf_t.inputs["Emission Strength"].default_value = 2.0

for y_pos in [-0.65, 0.65]:
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.08, location=(1.75, y_pos, 0.5))
    tl = bpy.context.active_object
    tl.name = f"Car_Taillight_p5"
    tl.data.materials.append(mat_tail)

# --- Phase 6: Details (mirrors, grille, plate) ---
mat_chrome = bpy.data.materials.new("Chrome")
mat_chrome.use_nodes = True
bsdf_c = mat_chrome.node_tree.nodes["Principled BSDF"]
bsdf_c.inputs["Base Color"].default_value = (0.9, 0.9, 0.92, 1.0)
bsdf_c.inputs["Metallic"].default_value = 1.0
bsdf_c.inputs["Roughness"].default_value = 0.05

# Side mirrors
for y_pos in [-1.0, 1.0]:
    bpy.ops.mesh.primitive_cube_add(size=0.15, location=(-0.3, y_pos, 0.95))
    mirror = bpy.context.active_object
    mirror.name = "Car_Mirror_p6"
    mirror.scale = (1.0, 0.5, 0.7)
    bpy.ops.object.transform_apply(scale=True)
    mirror.data.materials.append(mat_chrome)

# Front grille
bpy.ops.mesh.primitive_cube_add(size=1, location=(-1.78, 0, 0.45))
grille = bpy.context.active_object
grille.name = "Car_Grille_p6"
grille.scale = (0.05, 0.6, 0.15)
bpy.ops.object.transform_apply(scale=True)
grille.data.materials.append(mat_chrome)

# License plate
mat_plate = bpy.data.materials.new("Plate_White")
mat_plate.use_nodes = True
bsdf_p = mat_plate.node_tree.nodes["Principled BSDF"]
bsdf_p.inputs["Base Color"].default_value = (0.95, 0.95, 0.95, 1.0)

bpy.ops.mesh.primitive_cube_add(size=1, location=(-1.8, 0, 0.35))
plate = bpy.context.active_object
plate.name = "Car_Plate_p6"
plate.scale = (0.02, 0.3, 0.08)
bpy.ops.object.transform_apply(scale=True)
plate.data.materials.append(mat_plate)

# Door handles
for y_pos in [-0.95, 0.95]:
    bpy.ops.mesh.primitive_cube_add(size=0.1, location=(0.0, y_pos, 0.65))
    handle = bpy.context.active_object
    handle.name = "Car_Handle_p6"
    handle.scale = (1.5, 0.3, 0.4)
    bpy.ops.object.transform_apply(scale=True)
    handle.data.materials.append(mat_chrome)

# --- Environment ---
bpy.ops.object.light_add(type='SUN', location=(5, -5, 10))
sun = bpy.context.active_object
sun.name = "ENV_Sun"
sun.data.energy = 3.0

bpy.ops.object.light_add(type='AREA', location=(-3, 3, 5))
fill = bpy.context.active_object
fill.name = "ENV_Fill"
fill.data.energy = 50.0

# Ground plane
bpy.ops.mesh.primitive_plane_add(size=20, location=(0, 0, -0.05))
ground = bpy.context.active_object
ground.name = "ENV_Ground"
mat_ground = bpy.data.materials.new("Ground")
mat_ground.use_nodes = True
bsdf_gnd = mat_ground.node_tree.nodes["Principled BSDF"]
bsdf_gnd.inputs["Base Color"].default_value = (0.15, 0.15, 0.18, 1.0)
bsdf_gnd.inputs["Roughness"].default_value = 0.9
ground.data.materials.append(mat_ground)

# Camera
bpy.ops.object.camera_add(location=(5, -4, 3))
cam = bpy.context.active_object
cam.name = "ENV_Camera"
cam.rotation_euler = (math.radians(65), 0, math.radians(55))
bpy.context.scene.camera = cam

# Save
output_path = r"C:/Users/denis/Documents/Proyectos/3_TapGames/mcp-3d-pipeline/output/coche.blend"
bpy.ops.wm.save_as_mainfile(filepath=output_path)
print("## OUTPUT ##")
print(f"Saved: {output_path}")
print("Objects:", [o.name for o in bpy.data.objects])
