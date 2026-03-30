"""Create ceramic_tree.blend — Fantastical tree with ceramic plate foliage.

Generates a 7-phase model (phase_1 to phase_6 + victory) and renders each phase
as a 512x512 PNG with transparent background.

Usage:
  blender.exe --background --python create_ceramic_tree.py -- '{"output_dir":"path"}'
"""
import bpy
import bmesh
import json
import math
import os
import sys
from mathutils import Vector, Euler, Matrix, noise

# ─── Parse args ───────────────────────────────────────────────────────────────
try:
    args = json.loads(sys.argv[sys.argv.index("--") + 1])
except (ValueError, IndexError):
    args = {}

script_dir = os.path.dirname(os.path.abspath(__file__))
pipeline_dir = os.path.dirname(script_dir)
project_dir = os.path.dirname(pipeline_dir)
output_dir = args.get("output_dir", os.path.join(pipeline_dir, "output"))
render_dir = args.get("render_dir", "")  # If empty, will set below
resolution = args.get("resolution", 512)

# Resolve relative paths against project root
if not os.path.isabs(output_dir):
    output_dir = os.path.join(project_dir, output_dir)
if render_dir and not os.path.isabs(render_dir):
    render_dir = os.path.join(project_dir, render_dir)

os.makedirs(output_dir, exist_ok=True)

# ─── Clear scene ──────────────────────────────────────────────────────────────
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete()
for mesh in bpy.data.meshes:
    bpy.data.meshes.remove(mesh)
for mat in bpy.data.materials:
    bpy.data.materials.remove(mat)

scene = bpy.context.scene

# ─── Helper: tag objects with phase ───────────────────────────────────────────
# Objects are tagged with custom property "min_phase" (1-7, where 7=victory)
def tag_phase(obj, phase):
    obj["min_phase"] = phase

def set_phase_visibility(phase):
    """Show only objects whose min_phase <= given phase."""
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

def create_wood_material():
    """Weathered dark bark material."""
    mat = bpy.data.materials.new(name="WeatheredWood")
    mat.use_nodes = True
    nt = mat.node_tree
    nt.nodes.clear()

    out = nt.nodes.new('ShaderNodeOutputMaterial')
    out.location = (800, 0)

    bsdf = nt.nodes.new('ShaderNodeBsdfPrincipled')
    bsdf.location = (400, 0)
    nt.links.new(bsdf.outputs[0], out.inputs[0])

    # Texture coordinate
    tc = nt.nodes.new('ShaderNodeTexCoord')
    tc.location = (-600, 0)

    # Noise texture for bark pattern (replaces deprecated Musgrave in 5.1)
    noise_bark = nt.nodes.new('ShaderNodeTexNoise')
    noise_bark.location = (-200, 100)
    noise_bark.inputs['Scale'].default_value = 8.0
    noise_bark.inputs['Detail'].default_value = 12.0
    noise_bark.inputs['Roughness'].default_value = 0.7
    noise_bark.inputs['Distortion'].default_value = 1.5
    nt.links.new(tc.outputs['Object'], noise_bark.inputs['Vector'])

    # Color ramp for bark colors
    ramp = nt.nodes.new('ShaderNodeValToRGB')
    ramp.location = (100, 100)
    ramp.color_ramp.elements[0].position = 0.3
    ramp.color_ramp.elements[0].color = (0.04, 0.03, 0.025, 1.0)  # Dark bark
    ramp.color_ramp.elements[1].position = 0.7
    ramp.color_ramp.elements[1].color = (0.12, 0.08, 0.06, 1.0)  # Light bark
    nt.links.new(noise_bark.outputs['Fac'], ramp.inputs['Fac'])
    nt.links.new(ramp.outputs['Color'], bsdf.inputs['Base Color'])

    # Roughness variation
    rough_ramp = nt.nodes.new('ShaderNodeValToRGB')
    rough_ramp.location = (100, -100)
    rough_ramp.color_ramp.elements[0].position = 0.2
    rough_ramp.color_ramp.elements[0].color = (0.75, 0.75, 0.75, 1.0)
    rough_ramp.color_ramp.elements[1].position = 0.8
    rough_ramp.color_ramp.elements[1].color = (0.95, 0.95, 0.95, 1.0)
    nt.links.new(noise_bark.outputs['Fac'], rough_ramp.inputs['Fac'])
    nt.links.new(rough_ramp.outputs['Color'], bsdf.inputs['Roughness'])

    return mat


