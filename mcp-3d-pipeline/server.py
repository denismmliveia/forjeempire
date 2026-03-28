"""
MCP 3D Pipeline Server
Integrates Blender CLI + Image-to-3D conversion
"""
import asyncio
import json
import os
import subprocess
import sys
import tempfile
from pathlib import Path

from mcp.server.fastmcp import FastMCP

BLENDER_PATH = r"C:\Program Files\Blender Foundation\Blender 5.1\blender.exe"
SCRIPTS_DIR = Path(__file__).parent / "scripts"
OUTPUT_DIR = Path(__file__).parent / "output"
OUTPUT_DIR.mkdir(exist_ok=True)

mcp = FastMCP(
    "3D Pipeline",
    instructions="Blender automation + Image-to-3D conversion pipeline",
)


def run_blender_script(script_path: str, args: dict = None, blend_file: str = None) -> str:
    cmd = [BLENDER_PATH, "--background"]
    if blend_file:
        cmd.insert(1, blend_file)
    cmd.extend(["--python", script_path])
    if args:
        cmd.extend(["--", json.dumps(args)])

    try:
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=300,
            cwd=str(OUTPUT_DIR),
            encoding="utf-8",
            errors="replace",
        )
        output = result.stdout
        if result.returncode != 0:
            output += f"\nSTDERR:\n{result.stderr}"
        # Extract only lines after "## OUTPUT ##" marker for clean output
        if "## OUTPUT ##" in output:
            output = output.split("## OUTPUT ##")[1].strip()
        return output
    except subprocess.TimeoutExpired:
        return "ERROR: Blender script timed out after 300 seconds"
    except Exception as e:
        return f"ERROR: {e}"


# ─── Blender Tools ───────────────────────────────────────────────

@mcp.tool()
def blender_execute_script(python_code: str, blend_file: str = "") -> str:
    """Execute arbitrary Python code inside Blender (headless mode).
    Use this for any Blender automation: creating objects, modifying scenes,
    generating procedural models, applying modifiers, etc.

    Args:
        python_code: Python code to execute in Blender's Python environment.
                     Has access to `bpy` and all Blender Python API.
        blend_file: Optional path to a .blend file to open before running the script.
    """
    with tempfile.NamedTemporaryFile(mode="w", suffix=".py", delete=False, dir=str(OUTPUT_DIR)) as f:
        # Wrap user code to capture output
        wrapped = (
            "import sys, json\n"
            "try:\n"
            "    # User code\n"
        )
        for line in python_code.splitlines():
            wrapped += f"    {line}\n"
        wrapped += (
            "    print('## OUTPUT ##')\n"
            "    print('Script executed successfully')\n"
            "except Exception as e:\n"
            "    print('## OUTPUT ##')\n"
            "    print(f'ERROR: {e}')\n"
        )
        f.write(wrapped)
        f.flush()
        script_path = f.name

    try:
        return run_blender_script(script_path, blend_file=blend_file or None)
    finally:
        os.unlink(script_path)


@mcp.tool()
def blender_render(
    blend_file: str,
    output_path: str = "",
    engine: str = "CYCLES",
    resolution_x: int = 1920,
    resolution_y: int = 1080,
    samples: int = 128,
    use_gpu: bool = True,
) -> str:
    """Render a Blender scene to an image file.

    Args:
        blend_file: Path to the .blend file to render.
        output_path: Output image path. Defaults to output/render.png.
        engine: Render engine: CYCLES or BLENDER_EEVEE_NEXT.
        resolution_x: Width in pixels.
        resolution_y: Height in pixels.
        samples: Number of render samples.
        use_gpu: Whether to use GPU for rendering (recommended with RTX 5070 Ti).
    """
    if not output_path:
        output_path = str(OUTPUT_DIR / "render.png")

    args = {
        "output_path": output_path,
        "engine": engine,
        "resolution_x": resolution_x,
        "resolution_y": resolution_y,
        "samples": samples,
        "use_gpu": use_gpu,
    }
    script = str(SCRIPTS_DIR / "render.py")
    return run_blender_script(script, args=args, blend_file=blend_file)


