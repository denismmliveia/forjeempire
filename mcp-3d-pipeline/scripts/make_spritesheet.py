"""Pack a directory of PNG frames into a single spritesheet atlas + metadata JSON.

Usage:
    python make_spritesheet.py <frames_dir> <output_png> [--cols N]

Produces:
    output.png       — grid atlas (cols x rows)
    output.json      — { "cols", "rows", "frameCount", "frameWidth", "frameHeight" }
"""
import json
import math
import os
import sys
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    print("ERROR: Pillow not installed. Run: pip install Pillow", file=sys.stderr)
    sys.exit(1)


def make_spritesheet(frames_dir: str, output_png: str, cols: int = 0) -> dict:
    frames_path = Path(frames_dir)
    frame_files = sorted(frames_path.glob("frame_*.png"))

    if not frame_files:
        raise FileNotFoundError(f"No frame_*.png files found in {frames_dir}")

    frame_count = len(frame_files)

    # Auto-calculate grid: prefer squarish layout
    if cols <= 0:
        cols = math.ceil(math.sqrt(frame_count))
    rows = math.ceil(frame_count / cols)

    # Read first frame to get dimensions
    with Image.open(frame_files[0]) as first:
        fw, fh = first.size

    # Create atlas
    atlas = Image.new("RGBA", (cols * fw, rows * fh), (0, 0, 0, 0))

    for i, frame_file in enumerate(frame_files):
        col = i % cols
        row = i // cols
        with Image.open(frame_file) as frame:
            atlas.paste(frame, (col * fw, row * fh))

    # Optimize: quantize to reduce file size
    atlas.save(output_png, optimize=True)

    # Write metadata
    metadata = {
        "cols": cols,
        "rows": rows,
        "frameCount": frame_count,
        "frameWidth": fw,
        "frameHeight": fh,
    }
    json_path = Path(output_png).with_suffix(".json")
    with open(json_path, "w") as f:
        json.dump(metadata, f, indent=2)

    return metadata


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python make_spritesheet.py <frames_dir> <output_png> [--cols N]")
        sys.exit(1)

    frames_dir = sys.argv[1]
    output_png = sys.argv[2]
    cols = 0
    if "--cols" in sys.argv:
        cols = int(sys.argv[sys.argv.index("--cols") + 1])

    meta = make_spritesheet(frames_dir, output_png, cols)
    print(f"Spritesheet created: {output_png}")
    print(f"  Grid: {meta['cols']}x{meta['rows']} ({meta['frameCount']} frames)")
    print(f"  Frame size: {meta['frameWidth']}x{meta['frameHeight']}")
    atlas_size = os.path.getsize(output_png)
    print(f"  File size: {atlas_size / 1024:.1f} KB")