def create_ceramic_material(name="WeatheredCeramic", darken=0.0):
    """Cream-white ceramic with subtle variation."""
    mat = bpy.data.materials.new(name=name)
    mat.use_nodes = True
    nt = mat.node_tree
    nt.nodes.clear()

    out = nt.nodes.new('ShaderNodeOutputMaterial')
    out.location = (800, 0)

    bsdf = nt.nodes.new('ShaderNodeBsdfPrincipled')
    bsdf.location = (400, 0)
    nt.links.new(bsdf.outputs[0], out.inputs[0])

    tc = nt.nodes.new('ShaderNodeTexCoord')
    tc.location = (-400, 0)

    # Noise for surface variation
    noise_tex = nt.nodes.new('ShaderNodeTexNoise')
    noise_tex.location = (-200, 100)
    noise_tex.inputs['Scale'].default_value = 15.0
    noise_tex.inputs['Detail'].default_value = 4.0
    noise_tex.inputs['Roughness'].default_value = 0.5
    nt.links.new(tc.outputs['Object'], noise_tex.inputs['Vector'])

    # Color ramp for ceramic
    ramp = nt.nodes.new('ShaderNodeValToRGB')
    ramp.location = (100, 100)
    base_v = max(0.0, 0.85 - darken)
    dark_v = max(0.0, 0.72 - darken)
    ramp.color_ramp.elements[0].position = 0.3
    ramp.color_ramp.elements[0].color = (dark_v, dark_v * 0.95, dark_v * 0.88, 1.0)
    ramp.color_ramp.elements[1].position = 0.7
    ramp.color_ramp.elements[1].color = (base_v, base_v * 0.95, base_v * 0.88, 1.0)
    nt.links.new(noise_tex.outputs['Fac'], ramp.inputs['Fac'])
    nt.links.new(ramp.outputs['Color'], bsdf.inputs['Base Color'])

    bsdf.inputs['Roughness'].default_value = 0.3
    bsdf.inputs['Specular IOR Level'].default_value = 0.6

    return mat


def create_ceramic_border_material():
    """Darker ceramic for ornamental plate borders."""
    return create_ceramic_material("CeramicBorder", darken=0.3)


def create_crystal_pith_material():
    """Glowing NeonCyan crystal for victory phase."""
    mat = bpy.data.materials.new(name="CrystalPith")
    mat.use_nodes = True
    nt = mat.node_tree
    nt.nodes.clear()

    out = nt.nodes.new('ShaderNodeOutputMaterial')
    out.location = (800, 0)

    # Mix emission + glass
    mix = nt.nodes.new('ShaderNodeMixShader')
    mix.location = (600, 0)
    mix.inputs['Fac'].default_value = 0.6
    nt.links.new(mix.outputs[0], out.inputs[0])

    emit = nt.nodes.new('ShaderNodeEmission')
    emit.location = (300, 100)
    emit.inputs['Color'].default_value = (0.0, 0.94, 1.0, 1.0)  # NeonCyan
    emit.inputs['Strength'].default_value = 3.0
    nt.links.new(emit.outputs[0], mix.inputs[1])

    glass = nt.nodes.new('ShaderNodeBsdfGlass')
    glass.location = (300, -100)
    glass.inputs['Color'].default_value = (0.0, 0.94, 1.0, 1.0)
    glass.inputs['Roughness'].default_value = 0.1
    nt.links.new(glass.outputs[0], mix.inputs[2])

    # Voronoi pattern
    tc = nt.nodes.new('ShaderNodeTexCoord')
    tc.location = (-400, 0)
    voronoi = nt.nodes.new('ShaderNodeTexVoronoi')
    voronoi.location = (-100, 0)
    voronoi.inputs['Scale'].default_value = 8.0
    voronoi.inputs['Randomness'].default_value = 0.8
    nt.links.new(tc.outputs['Object'], voronoi.inputs['Vector'])

    # Modulate emission strength with voronoi
    math_mul = nt.nodes.new('ShaderNodeMath')
    math_mul.operation = 'MULTIPLY'
    math_mul.location = (100, 0)
    math_mul.inputs[1].default_value = 5.0
    nt.links.new(voronoi.outputs['Distance'], math_mul.inputs[0])
    nt.links.new(math_mul.outputs[0], emit.inputs['Strength'])

    return mat