@mcp.tool()
def blender_export(
    blend_file: str,
    output_path: str,
    format: str = "GLB",
) -> str:
    """Export a Blender file to another 3D format.

    Args:
        blend_file: Path to the .blend file to export.
        output_path: Output file path (extension determines format if format not set).
        format: Export format: GLB, GLTF, FBX, OBJ, STL, USD, PLY.
    """
    args = {"output_path": output_path, "format": format.upper()}
    script = str(SCRIPTS_DIR / "export.py")
    return run_blender_script(script, args=args, blend_file=blend_file)


@mcp.tool()
def blender_scene_info(blend_file: str) -> str:
    """Get detailed information about a Blender scene without opening the GUI.

    Args:
        blend_file: Path to the .blend file to analyze.
    """
    script = str(SCRIPTS_DIR / "scene_info.py")
    return run_blender_script(script, blend_file=blend_file)


@mcp.tool()
def blender_import_and_process(
    input_file: str,
    output_blend: str = "",
    operations: str = "",
) -> str:
    """Import a 3D model into Blender, optionally process it, and save as .blend.

    Args:
        input_file: Path to 3D model file (OBJ, FBX, GLB, GLTF, STL, PLY).
        output_blend: Where to save the .blend file. Defaults to output/imported.blend.
        operations: JSON string of operations to apply. Supported:
                    {"center": true, "scale": 1.0, "smooth": true, "decimate_ratio": 0.5}
    """
    if not output_blend:
        output_blend = str(OUTPUT_DIR / "imported.blend")

    args = {
        "input_file": input_file,
        "output_blend": output_blend,
        "operations": operations,
    }
    script = str(SCRIPTS_DIR / "import_and_process.py")
    return run_blender_script(script, args=args)


@mcp.tool()
def blender_decompose_phases(
    blend_file: str,
    output_dir: str = "",
    mode: str = "auto",
    manifest_json: str = "",
    model_name: str = "",
    render_phases: bool = True,
    render_resolution: int = 512,
    simplify_materials: bool = True,
) -> str:
    """Decompose a 3D model into 6 progressive phases for Forge Legends.

    Takes a complete .blend file and produces 6 phase variants, where phase 1
    is the simplest silhouette and phase 6 is the complete model.

    Objects with _p{N} suffixes are visible from phase N onward.
    Objects with ENV_ prefix are always visible (environment).

    Args:
        blend_file: Path to the complete .blend file (phase 6 = full model).
        output_dir: Directory for output files. Defaults to output/phases/.
        mode: "auto" (heuristic), "tagged" (reads _p{N} suffixes), "manual" (uses manifest_json).
        manifest_json: JSON mapping object names to phase numbers for manual mode.
        model_name: Name prefix for output files. Derived from blend_file if empty.
        render_phases: If True, render a PNG preview of each phase.
        render_resolution: Resolution (square) for phase preview renders.
        simplify_materials: If True, progressively simplify materials in earlier phases.
    """
    if not output_dir:
        output_dir = str(OUTPUT_DIR / "phases")
    if not model_name:
        model_name = Path(blend_file).stem

    args = {
        "output_dir": output_dir,
        "mode": mode,
        "manifest_json": manifest_json,
        "model_name": model_name,
        "render_phases": render_phases,
        "render_resolution": render_resolution,
        "simplify_materials": simplify_materials,
    }
    script = str(SCRIPTS_DIR / "decompose_phases.py")
    return run_blender_script(script, args=args, blend_file=blend_file)


@mcp.tool()
def blender_create_model(
    description: str,
    output_path: str = "",
    output_format: str = "blend",
) -> str:
    """Create a 3D model procedurally in Blender from a text description.
    Generates Python code to build the model using Blender primitives.

    Args:
        description: What to create, e.g. "a wooden table with 4 legs" or
                     "a low-poly tree with green leaves".
        output_path: Where to save. Defaults to output/model.[format].
        output_format: Output format: blend, glb, fbx, obj, stl.
    """
    if not output_path:
        output_path = str(OUTPUT_DIR / f"model.{output_format.lower()}")

    args = {"description": description, "output_path": output_path, "format": output_format}
    script = str(SCRIPTS_DIR / "create_model.py")
    return run_blender_script(script, args=args)


# ─── Image to 3D Tools ──────────────────────────────────────────

