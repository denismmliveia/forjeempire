"""Blender script: Decompose a 3D model into 6 progressive phases.

Modes:
  tagged  — objects have _p{N} suffixes (e.g. Bee_Wing_p3)
  auto    — heuristic assignment based on volume, centrality, vertex count
  manual  — JSON manifest maps object names to phases

Objects prefixed with ENV_ are always visible (environment).
"""
import bpy
import json
import math
import sys
from pathlib import Path
from mathutils import Vector

args = json.loads(sys.argv[sys.argv.index("--") + 1])

output_dir = Path(args["output_dir"])
output_dir.mkdir(parents=True, exist_ok=True)
mode = args.get("mode", "auto")
model_name = args.get("model_name", Path(bpy.data.filepath).stem)
render_phases = args.get("render_phases", True)
render_resolution = args.get("render_resolution", 512)
simplify_materials = args.get("simplify_materials", True)
manifest_input = args.get("manifest_json", "")


# ── Helpers ──────────────────────────────────────────────────────

def get_mesh_objects():
    """Return mesh objects excluding ENV_ prefixed ones."""
    return [o for o in bpy.data.objects if o.type == "MESH" and not o.name.startswith("ENV_")]


def bbox_volume(obj):
    """Compute bounding box volume of an object."""
    bb = [obj.matrix_world @ Vector(c) for c in obj.bound_box]
    dims = [max(v[i] for v in bb) - min(v[i] for v in bb) for i in range(3)]
    return dims[0] * dims[1] * dims[2]


def bbox_center(obj):
    """Compute bounding box center in world space."""
    bb = [obj.matrix_world @ Vector(c) for c in obj.bound_box]
    return Vector([sum(v[i] for v in bb) / 8 for i in range(3)])


def scene_centroid(objects):
    """Compute centroid of all objects."""
    if not objects:
        return Vector((0, 0, 0))
    centers = [bbox_center(o) for o in objects]
    return Vector([sum(c[i] for c in centers) / len(centers) for i in range(3)])


# ── Phase assignment ─────────────────────────────────────────────

def assign_tagged(objects):
    """Parse _p{N} suffixes from object names."""
    phase_map = {}
    for obj in objects:
        name = obj.name
        if "_p" in name:
            try:
                phase = int(name.rsplit("_p", 1)[1])
                phase_map[name] = max(1, min(6, phase))
            except ValueError:
                phase_map[name] = 6
        else:
            phase_map[name] = 6
    return phase_map


def assign_auto(objects):
    """Heuristic assignment based on volume, centrality, and vertex count."""
    if not objects:
        return {}

    centroid = scene_centroid(objects)
    total_verts = sum(len(o.data.vertices) for o in objects)

    scores = []
    for obj in objects:
        vol = bbox_volume(obj)
        dist = (bbox_center(obj) - centroid).length
        verts = len(obj.data.vertices)

        scores.append({
            "name": obj.name,
            "volume": vol,
            "distance": dist,
            "vertices": verts,
        })

    max_vol = max(s["volume"] for s in scores) or 1
    max_dist = max(s["distance"] for s in scores) or 1

    for s in scores:
        vol_score = s["volume"] / max_vol
        cent_score = 1.0 - (s["distance"] / max_dist)
        vert_score = s["vertices"] / total_verts if total_verts else 0
        s["combined"] = 0.4 * vol_score + 0.35 * cent_score + 0.25 * vert_score

    scores.sort(key=lambda s: s["combined"], reverse=True)

    # Distribute into 6 phases by percentile
    n = len(scores)
    thresholds = [
        int(n * 0.10),  # Phase 1: top 10%
        int(n * 0.25),  # Phase 2: next 15%
        int(n * 0.45),  # Phase 3: next 20%
        int(n * 0.65),  # Phase 4: next 20%
        int(n * 0.85),  # Phase 5: next 20%
    ]
    # Ensure at least 1 object in phase 1
    thresholds[0] = max(thresholds[0], 1)

    phase_map = {}
    for i, s in enumerate(scores):
        if i < thresholds[0]:
            phase_map[s["name"]] = 1
        elif i < thresholds[1]:
            phase_map[s["name"]] = 2
        elif i < thresholds[2]:
            phase_map[s["name"]] = 3
        elif i < thresholds[3]:
            phase_map[s["name"]] = 4
        elif i < thresholds[4]:
            phase_map[s["name"]] = 5
        else:
            phase_map[s["name"]] = 6

    return phase_map


