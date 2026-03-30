"""Create cheshire_grin.blend — Sinister floating Cheshire grin with fangs.

A crescent-shaped floating smile with green cracked lips, irregular sharp teeth,
and ethereal smoke. 7 phases (phase_1 to phase_6 + victory).

Usage:
  blender.exe --background --python create_cheshire_grin.py -- '{"output_dir":"...","render_dir":"..."}'
"""
import bpy
import bmesh
import json
import math
import os
import sys
from mathutils import Vector, Euler

# ─── Args ─────────────────────────────────────────────────────────────────────
try:
    args = json.loads(sys.argv[sys.argv.index("--") + 1])
except (ValueError, IndexError):
    args = {}

script_dir = os.path.dirname(os.path.abspath(__file__))
pipeline_dir = os.path.dirname(script_dir)
project_dir = os.path.dirname(pipeline_dir)
output_dir = args.get("output_dir", os.path.join(pipeline_dir, "output"))
render_dir = args.get("render_dir", "")
resolution = args.get("resolution", 512)
if not os.path.isabs(output_dir):
    output_dir = os.path.join(project_dir, output_dir)
if render_dir and not os.path.isabs(render_dir):
    render_dir = os.path.join(project_dir, render_dir)
os.makedirs(output_dir, exist_ok=True)

# ─── Clear ────────────────────────────────────────────────────────────────────
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete()
for m in bpy.data.meshes:
    bpy.data.meshes.remove(m)
for m in bpy.data.materials:
    bpy.data.materials.remove(m)

scene = bpy.context.scene

def tag_phase(obj, phase):
    obj["min_phase"] = phase

def set_phase_visibility(phase):
    for obj in bpy.data.objects:
        if "min_phase" in obj:
            obj.hide_render = obj["min_phase"] > phase
            obj.hide_viewport = obj["min_phase"] > phase
        elif obj.type in ('CAMERA', 'LIGHT'):
            obj.hide_render = False
            obj.hide_viewport = False


# ═══════════════════════════════════════════════════════════════════════════════
# MATERIALS
# ═══════════════════════════════════════════════════════════════════════════════

def mat_green_lip():
    mat = bpy.data.materials.new("GreenLip")
    mat.use_nodes = True
    nt = mat.node_tree
    nt.nodes.clear()
    out = nt.nodes.new('ShaderNodeOutputMaterial'); out.location = (900, 0)
    bsdf = nt.nodes.new('ShaderNodeBsdfPrincipled'); bsdf.location = (500, 0)
    nt.links.new(bsdf.outputs[0], out.inputs[0])

    tc = nt.nodes.new('ShaderNodeTexCoord'); tc.location = (-500, 0)

    # Green with variation
    noise = nt.nodes.new('ShaderNodeTexNoise'); noise.location = (-200, 150)
    noise.inputs['Scale'].default_value = 5.0
    noise.inputs['Detail'].default_value = 8.0
    noise.inputs['Roughness'].default_value = 0.65
    nt.links.new(tc.outputs['Object'], noise.inputs['Vector'])

    ramp = nt.nodes.new('ShaderNodeValToRGB'); ramp.location = (100, 150)
    ramp.color_ramp.elements[0].position = 0.35
    ramp.color_ramp.elements[0].color = (0.03, 0.12, 0.01, 1)
    ramp.color_ramp.elements[1].position = 0.65
    ramp.color_ramp.elements[1].color = (0.12, 0.4, 0.04, 1)
    e = ramp.color_ramp.elements.new(0.85)
    e.color = (0.22, 0.55, 0.06, 1)
    nt.links.new(noise.outputs['Fac'], ramp.inputs['Fac'])
    nt.links.new(ramp.outputs['Color'], bsdf.inputs['Base Color'])

    bsdf.inputs['Roughness'].default_value = 0.18
    bsdf.inputs['Specular IOR Level'].default_value = 0.8
    bsdf.inputs['Subsurface Weight'].default_value = 0.2
    bsdf.inputs['Subsurface Radius'].default_value = (0.1, 0.3, 0.05)

    # Voronoi cracks for bump
    vor = nt.nodes.new('ShaderNodeTexVoronoi'); vor.location = (-200, -100)
    vor.inputs['Scale'].default_value = 20.0
    nt.links.new(tc.outputs['Object'], vor.inputs['Vector'])
    bump = nt.nodes.new('ShaderNodeBump'); bump.location = (300, -100)
    bump.inputs['Strength'].default_value = 0.4
    nt.links.new(vor.outputs['Distance'], bump.inputs['Height'])
    nt.links.new(bump.outputs[0], bsdf.inputs['Normal'])

    return mat

