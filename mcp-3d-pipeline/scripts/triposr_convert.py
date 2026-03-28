"""Convert an image to a 3D model using TripoSR (local GPU)."""
import sys
import os
from pathlib import Path

# Add TripoSR repo to path
TRIPOSR_DIR = str(Path(__file__).parent.parent / "TripoSR")
if TRIPOSR_DIR not in sys.path:
    sys.path.insert(0, TRIPOSR_DIR)


def main():
    if len(sys.argv) < 3:
        print("Usage: triposr_convert.py <image_path> <output_path>")
        sys.exit(1)

    image_path = sys.argv[1]
    output_path = sys.argv[2]

    if not os.path.exists(image_path):
        print(f"ERROR: Image not found: {image_path}")
        sys.exit(1)

    try:
        import torch
        from tsr.system import TSR
        from PIL import Image
        import numpy as np
    except ImportError as e:
        print(f"ERROR: Missing dependency: {e}")
        sys.exit(1)

    device = "cuda" if torch.cuda.is_available() else "cpu"
    print(f"Using device: {device}")

    # Load model (downloads from HuggingFace on first run ~1.5GB)
    print("Loading TripoSR model (first run downloads ~1.5GB)...")
    model = TSR.from_pretrained(
        "stabilityai/TripoSR",
        config_name="config.yaml",
        weight_name="model.ckpt",
    )
    model.to(device)

    # Process image - remove background for better results
    print("Processing image...")
    image = Image.open(image_path).convert("RGB")

    # Try to remove background with rembg for cleaner results
    try:
        from rembg import remove
        print("Removing background...")
        image_rgba = remove(Image.open(image_path))
        # Create white background version
        bg = Image.new("RGBA", image_rgba.size, (255, 255, 255, 255))
        bg.paste(image_rgba, mask=image_rgba.split()[3])
        image = bg.convert("RGB")
        print("Background removed successfully")
    except Exception as e:
        print(f"Background removal skipped: {e}")

    # Run inference
    print("Generating 3D model (this may take 30-60 seconds)...")
    with torch.no_grad():
        scene_codes = model([image], device=device)

    # Extract mesh
    print("Extracting mesh...")
    meshes = model.extract_mesh(scene_codes, resolution=256)
    mesh = meshes[0]

    # Ensure output directory exists
    Path(output_path).parent.mkdir(parents=True, exist_ok=True)

    # Save
    mesh.export(output_path)
    vertex_count = len(mesh.vertices)
    face_count = len(mesh.faces)
    print(f"Saved 3D model to: {output_path}")
    print(f"Vertices: {vertex_count}, Faces: {face_count}")
    print(f"File size: {os.path.getsize(output_path) / 1024:.1f} KB")


if __name__ == "__main__":
    main()