def create_glow_ceramic_material():
    """Ceramic with subtle cyan edge emission for victory."""
    mat = bpy.data.materials.new(name="GlowCeramic")
    mat.use_nodes = True
    nt = mat.node_tree
    nt.nodes.clear()

    out = nt.nodes.new('ShaderNodeOutputMaterial')
    out.location = (800, 0)

    mix = nt.nodes.new('ShaderNodeMixShader')
    mix.location = (600, 0)
    mix.inputs['Fac'].default_value = 0.15  # Mostly ceramic, slight glow
    nt.links.new(mix.outputs[0], out.inputs[0])

    # Base ceramic
    bsdf = nt.nodes.new('ShaderNodeBsdfPrincipled')
    bsdf.location = (300, 150)
    bsdf.inputs['Base Color'].default_value = (0.85, 0.82, 0.75, 1.0)
    bsdf.inputs['Roughness'].default_value = 0.3
    nt.links.new(bsdf.outputs[0], mix.inputs[1])

    # Edge glow
    emit = nt.nodes.new('ShaderNodeEmission')
    emit.location = (300, -100)
    emit.inputs['Color'].default_value = (0.0, 0.94, 1.0, 1.0)
    emit.inputs['Strength'].default_value = 1.5
    nt.links.new(emit.outputs[0], mix.inputs[2])

    # Fresnel to make glow appear on edges
    fresnel = nt.nodes.new('ShaderNodeFresnel')
    fresnel.location = (300, 0)
    fresnel.inputs['IOR'].default_value = 1.8
    nt.links.new(fresnel.outputs[0], mix.inputs['Fac'])

    return mat


# Create all materials
mat_wood = create_wood_material()
mat_ceramic = create_ceramic_material()
mat_ceramic_border = create_ceramic_border_material()
mat_crystal = create_crystal_pith_material()
mat_glow_ceramic = create_glow_ceramic_material()


# ═══════════════════════════════════════════════════════════════════════════════
# GEOMETRY BUILDERS
# ═══════════════════════════════════════════════════════════════════════════════

def make_trunk():
    """Create twisted S-shaped trunk with roots."""
    # Main trunk via curve
    curve_data = bpy.data.curves.new('TrunkCurve', type='CURVE')
    curve_data.dimensions = '3D'
    curve_data.resolution_u = 12
    curve_data.bevel_depth = 0.12
    curve_data.bevel_resolution = 6

    spline = curve_data.splines.new('BEZIER')
    spline.bezier_points.add(4)  # 5 points total for S-shape

    points = [
        # (position, handle_left_offset, handle_right_offset, radius)
        (Vector((0, 0, -0.8)), Vector((0, 0.1, -0.15)), Vector((0, -0.1, 0.15)), 1.6),
        (Vector((0.08, 0.02, -0.4)), Vector((-0.05, 0, -0.12)), Vector((0.05, 0, 0.12)), 1.2),
        (Vector((-0.06, -0.02, 0.0)), Vector((0.04, 0, -0.12)), Vector((-0.04, 0, 0.12)), 1.0),
        (Vector((0.1, 0.03, 0.4)), Vector((-0.05, 0, -0.12)), Vector((0.05, 0, 0.12)), 0.7),
        (Vector((0.0, 0.0, 0.8)), Vector((-0.03, 0, -0.1)), Vector((0.03, 0, 0.1)), 0.4),
    ]

    for i, (pos, hl, hr, rad) in enumerate(points):
        bp = spline.bezier_points[i]
        bp.co = pos
        bp.handle_left = pos + hl
        bp.handle_right = pos + hr
        bp.radius = rad

    trunk_curve = bpy.data.objects.new('TrunkCurve', curve_data)
    bpy.context.collection.objects.link(trunk_curve)

    # Convert to mesh
    bpy.context.view_layer.objects.active = trunk_curve
    trunk_curve.select_set(True)
    bpy.ops.object.convert(target='MESH')
    trunk = bpy.context.active_object
    trunk.name = "Trunk"

    # Add subdivision for smoothness
    sub = trunk.modifiers.new("Subdiv", 'SUBSURF')
    sub.levels = 2
    sub.render_levels = 2

    # Displacement for bark texture
    disp_tex = bpy.data.textures.new("BarkDisp", 'MUSGRAVE')
    disp_tex.musgrave_type = 'RIDGED_MULTIFRACTAL'
    disp_tex.noise_scale = 0.3
    disp_tex.dimension_max = 1.0

    disp = trunk.modifiers.new("BarkDisplacement", 'DISPLACE')
    disp.texture = disp_tex
    disp.strength = 0.015
    disp.texture_coords = 'OBJECT'

    trunk.data.materials.append(mat_wood)
    tag_phase(trunk, 1)

    # Knots (bulges on trunk)
    knot_positions = [
        Vector((0.14, 0.05, -0.2)),
        Vector((-0.1, 0.06, 0.15)),
        Vector((0.12, -0.04, 0.45)),
    ]
    for i, pos in enumerate(knot_positions):
        bpy.ops.mesh.primitive_uv_sphere_add(
            radius=0.06, segments=12, ring_count=8, location=pos
        )
        knot = bpy.context.active_object
        knot.name = f"Knot_{i}"
        knot.scale = (1.3, 1.0, 0.8)
        knot.data.materials.append(mat_wood)
        tag_phase(knot, 1)

    return trunk