def mat_dark_mouth():
    mat = bpy.data.materials.new("DarkMouth")
    mat.use_nodes = True
    nt = mat.node_tree
    bsdf = nt.nodes.get('Principled BSDF')
    bsdf.inputs['Base Color'].default_value = (0.005, 0.003, 0.005, 1)
    bsdf.inputs['Roughness'].default_value = 0.95
    return mat

def mat_tooth():
    mat = bpy.data.materials.new("Tooth")
    mat.use_nodes = True
    nt = mat.node_tree
    bsdf = nt.nodes.get('Principled BSDF')
    bsdf.inputs['Base Color'].default_value = (0.5, 0.55, 0.2, 1)
    bsdf.inputs['Roughness'].default_value = 0.22
    bsdf.inputs['Subsurface Weight'].default_value = 0.15
    bsdf.inputs['Subsurface Radius'].default_value = (0.1, 0.12, 0.05)
    return mat

def mat_fang():
    mat = bpy.data.materials.new("Fang")
    mat.use_nodes = True
    nt = mat.node_tree
    bsdf = nt.nodes.get('Principled BSDF')
    bsdf.inputs['Base Color'].default_value = (0.6, 0.65, 0.3, 1)
    bsdf.inputs['Roughness'].default_value = 0.12
    bsdf.inputs['Specular IOR Level'].default_value = 0.7
    return mat

def mat_smoke():
    mat = bpy.data.materials.new("Smoke")
    mat.use_nodes = True
    nt = mat.node_tree
    nt.nodes.clear()
    out = nt.nodes.new('ShaderNodeOutputMaterial'); out.location = (700, 0)
    mix = nt.nodes.new('ShaderNodeMixShader'); mix.location = (500, 0)
    nt.links.new(mix.outputs[0], out.inputs[0])
    tr = nt.nodes.new('ShaderNodeBsdfTransparent'); tr.location = (250, 80)
    em = nt.nodes.new('ShaderNodeEmission'); em.location = (250, -80)
    em.inputs['Color'].default_value = (0.08, 0.3, 0.06, 1)
    em.inputs['Strength'].default_value = 0.6
    nt.links.new(tr.outputs[0], mix.inputs[1])
    nt.links.new(em.outputs[0], mix.inputs[2])

    tc = nt.nodes.new('ShaderNodeTexCoord'); tc.location = (-300, 0)
    noise = nt.nodes.new('ShaderNodeTexNoise'); noise.location = (-50, 0)
    noise.inputs['Scale'].default_value = 3.0
    noise.inputs['Distortion'].default_value = 3.0
    nt.links.new(tc.outputs['Object'], noise.inputs['Vector'])
    r = nt.nodes.new('ShaderNodeValToRGB'); r.location = (150, 0)
    r.color_ramp.elements[0].position = 0.35
    r.color_ramp.elements[0].color = (0.8, 0.8, 0.8, 1)
    r.color_ramp.elements[1].position = 0.55
    r.color_ramp.elements[1].color = (0.3, 0.3, 0.3, 1)
    nt.links.new(noise.outputs['Fac'], r.inputs['Fac'])
    nt.links.new(r.outputs['Color'], mix.inputs['Fac'])
    return mat

def mat_glow_tooth():
    mat = bpy.data.materials.new("GlowTooth")
    mat.use_nodes = True
    nt = mat.node_tree
    nt.nodes.clear()
    out = nt.nodes.new('ShaderNodeOutputMaterial'); out.location = (700, 0)
    mix = nt.nodes.new('ShaderNodeMixShader'); mix.location = (500, 0)
    mix.inputs['Fac'].default_value = 0.4
    nt.links.new(mix.outputs[0], out.inputs[0])
    bs = nt.nodes.new('ShaderNodeBsdfPrincipled'); bs.location = (250, 100)
    bs.inputs['Base Color'].default_value = (0.5, 0.6, 0.2, 1)
    bs.inputs['Roughness'].default_value = 0.2
    nt.links.new(bs.outputs[0], mix.inputs[1])
    em = nt.nodes.new('ShaderNodeEmission'); em.location = (250, -100)
    em.inputs['Color'].default_value = (0.2, 0.95, 0.08, 1)
    em.inputs['Strength'].default_value = 3.0
    nt.links.new(em.outputs[0], mix.inputs[2])
    return mat

