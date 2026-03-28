import bpy
import math

bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete(use_global=False)

# --- Phase 1: Planet core sphere ---
bpy.ops.mesh.primitive_uv_sphere_add(radius=1.5, segments=64, ring_count=32, location=(0, 0, 0))
planet = bpy.context.active_object
planet.name = "Planet_Core_p1"

mat_planet = bpy.data.materials.new("Planet_Surface")
mat_planet.use_nodes = True
nodes = mat_planet.node_tree.nodes
links = mat_planet.node_tree.links
bsdf = nodes["Principled BSDF"]

# Add noise texture for terrain
noise = nodes.new("ShaderNodeTexNoise")
noise.inputs["Scale"].default_value = 4.0
noise.inputs["Detail"].default_value = 8.0

color_ramp = nodes.new("ShaderNodeValToRGB")
color_ramp.color_ramp.elements[0].position = 0.4
color_ramp.color_ramp.elements[0].color = (0.1, 0.25, 0.6, 1.0)  # ocean
color_ramp.color_ramp.elements[1].position = 0.55
color_ramp.color_ramp.elements[1].color = (0.2, 0.5, 0.15, 1.0)  # land

links.new(noise.outputs["Fac"], color_ramp.inputs["Fac"])
links.new(color_ramp.outputs["Color"], bsdf.inputs["Base Color"])
planet.data.materials.append(mat_planet)

# --- Phase 2: Atmosphere glow ---
bpy.ops.mesh.primitive_uv_sphere_add(radius=1.6, segments=48, ring_count=24, location=(0, 0, 0))
atmo = bpy.context.active_object
atmo.name = "Planet_Atmosphere_p2"

mat_atmo = bpy.data.materials.new("Atmosphere")
mat_atmo.use_nodes = True
nodes_a = mat_atmo.node_tree.nodes
links_a = mat_atmo.node_tree.links
bsdf_a = nodes_a["Principled BSDF"]
bsdf_a.inputs["Base Color"].default_value = (0.3, 0.6, 1.0, 1.0)
bsdf_a.inputs["Alpha"].default_value = 0.08
bsdf_a.inputs["Emission Color"].default_value = (0.2, 0.4, 0.8, 1.0)
bsdf_a.inputs["Emission Strength"].default_value = 0.5
mat_atmo.blend_method = "BLEND" if hasattr(mat_atmo, 'blend_method') else None
atmo.data.materials.append(mat_atmo)

# --- Phase 3: Rings ---
mat_ring = bpy.data.materials.new("Ring_Material")
mat_ring.use_nodes = True
nodes_r = mat_ring.node_tree.nodes
links_r = mat_ring.node_tree.links
bsdf_r = nodes_r["Principled BSDF"]

noise_r = nodes_r.new("ShaderNodeTexNoise")
noise_r.inputs["Scale"].default_value = 50.0
noise_r.inputs["Detail"].default_value = 3.0

cr_r = nodes_r.new("ShaderNodeValToRGB")
cr_r.color_ramp.elements[0].position = 0.3
cr_r.color_ramp.elements[0].color = (0.6, 0.5, 0.35, 1.0)
cr_r.color_ramp.elements[1].position = 0.7
cr_r.color_ramp.elements[1].color = (0.8, 0.7, 0.5, 1.0)

links_r.new(noise_r.outputs["Fac"], cr_r.inputs["Fac"])
links_r.new(cr_r.outputs["Color"], bsdf_r.inputs["Base Color"])
bsdf_r.inputs["Metallic"].default_value = 0.3
bsdf_r.inputs["Roughness"].default_value = 0.6

# Main ring
bpy.ops.mesh.primitive_torus_add(
    major_radius=2.8, minor_radius=0.15,
    major_segments=64, minor_segments=12,
    location=(0, 0, 0),
    rotation=(math.radians(20), math.radians(5), 0)
)
ring1 = bpy.context.active_object
ring1.name = "Planet_Ring1_p3"
ring1.scale = (1.0, 1.0, 0.1)
bpy.ops.object.transform_apply(scale=True)
ring1.data.materials.append(mat_ring)

# Inner ring
bpy.ops.mesh.primitive_torus_add(
    major_radius=2.3, minor_radius=0.08,
    major_segments=64, minor_segments=12,
    location=(0, 0, 0),
    rotation=(math.radians(20), math.radians(5), 0)
)
ring2 = bpy.context.active_object
ring2.name = "Planet_Ring2_p3"
ring2.scale = (1.0, 1.0, 0.1)
bpy.ops.object.transform_apply(scale=True)
ring2.data.materials.append(mat_ring)

# --- Phase 4: Moons ---
mat_moon = bpy.data.materials.new("Moon_Gray")
mat_moon.use_nodes = True
bsdf_m = mat_moon.node_tree.nodes["Principled BSDF"]
bsdf_m.inputs["Base Color"].default_value = (0.6, 0.6, 0.58, 1.0)
bsdf_m.inputs["Roughness"].default_value = 0.8

# Moon 1 - larger
bpy.ops.mesh.primitive_uv_sphere_add(radius=0.25, location=(3.0, 1.0, 0.5))
moon1 = bpy.context.active_object
moon1.name = "Planet_Moon1_p4"
moon1.data.materials.append(mat_moon)

# Moon 2 - smaller, icy
mat_ice = bpy.data.materials.new("Moon_Ice")
mat_ice.use_nodes = True
bsdf_ice = mat_ice.node_tree.nodes["Principled BSDF"]
bsdf_ice.inputs["Base Color"].default_value = (0.7, 0.85, 0.95, 1.0)
bsdf_ice.inputs["Roughness"].default_value = 0.3
bsdf_ice.inputs["Metallic"].default_value = 0.2