def make_roots(trunk):
    """Create exposed roots at the base."""
    roots = []
    root_configs = [
        # (angle_deg, length, curve_amount, thickness)
        (30, 0.35, 0.08, 0.05),
        (100, 0.30, -0.06, 0.04),
        (180, 0.38, 0.1, 0.055),
        (250, 0.32, -0.07, 0.045),
        (330, 0.28, 0.05, 0.035),
    ]

    for i, (angle, length, curve, thick) in enumerate(root_configs):
        rad = math.radians(angle)
        x = math.cos(rad) * 0.08
        y = math.sin(rad) * 0.08

        # Root as a curve
        cd = bpy.data.curves.new(f'Root_{i}_curve', type='CURVE')
        cd.dimensions = '3D'
        cd.bevel_depth = thick
        cd.bevel_resolution = 4
        cd.resolution_u = 8

        sp = cd.splines.new('BEZIER')
        sp.bezier_points.add(1)  # 2 points

        sp.bezier_points[0].co = Vector((x, y, -0.7))
        sp.bezier_points[0].handle_right = Vector((x + math.cos(rad) * 0.1, y + math.sin(rad) * 0.1, -0.75))
        sp.bezier_points[0].handle_left = Vector((x, y, -0.65))
        sp.bezier_points[0].radius = 1.2

        end_x = x + math.cos(rad) * length
        end_y = y + math.sin(rad) * length
        sp.bezier_points[1].co = Vector((end_x, end_y, -0.85))
        sp.bezier_points[1].handle_left = Vector((end_x - math.cos(rad) * 0.08, end_y - math.sin(rad) * 0.08, -0.82))
        sp.bezier_points[1].handle_right = Vector((end_x + math.cos(rad) * 0.05, end_y + math.sin(rad) * 0.05, -0.88))
        sp.bezier_points[1].radius = 0.3

        root_obj = bpy.data.objects.new(f'Root_{i}', cd)
        bpy.context.collection.objects.link(root_obj)

        bpy.context.view_layer.objects.active = root_obj
        root_obj.select_set(True)
        bpy.ops.object.convert(target='MESH')
        root_mesh = bpy.context.active_object
        root_mesh.name = f"Root_{i}"
        root_mesh.data.materials.append(mat_wood)
        tag_phase(root_mesh, 1)
        roots.append(root_mesh)

    return roots