def mat_glow_lip():
    mat = bpy.data.materials.new("GlowLip")
    mat.use_nodes = True
    nt = mat.node_tree
    nt.nodes.clear()
    out = nt.nodes.new('ShaderNodeOutputMaterial'); out.location = (700, 0)
    mix = nt.nodes.new('ShaderNodeMixShader'); mix.location = (500, 0)
    nt.links.new(mix.outputs[0], out.inputs[0])
    bs = nt.nodes.new('ShaderNodeBsdfPrincipled'); bs.location = (250, 100)
    bs.inputs['Base Color'].default_value = (0.12, 0.4, 0.04, 1)
    bs.inputs['Roughness'].default_value = 0.18
    nt.links.new(bs.outputs[0], mix.inputs[1])
    em = nt.nodes.new('ShaderNodeEmission'); em.location = (250, -100)
    em.inputs['Color'].default_value = (0.1, 0.8, 0.05, 1)
    em.inputs['Strength'].default_value = 2.0
    nt.links.new(em.outputs[0], mix.inputs[2])
    fr = nt.nodes.new('ShaderNodeFresnel'); fr.location = (250, 0)
    fr.inputs['IOR'].default_value = 2.0
    nt.links.new(fr.outputs[0], mix.inputs['Fac'])
    return mat

# Create all
M_LIP = mat_green_lip()
M_MOUTH = mat_dark_mouth()
M_TOOTH = mat_tooth()
M_FANG = mat_fang()
M_SMOKE = mat_smoke()
M_GLOW_T = mat_glow_tooth()
M_GLOW_L = mat_glow_lip()


# ═══════════════════════════════════════════════════════════════════════════════
# GEOMETRY
# ═══════════════════════════════════════════════════════════════════════════════

def smile_arc(t, is_upper=True):
    """Return (x, z) on the smile crescent for parameter t in [-1, 1].
    t=-1 = left corner, t=0 = center, t=1 = right corner.
    """
    x = t * 0.7
    # Parabolic arc for the crescent corners-up shape
    corner_lift = 0.35 * t * t  # Corners go UP
    if is_upper:
        z = corner_lift + 0.06
    else:
        z = corner_lift - 0.1 * (1 - t * t)  # Center dips down more
    return x, z


def make_lip_mesh(name, is_upper, phase=1):
    """Create lip as a series of ellipsoidal sections along the smile arc."""
    verts = []
    faces = []
    segments = 32  # Along the arc
    ring_pts = 8   # Cross-section circle

    thickness = 0.055 if not is_upper else 0.045  # Lower lip plumper
    depth = 0.04  # Y depth

    for i in range(segments + 1):
        t = -1.0 + 2.0 * i / segments  # -1 to 1
        x, z = smile_arc(t, is_upper)

        # Taper at corners
        corner_factor = 1.0 - 0.7 * abs(t) ** 2
        th = thickness * max(corner_factor, 0.15)
        dp = depth * max(corner_factor, 0.2)

        for j in range(ring_pts):
            angle = 2 * math.pi * j / ring_pts
            # Elliptical cross section
            dy = dp * math.cos(angle)
            dz = th * math.sin(angle)
            verts.append((x, dy, z + dz))

    # Create faces between rings
    for i in range(segments):
        for j in range(ring_pts):
            v0 = i * ring_pts + j
            v1 = i * ring_pts + (j + 1) % ring_pts
            v2 = (i + 1) * ring_pts + (j + 1) % ring_pts
            v3 = (i + 1) * ring_pts + j
            faces.append((v0, v1, v2, v3))

    mesh = bpy.data.meshes.new(name)
    mesh.from_pydata(verts, [], faces)
    mesh.update()

    obj = bpy.data.objects.new(name, mesh)
    bpy.context.collection.objects.link(obj)

    # Smooth + subdivide
    bpy.context.view_layer.objects.active = obj
    obj.select_set(True)
    bpy.ops.object.shade_smooth()
    sub = obj.modifiers.new("Sub", 'SUBSURF')
    sub.levels = 2
    sub.render_levels = 2

    obj.data.materials.append(M_LIP)
    tag_phase(obj, phase)
    return obj