def assign_manual(objects, manifest_str):
    """Use provided JSON manifest for assignment."""
    manual_map = json.loads(manifest_str)
    phase_map = {}
    for obj in objects:
        # Try exact match, then match without _p suffix
        base_name = obj.name.rsplit("_p", 1)[0] if "_p" in obj.name else obj.name
        if obj.name in manual_map:
            phase_map[obj.name] = manual_map[obj.name]
        elif base_name in manual_map:
            phase_map[obj.name] = manual_map[base_name]
        else:
            phase_map[obj.name] = 6
    return phase_map


# ── Material simplification ─────────────────────────────────────

def create_silhouette_material():
    """Phase 1: Dark silhouette with subtle rim."""
    mat = bpy.data.materials.new("Phase1_Silhouette")
    mat.use_nodes = True
    nodes = mat.node_tree.nodes
    links = mat.node_tree.links
    nodes.clear()

    output = nodes.new("ShaderNodeOutputMaterial")
    bsdf = nodes.new("ShaderNodeBsdfPrincipled")
    bsdf.inputs["Base Color"].default_value = (0.08, 0.08, 0.12, 1.0)
    bsdf.inputs["Roughness"].default_value = 0.9
    bsdf.inputs["Metallic"].default_value = 0.0
    bsdf.inputs["Emission Color"].default_value = (0.15, 0.18, 0.25, 1.0)
    bsdf.inputs["Emission Strength"].default_value = 0.3
    links.new(bsdf.outputs[0], output.inputs[0])
    return mat


def create_flat_material(original_mat):
    """Phase 2-3: Flat color version of original material."""
    mat = bpy.data.materials.new(f"Flat_{original_mat.name}")
    mat.use_nodes = True
    bsdf = mat.node_tree.nodes["Principled BSDF"]

    # Copy base color from original
    orig_bsdf = None
    for node in original_mat.node_tree.nodes:
        if node.type == "BSDF_PRINCIPLED":
            orig_bsdf = node
            break

    if orig_bsdf:
        bsdf.inputs["Base Color"].default_value = orig_bsdf.inputs["Base Color"].default_value[:]
    bsdf.inputs["Roughness"].default_value = 1.0
    bsdf.inputs["Metallic"].default_value = 0.0
    bsdf.inputs["Emission Strength"].default_value = 0.0
    return mat


def simplify_for_phase(phase, objects, phase_map):
    """Apply material simplification based on current phase."""
    if phase >= 5:
        return  # Phases 5-6: keep original materials

    silhouette_mat = None
    flat_cache = {}

    for obj in objects:
        obj_phase = phase_map.get(obj.name, 6)
        if obj_phase > phase:
            continue  # Hidden, skip

        if phase == 1:
            # Silhouette for everything
            if silhouette_mat is None:
                silhouette_mat = create_silhouette_material()
            obj.data.materials.clear()
            obj.data.materials.append(silhouette_mat)

        elif phase in (2, 3):
            # Flat colors
            if obj.data.materials:
                orig = obj.data.materials[0]
                if orig.name not in flat_cache:
                    flat_cache[orig.name] = create_flat_material(orig)
                obj.data.materials.clear()
                obj.data.materials.append(flat_cache[orig.name])

        elif phase == 4:
            # Basic materials: reduce emission, keep some detail
            for slot in obj.material_slots:
                if slot.material:
                    for node in slot.material.node_tree.nodes:
                        if node.type == "BSDF_PRINCIPLED":
                            if node.inputs["Emission Strength"].default_value > 0:
                                node.inputs["Emission Strength"].default_value *= 0.5