@mcp.tool()
def image_to_3d(
    image_path: str,
    output_path: str = "",
    method: str = "triposr",
    import_to_blender: bool = True,
) -> str:
    """Convert a 2D image to a 3D model.

    Args:
        image_path: Path to the input image (PNG, JPG).
        output_path: Where to save the 3D model. Defaults to output/from_image.obj.
        method: Conversion method: 'triposr' (local GPU) or 'huggingface' (API).
        import_to_blender: If True, also imports into Blender and saves as .blend.
    """
    if not output_path:
        output_path = str(OUTPUT_DIR / "from_image.obj")

    try:
        if method == "triposr":
            result = _run_triposr(image_path, output_path)
        elif method == "huggingface":
            result = _run_huggingface_3d(image_path, output_path)
        else:
            return f"ERROR: Unknown method '{method}'. Use 'triposr' or 'huggingface'."

        if import_to_blender and not result.startswith("ERROR"):
            blend_path = str(Path(output_path).with_suffix(".blend"))
            import_result = blender_import_and_process(
                input_file=output_path,
                output_blend=blend_path,
                operations='{"center": true, "smooth": true}',
            )
            result += f"\nBlender import: {import_result}\nBlend file: {blend_path}"

        return result
    except Exception as e:
        return f"ERROR: {e}"


def _run_triposr(image_path: str, output_path: str) -> str:
    """Run TripoSR locally for image-to-3D conversion."""
    script = str(SCRIPTS_DIR / "triposr_convert.py")
    cmd = [sys.executable, script, image_path, output_path]
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=300)
        if result.returncode != 0:
            return f"ERROR: TripoSR failed:\n{result.stderr}"
        return result.stdout.strip()
    except FileNotFoundError:
        return "ERROR: TripoSR not installed. Install with: pip install tsr"
    except Exception as e:
        return f"ERROR: {e}"


def _run_huggingface_3d(image_path: str, output_path: str) -> str:
    """Use Hugging Face Inference API for image-to-3D."""
    script = str(SCRIPTS_DIR / "hf_image_to_3d.py")
    cmd = [sys.executable, script, image_path, output_path]
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=300)
        if result.returncode != 0:
            return f"ERROR: HuggingFace conversion failed:\n{result.stderr}"
        return result.stdout.strip()
    except Exception as e:
        return f"ERROR: {e}"


@mcp.tool()
def pipeline_concept_to_3d(
    concept_description: str,
    style: str = "3d render, detailed, studio lighting",
    output_dir: str = "",
) -> str:
    """Full pipeline: generates a concept description for image generation,
    then provides instructions for the next steps.

    This tool helps plan the concept-art-to-3D pipeline by preparing
    the workflow steps.

    Args:
        concept_description: What to create, e.g. "a warrior character with armor".
        style: Art style prompt additions for concept art generation.
        output_dir: Directory for output files. Defaults to output/.
    """
    if not output_dir:
        output_dir = str(OUTPUT_DIR)

    Path(output_dir).mkdir(parents=True, exist_ok=True)

    prompt = f"{concept_description}, {style}, front view, white background, centered, full body"

    return json.dumps({
        "status": "pipeline_ready",
        "steps": [
            {
                "step": 1,
                "action": "generate_concept_art",
                "prompt": prompt,
                "note": "Use an image generation AI (DALL-E, Stable Diffusion, Midjourney) with this prompt",
            },
            {
                "step": 2,
                "action": "save_image",
                "path": str(Path(output_dir) / "concept.png"),
                "note": "Save the generated image to this path",
            },
            {
                "step": 3,
                "action": "image_to_3d",
                "input": str(Path(output_dir) / "concept.png"),
                "output": str(Path(output_dir) / "model.obj"),
                "note": "Use the image_to_3d tool with the saved image",
            },
            {
                "step": 4,
                "action": "blender_process",
                "note": "Import into Blender for refinement, materials, and rendering",
            },
        ],
        "optimized_prompt": prompt,
    }, indent=2)


# ─── Utilities ───────────────────────────────────────────────────

@mcp.tool()
def list_output_files() -> str:
    """List all files in the pipeline output directory."""
    files = []
    for f in OUTPUT_DIR.rglob("*"):
        if f.is_file():
            size = f.stat().st_size
            size_str = f"{size / 1024:.1f} KB" if size < 1048576 else f"{size / 1048576:.1f} MB"
            files.append(f"  {f.relative_to(OUTPUT_DIR)} ({size_str})")
    if not files:
        return "Output directory is empty."
    return "Output files:\n" + "\n".join(files)


if __name__ == "__main__":
    mcp.run(transport="stdio")