def make_inner_mouth(phase=1):
    """Dark void inside — follows the smile arc."""
    verts = []
    faces = []
    segments = 24
    ring_pts = 8

    for i in range(segments + 1):
        t = -0.85 + 1.7 * i / segments
        x, z_upper = smile_arc(t, True)
        _, z_lower = smile_arc(t, False)
        z_center = (z_upper + z_lower) / 2
        height = max((z_upper - z_lower) * 0.6, 0.02)

        corner_factor = 1.0 - 0.6 * abs(t) ** 2
        dp = 0.05 * max(corner_factor, 0.15)

        for j in range(ring_pts):
            angle = 2 * math.pi * j / ring_pts
            dy = dp * math.cos(angle) + 0.005
            dz = height * math.sin(angle)
            verts.append((x, dy, z_center + dz))

    for i in range(segments):
        for j in range(ring_pts):
            v0 = i * ring_pts + j
            v1 = i * ring_pts + (j + 1) % ring_pts
            v2 = (i + 1) * ring_pts + (j + 1) % ring_pts
            v3 = (i + 1) * ring_pts + j
            faces.append((v0, v1, v2, v3))

    mesh = bpy.data.meshes.new("InnerMouth")
    mesh.from_pydata(verts, [], faces)
    mesh.update()
    obj = bpy.data.objects.new("InnerMouth", mesh)
    bpy.context.collection.objects.link(obj)
    bpy.context.view_layer.objects.active = obj
    obj.select_set(True)
    bpy.ops.object.shade_smooth()

    obj.data.materials.append(M_MOUTH)
    tag_phase(obj, phase)
    return obj


def place_tooth(name, t_pos, is_upper, is_fang=False, scale_mult=1.0, phase=2):
    """Place a tooth at position t on the smile arc."""
    x_u, z_u = smile_arc(t_pos, True)
    x_l, z_l = smile_arc(t_pos, False)
    x = (x_u + x_l) / 2
    z_mid = (z_u + z_l) / 2
    gap = abs(z_u - z_l)

    y_front = -0.045  # In front of lips

    if is_fang:
        length = max(gap * 2.5, 0.12) * scale_mult
        radius = 0.025 * scale_mult
        bpy.ops.mesh.primitive_cone_add(
            radius1=radius, radius2=radius * 0.15, depth=length,
            vertices=12, location=(x, y_front, z_mid)
        )
        tooth = bpy.context.active_object
        if is_upper:
            tooth.location.z = z_u - 0.01
        else:
            tooth.rotation_euler = Euler((math.radians(180), 0, 0))
            tooth.location.z = z_l + 0.01
    else:
        tooth_h = max(gap * 1.0, 0.06) * scale_mult
        tooth_w = 0.035 * scale_mult
        bpy.ops.mesh.primitive_cube_add(size=1.0, location=(x, y_front, z_mid))
        tooth = bpy.context.active_object
        tooth.scale = (tooth_w, 0.025, tooth_h / 2)
        bev = tooth.modifiers.new("Bev", 'BEVEL')
        bev.width = 0.006
        bev.segments = 2

    tooth.name = name
    # Tilt to follow arc curvature
    arc_angle = math.atan2(0.7 * t_pos, 1.0) * 0.35
    tooth.rotation_euler.y += arc_angle

    tooth.data.materials.append(M_FANG if is_fang else M_TOOTH)
    tag_phase(tooth, phase)
    return tooth


def make_smoke_wisp(name, location, scale, phase=6):
    """Single ethereal smoke wisp."""
    bpy.ops.mesh.primitive_ico_sphere_add(
        radius=1.0, subdivisions=3, location=location
    )
    w = bpy.context.active_object
    w.name = name
    w.scale = scale

    # Organic deformation
    dtex = bpy.data.textures.new(f"{name}_d", 'CLOUDS')
    dtex.noise_scale = 0.5
    d = w.modifiers.new("Warp", 'DISPLACE')
    d.texture = dtex
    d.strength = 0.06
    d.texture_coords = 'OBJECT'

    w.data.materials.append(M_SMOKE)
    tag_phase(w, phase)
    return w


# ═══════════════════════════════════════════════════════════════════════════════
# BUILD
# ═══════════════════════════════════════════════════════════════════════════════

# ─── Phase 1: Lips + inner mouth ─────────────────────────────────────────────
upper_lip = make_lip_mesh("UpperLip", is_upper=True, phase=1)
lower_lip = make_lip_mesh("LowerLip", is_upper=False, phase=1)
inner_mouth = make_inner_mouth(phase=1)