def make_branch(name, start, end, thickness=0.04, phase=2):
    """Create an organic branch from start to end point."""
    cd = bpy.data.curves.new(f'{name}_curve', type='CURVE')
    cd.dimensions = '3D'
    cd.bevel_depth = thickness
    cd.bevel_resolution = 4
    cd.resolution_u = 8

    sp = cd.splines.new('BEZIER')
    sp.bezier_points.add(1)

    mid = (start + end) / 2.0
    # Add natural curve
    mid.x += (end.x - start.x) * 0.2
    mid.z += 0.05

    sp.bezier_points[0].co = start
    sp.bezier_points[0].handle_right = start + (mid - start) * 0.5
    sp.bezier_points[0].handle_left = start - (mid - start) * 0.3
    sp.bezier_points[0].radius = 1.0

    sp.bezier_points[1].co = end
    sp.bezier_points[1].handle_left = end - (end - mid) * 0.5
    sp.bezier_points[1].handle_right = end + (end - mid) * 0.3
    sp.bezier_points[1].radius = 0.3

    branch_obj = bpy.data.objects.new(name, cd)
    bpy.context.collection.objects.link(branch_obj)

    bpy.context.view_layer.objects.active = branch_obj
    branch_obj.select_set(True)
    bpy.ops.object.convert(target='MESH')
    branch = bpy.context.active_object
    branch.name = name
    branch.data.materials.append(mat_wood)
    tag_phase(branch, phase)
    return branch


def make_plate(name, location, radius=0.15, rotation=(0, 0, 0), phase=3, material=None):
    """Create a concave ceramic plate with raised rim."""
    if material is None:
        material = mat_ceramic

    # Create torus for the rim
    bpy.ops.mesh.primitive_torus_add(
        major_radius=radius,
        minor_radius=radius * 0.08,
        major_segments=32,
        minor_segments=8,
        location=location
    )
    rim = bpy.context.active_object
    rim.name = f"{name}_rim"
    rim.rotation_euler = Euler(rotation)
    rim.data.materials.append(mat_ceramic_border)
    tag_phase(rim, phase)

    # Create disc center (slightly concave)
    bpy.ops.mesh.primitive_uv_sphere_add(
        radius=radius * 0.95,
        segments=24,
        ring_count=12,
        location=location
    )
    disc = bpy.context.active_object
    disc.name = f"{name}_disc"
    disc.scale = (1.0, 1.0, 0.12)
    disc.rotation_euler = Euler(rotation)
    disc.data.materials.append(material)
    tag_phase(disc, phase)

    return rim, disc


def make_bowl(name, location, radius=0.08, rotation=(0, 0, 0), phase=4):
    """Create a small bowl/cup."""
    bpy.ops.mesh.primitive_uv_sphere_add(
        radius=radius,
        segments=16,
        ring_count=8,
        location=location
    )
    bowl = bpy.context.active_object
    bowl.name = name

    # Cut top half and add thickness
    bpy.ops.object.mode_set(mode='EDIT')
    bm = bmesh.from_edit_mesh(bowl.data)
    verts_to_delete = [v for v in bm.verts if v.co.z > 0.001]
    bmesh.ops.delete(bm, geom=verts_to_delete, context='VERTS')
    bmesh.update_edit_mesh(bowl.data)
    bpy.ops.object.mode_set(mode='OBJECT')

    # Solidify for wall thickness
    solid = bowl.modifiers.new("Solidify", 'SOLIDIFY')
    solid.thickness = radius * 0.15
    solid.offset = -1.0

    bowl.rotation_euler = Euler(rotation)
    bowl.data.materials.append(mat_ceramic)
    tag_phase(bowl, phase)
    return bowl


def make_crystal_pith(location):
    """Crystal Pith — glowing cross-section at trunk base (victory only)."""
    bpy.ops.mesh.primitive_cylinder_add(
        radius=0.13, depth=0.04, vertices=32,
        location=location
    )
    pith = bpy.context.active_object
    pith.name = "CrystalPith"
    pith.rotation_euler = Euler((math.radians(8), math.radians(5), 0))
    pith.data.materials.append(mat_crystal)
    tag_phase(pith, 7)

    # Outer glow ring
    bpy.ops.mesh.primitive_torus_add(
        major_radius=0.15, minor_radius=0.02,
        major_segments=48, minor_segments=8,
        location=(location[0], location[1], location[2])
    )
    ring = bpy.context.active_object
    ring.name = "CrystalPithRing"
    ring.rotation_euler = Euler((math.radians(8), math.radians(5), 0))
    ring.data.materials.append(mat_crystal)
    tag_phase(ring, 7)

    return pith