# ── Render setup ─────────────────────────────────────────────────

def setup_render(resolution):
    """Configure render for phase previews."""
    scene = bpy.context.scene
    scene.render.engine = 'CYCLES'
    scene.cycles.samples = 64
    scene.cycles.device = 'GPU'
    scene.render.resolution_x = resolution
    scene.render.resolution_y = resolution
    scene.render.film_transparent = True
    scene.render.image_settings.file_format = 'PNG'
    scene.render.image_settings.color_mode = 'RGBA'

    # GPU setup
    try:
        prefs = bpy.context.preferences.addons['cycles'].preferences
        prefs.compute_device_type = 'CUDA'
        prefs.get_devices()
        for d in prefs.devices:
            d.use = d.type != 'CPU'
    except Exception:
        pass


# ── Main decomposition ──────────────────────────────────────────

mesh_objects = get_mesh_objects()

# Assign phases
if mode == "tagged":
    phase_map = assign_tagged(mesh_objects)
elif mode == "manual":
    phase_map = assign_manual(mesh_objects, manifest_input)
else:
    phase_map = assign_auto(mesh_objects)

# Store original materials for restoration
original_materials = {}
for obj in mesh_objects:
    original_materials[obj.name] = [slot.material for slot in obj.material_slots]

# Setup render if needed
if render_phases:
    setup_render(render_resolution)

print("## OUTPUT ##")
print(f"Model: {model_name}")
print(f"Mode: {mode}")
print(f"Mesh objects: {len(mesh_objects)}")
print(f"Phase assignments:")
for phase in range(1, 7):
    objs_in_phase = [n for n, p in phase_map.items() if p == phase]
    print(f"  Phase {phase}: {len(objs_in_phase)} objects - {objs_in_phase}")

# Generate each phase
for phase in range(1, 7):
    # Restore original materials
    for obj in mesh_objects:
        obj.data.materials.clear()
        for mat in original_materials.get(obj.name, []):
            if mat:
                obj.data.materials.append(mat)

    # Set visibility based on phase
    for obj in mesh_objects:
        obj_phase = phase_map.get(obj.name, 6)
        hidden = obj_phase > phase
        obj.hide_render = hidden
        obj.hide_viewport = hidden
        obj.hide_set(hidden)

    # Apply material simplification
    if simplify_materials:
        simplify_for_phase(phase, mesh_objects, phase_map)

    # Save phase .blend
    phase_blend = str(output_dir / f"{model_name}_phase{phase}.blend")
    bpy.ops.wm.save_as_mainfile(filepath=phase_blend, copy=True)
    print(f"Saved: {phase_blend}")

    # Render phase PNG
    if render_phases:
        phase_png = str(output_dir / f"{model_name}_phase{phase}.png")
        bpy.context.scene.render.filepath = phase_png
        bpy.ops.render.render(write_still=True)
        print(f"Rendered: {phase_png}")

# Restore everything to phase 6 state
for obj in mesh_objects:
    obj.hide_render = False
    obj.hide_viewport = False
    obj.hide_set(False)
    obj.data.materials.clear()
    for mat in original_materials.get(obj.name, []):
        if mat:
            obj.data.materials.append(mat)

# Generate manifest
manifest = {
    "model_name": model_name,
    "total_phases": 6,
    "mode_used": mode,
    "phases": {},
    "phase_assignments": phase_map,
}
for phase in range(1, 7):
    manifest["phases"][str(phase)] = {
        "blend_file": f"{model_name}_phase{phase}.blend",
        "render_file": f"{model_name}_phase{phase}.png" if render_phases else None,
        "visible_objects": [n for n, p in phase_map.items() if p <= phase],
        "new_objects": [n for n, p in phase_map.items() if p == phase],
    }

manifest_path = str(output_dir / f"{model_name}_manifest.json")
with open(manifest_path, "w") as f:
    json.dump(manifest, f, indent=2)
print(f"Manifest: {manifest_path}")
print("Decomposition complete!")