# ─── Phase 2: Front incisors (upper + lower) ─────────────────────────────────
front_positions = [-0.06, -0.025, 0.025, 0.06]
for i, tp in enumerate(front_positions):
    t = tp / 0.7  # Normalize to [-1, 1]
    place_tooth(f"U_front_{i}", t, True, is_fang=False, scale_mult=1.0, phase=2)
    place_tooth(f"L_front_{i}", t, False, is_fang=False, scale_mult=0.85, phase=2)

# ─── Phase 3: Canine fangs ───────────────────────────────────────────────────
fang_ts = [-0.13, 0.13]
for i, tp in enumerate(fang_ts):
    t = tp / 0.7
    place_tooth(f"U_fang_{i}", t, True, is_fang=True, scale_mult=1.4, phase=3)
    place_tooth(f"L_fang_{i}", t, False, is_fang=True, scale_mult=1.0, phase=3)

# Pre-molars
premolar_ts = [-0.19, 0.19]
for i, tp in enumerate(premolar_ts):
    t = tp / 0.7
    place_tooth(f"U_premolar_{i}", t, True, is_fang=False, scale_mult=0.9, phase=3)

# ─── Phase 4: Back teeth / irregular fangs ────────────────────────────────────
back_ts = [-0.26, -0.33, -0.40, 0.26, 0.33, 0.40]
for i, tp in enumerate(back_ts):
    t = tp / 0.7
    is_f = i % 2 == 0
    sc = 0.7 + (i % 3) * 0.15
    place_tooth(f"U_back_{i}", t, True, is_fang=is_f, scale_mult=sc, phase=4)
    place_tooth(f"L_back_{i}", t, False, is_fang=(not is_f), scale_mult=sc * 0.8, phase=4)

# ─── Phase 5: Extra menacing fangs between existing teeth ─────────────────────
extra_ts = [-0.095, 0.095, -0.22, 0.22]
for i, tp in enumerate(extra_ts):
    t = tp / 0.7
    place_tooth(f"Extra_fang_{i}", t, True, is_fang=True, scale_mult=1.2, phase=5)

# Lip crease detail
bpy.ops.mesh.primitive_torus_add(
    major_radius=0.55, minor_radius=0.005,
    major_segments=48, minor_segments=6,
    location=(0, -0.035, 0.12)
)
crease = bpy.context.active_object
crease.name = "LipCrease"
crease.scale = (1.3, 0.6, 0.5)
crease.data.materials.append(M_LIP)
tag_phase(crease, 5)

# ─── Phase 6: Corner tips + smoke wisps ───────────────────────────────────────
# Elongated corner tips
for side, sx in [("L", -1), ("R", 1)]:
    bpy.ops.mesh.primitive_cone_add(
        radius1=0.03, radius2=0.005, depth=0.12,
        vertices=12, location=(sx * 0.72, -0.02, 0.37)
    )
    tip = bpy.context.active_object
    tip.name = f"CornerTip_{side}"
    tip.rotation_euler = Euler((math.radians(-15), math.radians(sx * 55), 0))
    tip.data.materials.append(M_LIP)
    tag_phase(tip, 6)

# Smoke wisps
smoke_configs = [
    ("Smoke_L1", (-0.6, -0.06, 0.42), (0.08, 0.05, 0.18)),
    ("Smoke_L2", (-0.55, -0.04, 0.5), (0.06, 0.04, 0.14)),
    ("Smoke_R1", (0.6, -0.06, 0.42), (0.08, 0.05, 0.18)),
    ("Smoke_R2", (0.55, -0.04, 0.5), (0.06, 0.04, 0.14)),
    ("Smoke_C1", (-0.1, -0.04, 0.15), (0.08, 0.06, 0.12)),
    ("Smoke_C2", (0.1, -0.04, 0.15), (0.08, 0.06, 0.12)),
]
for sname, sloc, ssc in smoke_configs:
    make_smoke_wisp(sname, sloc, ssc, phase=6)