# ═══════════════════════════════════════════════════════════════════════════════
# BUILD THE TREE
# ═══════════════════════════════════════════════════════════════════════════════

# ─── Phase 1: Trunk + roots ──────────────────────────────────────────────────
trunk = make_trunk()
roots = make_roots(trunk)

# ─── Phase 2: Main branches ──────────────────────────────────────────────────
branch_L = make_branch("Branch_L",
    Vector((-0.05, 0, 0.2)), Vector((-0.45, 0.05, 0.55)),
    thickness=0.035, phase=2)
branch_R = make_branch("Branch_R",
    Vector((0.08, 0, 0.3)), Vector((0.5, -0.03, 0.6)),
    thickness=0.03, phase=2)
branch_Top = make_branch("Branch_Top",
    Vector((0.02, 0, 0.7)), Vector((0.08, 0.02, 1.05)),
    thickness=0.025, phase=2)

# Sub-branches
branch_LU = make_branch("Branch_LU",
    Vector((-0.3, 0.03, 0.45)), Vector((-0.55, 0.08, 0.8)),
    thickness=0.02, phase=2)
branch_RU = make_branch("Branch_RU",
    Vector((0.35, -0.02, 0.5)), Vector((0.45, 0.05, 0.85)),
    thickness=0.018, phase=2)
branch_RD = make_branch("Branch_RD",
    Vector((0.2, -0.01, 0.15)), Vector((0.42, 0.04, 0.25)),
    thickness=0.02, phase=2)

# ─── Phase 3: Large plates (5-6) ─────────────────────────────────────────────
large_plates = [
    ("Plate_L1", (-0.48, 0.05, 0.6), 0.18, (0.2, 0.1, 0.3)),
    ("Plate_R1", (0.52, -0.03, 0.65), 0.2, (-0.15, 0.2, -0.1)),
    ("Plate_Top1", (0.08, 0.02, 1.1), 0.16, (0.1, -0.05, 0.2)),
    ("Plate_LU1", (-0.58, 0.08, 0.85), 0.17, (0.25, -0.1, 0.5)),
    ("Plate_RU1", (0.48, 0.05, 0.9), 0.19, (-0.2, 0.15, -0.3)),
    ("Plate_Center", (0.0, 0.06, 0.9), 0.22, (0.05, 0.1, 0.0)),
]
for pname, ploc, prad, prot in large_plates:
    make_plate(pname, ploc, prad, prot, phase=3)

# ─── Phase 4: Medium and small plates + bowls ────────────────────────────────
medium_plates = [
    ("Plate_M1", (-0.35, 0.1, 0.75), 0.12, (0.3, 0.2, 0.1)),
    ("Plate_M2", (0.3, 0.08, 0.78), 0.11, (-0.1, 0.3, 0.2)),
    ("Plate_M3", (-0.2, -0.05, 0.65), 0.1, (0.15, -0.1, 0.4)),
    ("Plate_M4", (0.15, -0.06, 0.95), 0.13, (-0.2, 0.1, -0.2)),
    ("Plate_M5", (-0.45, 0.12, 0.95), 0.1, (0.35, 0.05, 0.6)),
    ("Plate_M6", (0.35, -0.08, 0.45), 0.09, (-0.1, -0.2, 0.1)),
]
for pname, ploc, prad, prot in medium_plates:
    make_plate(pname, ploc, prad, prot, phase=4)

# Small plates
small_plates = [
    ("Plate_S1", (-0.55, 0.15, 0.7), 0.07, (0.4, 0.3, 0.2)),
    ("Plate_S2", (0.55, 0.0, 0.75), 0.06, (-0.3, 0.1, 0.5)),
    ("Plate_S3", (0.0, 0.1, 1.05), 0.08, (0.1, 0.2, 0.3)),
    ("Plate_S4", (-0.3, -0.08, 0.55), 0.065, (0.2, -0.15, 0.4)),
]
for pname, ploc, prad, prot in small_plates:
    make_plate(pname, ploc, prad, prot, phase=4)