bpy.ops.mesh.primitive_uv_sphere_add(radius=0.15, location=(-2.5, -1.5, -0.3))
moon2 = bpy.context.active_object
moon2.name = "Planet_Moon2_p4"
moon2.data.materials.append(mat_ice)

# --- Phase 5: Space station / satellite ---
mat_station = bpy.data.materials.new("Station_Metal")
mat_station.use_nodes = True
bsdf_st = mat_station.node_tree.nodes["Principled BSDF"]
bsdf_st.inputs["Base Color"].default_value = (0.7, 0.7, 0.72, 1.0)
bsdf_st.inputs["Metallic"].default_value = 0.9
bsdf_st.inputs["Roughness"].default_value = 0.2

# Station body
bpy.ops.mesh.primitive_cylinder_add(radius=0.08, depth=0.4, location=(2.0, -2.0, 1.0))
st_body = bpy.context.active_object
st_body.name = "Planet_Station_p5"
st_body.data.materials.append(mat_station)

# Solar panels
mat_solar = bpy.data.materials.new("Solar_Panel")
mat_solar.use_nodes = True
bsdf_sol = mat_solar.node_tree.nodes["Principled BSDF"]
bsdf_sol.inputs["Base Color"].default_value = (0.05, 0.05, 0.3, 1.0)
bsdf_sol.inputs["Metallic"].default_value = 0.5

for x_off in [-0.25, 0.25]:
    bpy.ops.mesh.primitive_cube_add(size=1, location=(2.0 + x_off, -2.0, 1.0))
    panel = bpy.context.active_object
    panel.name = "Planet_SolarPanel_p5"
    panel.scale = (0.15, 0.02, 0.3)
    bpy.ops.object.transform_apply(scale=True)
    panel.data.materials.append(mat_solar)

# --- Phase 6: Details (craters, clouds, stars, asteroid belt) ---
# Cloud wisps on planet surface
mat_cloud = bpy.data.materials.new("Cloud")
mat_cloud.use_nodes = True
bsdf_cl = mat_cloud.node_tree.nodes["Principled BSDF"]
bsdf_cl.inputs["Base Color"].default_value = (1.0, 1.0, 1.0, 1.0)
bsdf_cl.inputs["Alpha"].default_value = 0.3
mat_cloud.blend_method = "BLEND" if hasattr(mat_cloud, 'blend_method') else None

for i, (lat, lon) in enumerate([(30, 45), (60, 120), (-20, 200), (10, 300)]):
    r = 1.55
    x = r * math.cos(math.radians(lat)) * math.cos(math.radians(lon))
    y = r * math.cos(math.radians(lat)) * math.sin(math.radians(lon))
    z = r * math.sin(math.radians(lat))
    bpy.ops.mesh.primitive_uv_sphere_add(radius=0.3, location=(x, y, z))
    cloud = bpy.context.active_object
    cloud.name = f"Planet_Cloud{i}_p6"
    cloud.scale = (2.0, 1.0, 0.2)
    bpy.ops.object.transform_apply(scale=True)
    cloud.data.materials.append(mat_cloud)

# Small asteroids
mat_ast = bpy.data.materials.new("Asteroid")
mat_ast.use_nodes = True
bsdf_ast = mat_ast.node_tree.nodes["Principled BSDF"]
bsdf_ast.inputs["Base Color"].default_value = (0.35, 0.3, 0.25, 1.0)
bsdf_ast.inputs["Roughness"].default_value = 0.95

for i in range(5):
    angle = i * (360 / 5)
    dist = 4.0
    x = dist * math.cos(math.radians(angle))
    y = dist * math.sin(math.radians(angle))
    z = 0.3 * math.sin(math.radians(angle * 2))
    bpy.ops.mesh.primitive_ico_sphere_add(radius=0.06, location=(x, y, z))
    ast = bpy.context.active_object
    ast.name = f"Planet_Asteroid{i}_p6"
    ast.data.materials.append(mat_ast)

# --- Environment ---
# World background - dark space with stars
world = bpy.context.scene.world
if world is None:
    world = bpy.data.worlds.new("World")
    bpy.context.scene.world = world
world.use_nodes = True
w_nodes = world.node_tree.nodes
w_links = world.node_tree.links
bg = w_nodes["Background"]
bg.inputs["Color"].default_value = (0.0, 0.0, 0.02, 1.0)
bg.inputs["Strength"].default_value = 0.5

bpy.ops.object.light_add(type='SUN', location=(10, -5, 8))
sun = bpy.context.active_object
sun.name = "ENV_Sun"
sun.data.energy = 5.0

bpy.ops.object.light_add(type='POINT', location=(-5, 3, 2))
fill = bpy.context.active_object
fill.name = "ENV_Fill"
fill.data.energy = 100.0

bpy.ops.object.camera_add(location=(6, -5, 3))
cam = bpy.context.active_object
cam.name = "ENV_Camera"
cam.rotation_euler = (math.radians(70), 0, math.radians(50))
bpy.context.scene.camera = cam

output_path = r"C:/Users/denis/Documents/Proyectos/3_TapGames/mcp-3d-pipeline/output/planeta_espacial.blend"
bpy.ops.wm.save_as_mainfile(filepath=output_path)
print("## OUTPUT ##")
print(f"Saved: {output_path}")
print("Objects:", [o.name for o in bpy.data.objects])