# ─── Phase 7 (Victory): Glowing teeth + lips ─────────────────────────────────
# Glow overlays on front teeth and fangs
for prefix in ["U_front_", "L_front_", "U_fang_", "L_fang_"]:
    for obj in list(bpy.data.objects):
        if obj.name.startswith(prefix) and "glow" not in obj.name:
            g = obj.copy()
            g.data = obj.data.copy()
            g.name = f"{obj.name}_glow"
            g.data.materials.clear()
            g.data.materials.append(M_GLOW_T)
            g.scale = tuple(s * 1.04 for s in obj.scale)
            bpy.context.collection.objects.link(g)
            tag_phase(g, 7)

# Lip glow overlays
for lname in ["UpperLip", "LowerLip"]:
    for obj in bpy.data.objects:
        if obj.name == lname:
            g = obj.copy()
            g.data = obj.data.copy()
            g.name = f"{lname}_glow"
            g.data.materials.clear()
            g.data.materials.append(M_GLOW_L)
            g.scale = tuple(s * 1.03 for s in obj.scale)
            bpy.context.collection.objects.link(g)
            tag_phase(g, 7)

# Extra bright smoke for victory
for i, (x, z) in enumerate([(-0.45, 0.55), (0.45, 0.55), (0.0, 0.25)]):
    make_smoke_wisp(f"VSmoke_{i}", (x, -0.08, z), (0.1, 0.08, 0.2), phase=7)


# ═══════════════════════════════════════════════════════════════════════════════
# CAMERA & LIGHTS
# ═══════════════════════════════════════════════════════════════════════════════

bpy.ops.object.camera_add(location=(0, -3.0, 0.12))
cam = bpy.context.active_object
cam.data.type = 'ORTHO'
cam.data.ortho_scale = 1.8
cam.rotation_euler = Euler((math.radians(90), 0, 0))
scene.camera = cam

# Key light — green-tinted from above-front
bpy.ops.object.light_add(type='AREA', location=(0.3, -2.0, 1.0))
key = bpy.context.active_object
key.name = "Key"
key.data.energy = 150
key.data.size = 2.5
key.data.color = (0.75, 1.0, 0.65)
key.rotation_euler = Euler((math.radians(55), 0, math.radians(-5)))

# Under-light for sinister uplighting on teeth
bpy.ops.object.light_add(type='AREA', location=(0, -1.5, -0.5))
under = bpy.context.active_object
under.name = "Under"
under.data.energy = 60
under.data.size = 1.5
under.data.color = (0.5, 0.9, 0.4)
under.rotation_euler = Euler((math.radians(-45), 0, 0))

# Rim lights
for x, n in [(-1.5, "RimL"), (1.5, "RimR")]:
    bpy.ops.object.light_add(type='AREA', location=(x, -0.8, 0.2))
    r = bpy.context.active_object
    r.name = n
    r.data.energy = 30
    r.data.size = 1.2
    r.data.color = (0.4, 0.7, 0.35)
    r.rotation_euler = Euler((math.radians(5), math.radians(-x * 25), 0))


# ═══════════════════════════════════════════════════════════════════════════════
# RENDER SETTINGS
# ═══════════════════════════════════════════════════════════════════════════════

scene.render.engine = 'BLENDER_EEVEE'
scene.render.resolution_x = resolution
scene.render.resolution_y = resolution
scene.render.film_transparent = True
scene.render.image_settings.file_format = 'PNG'
scene.render.image_settings.color_mode = 'RGBA'
scene.view_settings.view_transform = 'Standard'
scene.view_settings.look = 'None'
scene.eevee.taa_render_samples = 64

scene.world = bpy.data.worlds.new("W")
scene.world.use_nodes = True
bg = scene.world.node_tree.nodes['Background']
bg.inputs['Strength'].default_value = 0.0


# ═══════════════════════════════════════════════════════════════════════════════
# SAVE & RENDER
# ═══════════════════════════════════════════════════════════════════════════════

blend_path = os.path.join(output_dir, "cheshire_grin.blend")
bpy.ops.wm.save_as_mainfile(filepath=blend_path)
print(f"Saved: {blend_path}")

r_out = render_dir if render_dir else os.path.join(output_dir, "cheshire_grin_renders")
os.makedirs(r_out, exist_ok=True)

for phase in range(1, 8):
    set_phase_visibility(phase)
    fp = os.path.join(r_out, f"phase_{phase}.png")
    scene.render.filepath = fp
    bpy.ops.render.render(write_still=True)
    print(f"Rendered phase_{phase}: {fp}")

print("## OUTPUT ##")
print(f"Blend: {blend_path}")
print(f"Renders: {r_out}")