# Bowls on horizontal branches
bowls = [
    ("Bowl_1", (0.42, 0.04, 0.28), 0.055, (0.0, 0.0, 0.0)),
    ("Bowl_2", (-0.38, 0.07, 0.5), 0.05, (0.05, 0.0, 0.1)),
    ("Bowl_3", (0.25, 0.03, 0.42), 0.045, (0.0, 0.05, 0.0)),
]
for bname, bloc, brad, brot in bowls:
    make_bowl(bname, bloc, brad, brot, phase=4)

# ─── Phase 5: Ornamental detail + cracks + moss ──────────────────────────────
# Add displacement modifier to some large plates for ornamental borders
for pname in ["Plate_L1", "Plate_R1", "Plate_Center", "Plate_Top1"]:
    rim_name = f"{pname}_rim"
    for obj in bpy.data.objects:
        if obj.name == rim_name:
            # Add noise displacement for ornamental pattern
            dtex = bpy.data.textures.new(f"{pname}_ornament", 'VORONOI')
            dtex.noise_scale = 0.05
            dtex.intensity = 0.8

            dmod = obj.modifiers.new("Ornament", 'DISPLACE')
            dmod.texture = dtex
            dmod.strength = 0.008
            dmod.texture_coords = 'OBJECT'
            # Tag this ornament detail as phase 5
            # (The base rim is already phase 3, ornament just adds detail)

# Moss patches on trunk
moss_positions = [
    Vector((-0.08, 0.12, -0.1)),
    Vector((0.1, 0.1, 0.3)),
    Vector((-0.05, 0.08, 0.55)),
]
for i, pos in enumerate(moss_positions):
    bpy.ops.mesh.primitive_uv_sphere_add(
        radius=0.04, segments=8, ring_count=6, location=pos
    )
    moss = bpy.context.active_object
    moss.name = f"Moss_{i}"
    moss.scale = (1.5, 1.5, 0.3)

    moss_mat = bpy.data.materials.new(f"Moss_{i}")
    moss_mat.use_nodes = True
    moss_nt = moss_mat.node_tree
    bsdf = moss_nt.nodes.get('Principled BSDF')
    if bsdf:
        bsdf.inputs['Base Color'].default_value = (0.08, 0.15, 0.06, 1.0)
        bsdf.inputs['Roughness'].default_value = 0.95
    moss.data.materials.append(moss_mat)
    tag_phase(moss, 5)

# Crack detail plates (additional small cracked plates)
crack_plates = [
    ("Plate_Crack1", (-0.15, 0.12, 0.82), 0.09, (0.3, 0.1, 0.7)),
    ("Plate_Crack2", (0.22, 0.1, 0.72), 0.08, (-0.2, 0.2, -0.4)),
]
for pname, ploc, prad, prot in crack_plates:
    make_plate(pname, ploc, prad, prot, phase=5)

# ─── Phase 6: Dense canopy fill ──────────────────────────────────────────────
fill_plates = [
    ("Plate_F1", (-0.42, 0.15, 0.68), 0.11, (0.5, 0.1, 0.3)),
    ("Plate_F2", (0.42, 0.1, 0.55), 0.1, (-0.3, 0.2, 0.1)),
    ("Plate_F3", (-0.1, 0.15, 0.98), 0.09, (0.2, -0.1, 0.6)),
    ("Plate_F4", (0.2, 0.12, 1.0), 0.12, (-0.15, 0.1, -0.2)),
    ("Plate_F5", (-0.5, 0.05, 0.55), 0.08, (0.4, 0.2, 0.5)),
    ("Plate_F6", (0.1, -0.1, 0.82), 0.07, (0.1, -0.3, 0.2)),
    ("Plate_F7", (-0.25, 0.15, 1.0), 0.1, (0.3, 0.15, 0.4)),
]
for pname, ploc, prad, prot in fill_plates:
    make_plate(pname, ploc, prad, prot, phase=6)

# Additional bowls for phase 6
bowls_p6 = [
    ("Bowl_4", (-0.32, 0.1, 0.38), 0.04, (0.05, 0.0, 0.1)),
    ("Bowl_5", (0.38, 0.06, 0.68), 0.05, (0.0, 0.05, 0.0)),
]
for bname, bloc, brad, brot in bowls_p6:
    make_bowl(bname, bloc, brad, brot, phase=6)

# ─── Phase 7 (Victory): Crystal Pith + glowing plates ────────────────────────
make_crystal_pith((-0.02, 0.12, -0.35))

# Swap ceramics to glow material for victory
# We do this by creating duplicate overlay objects with glow material
# that only appear in phase 7
for pname in ["Plate_L1", "Plate_R1", "Plate_Center", "Plate_Top1",
              "Plate_RU1", "Plate_LU1"]:
    rim_name = f"{pname}_rim"
    for obj in bpy.data.objects:
        if obj.name == rim_name:
            glow_obj = obj.copy()
            glow_obj.data = obj.data.copy()
            glow_obj.name = f"{pname}_glow"
            glow_obj.data.materials.clear()
            glow_obj.data.materials.append(mat_glow_ceramic)
            glow_obj.scale = (1.02, 1.02, 1.02)  # Slightly larger for visible glow
            bpy.context.collection.objects.link(glow_obj)
            tag_phase(glow_obj, 7)


# ═══════════════════════════════════════════════════════════════════════════════
# CAMERA & LIGHTING
# ═══════════════════════════════════════════════════════════════════════════════

# Camera — ortho, front view
bpy.ops.object.camera_add(location=(0, -3.5, 0.15))
cam = bpy.context.active_object
cam.data.type = 'ORTHO'
cam.data.ortho_scale = 2.5
cam.rotation_euler = Euler((math.radians(90), 0, 0))
scene.camera = cam

# Key light — area, white, 45 deg above-right
bpy.ops.object.light_add(type='AREA', location=(1.5, -1.5, 2.0))
key = bpy.context.active_object
key.name = "KeyLight"
key.data.energy = 200
key.data.size = 2.5
key.data.color = (1.0, 1.0, 0.98)
key.rotation_euler = Euler((math.radians(45), math.radians(15), math.radians(-30)))

# Fill light — dim, below-left
bpy.ops.object.light_add(type='AREA', location=(-1.2, -1.0, -0.5))
fill = bpy.context.active_object
fill.name = "FillLight"
fill.data.energy = 60
fill.data.size = 2.0
fill.data.color = (0.9, 0.95, 1.0)
fill.rotation_euler = Euler((math.radians(-20), math.radians(-10), math.radians(20)))

# Rim light — cyan tint for sci-fi coherence
bpy.ops.object.light_add(type='AREA', location=(0, 2.0, 1.0))
rim = bpy.context.active_object
rim.name = "RimLight"
rim.data.energy = 40
rim.data.size = 3.0
rim.data.color = (0.7, 0.95, 1.0)
rim.rotation_euler = Euler((math.radians(-70), 0, math.radians(180)))


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

# Eevee settings
scene.eevee.taa_render_samples = 64

# World (transparent for overlay renders)
scene.world = bpy.data.worlds.new("W")
scene.world.use_nodes = True
bg = scene.world.node_tree.nodes['Background']
bg.inputs['Strength'].default_value = 0.0


# ═══════════════════════════════════════════════════════════════════════════════
# SAVE BLEND & RENDER PHASES
# ═══════════════════════════════════════════════════════════════════════════════

blend_path = os.path.join(output_dir, "ceramic_tree.blend")
bpy.ops.wm.save_as_mainfile(filepath=blend_path)
print(f"Saved blend: {blend_path}")

# Render each phase
if render_dir:
    render_output = render_dir
else:
    render_output = os.path.join(output_dir, "ceramic_tree_renders")
os.makedirs(render_output, exist_ok=True)

phase_names = {1: "phase_1", 2: "phase_2", 3: "phase_3",
               4: "phase_4", 5: "phase_5", 6: "phase_6", 7: "phase_7"}

for phase in range(1, 8):
    set_phase_visibility(phase)
    filepath = os.path.join(render_output, f"{phase_names[phase]}.png")
    scene.render.filepath = filepath
    bpy.ops.render.render(write_still=True)
    print(f"Rendered {phase_names[phase]}: {filepath}")

print("## OUTPUT ##")
print(f"Blend: {blend_path}")
print(f"Renders: {render_output}")
print(f"Phases: 1-6 + victory (phase_7)")
