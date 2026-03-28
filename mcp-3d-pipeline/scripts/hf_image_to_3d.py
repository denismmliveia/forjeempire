"""Convert an image to 3D using Hugging Face Inference API (no local GPU needed)."""
import os
import sys
import json
import urllib.request
import urllib.error
import base64


def main():
    if len(sys.argv) < 3:
        print("Usage: hf_image_to_3d.py <image_path> <output_path>")
        sys.exit(1)

    image_path = sys.argv[1]
    output_path = sys.argv[2]

    if not os.path.exists(image_path):
        print(f"ERROR: Image not found: {image_path}")
        sys.exit(1)

    hf_token = os.environ.get("HF_TOKEN", "")
    if not hf_token:
        print("ERROR: Set HF_TOKEN environment variable with your Hugging Face API token.")
        print("Get one free at: https://huggingface.co/settings/tokens")
        sys.exit(1)

    # Read image
    with open(image_path, "rb") as f:
        image_data = f.read()

    # Call HF Inference API
    api_url = "https://api-inference.huggingface.co/models/stabilityai/TripoSR"
    headers = {
        "Authorization": f"Bearer {hf_token}",
        "Content-Type": "application/octet-stream",
    }

    print("Sending image to Hugging Face API...")
    req = urllib.request.Request(api_url, data=image_data, headers=headers)

    try:
        with urllib.request.urlopen(req, timeout=120) as response:
            result = response.read()

        with open(output_path, "wb") as f:
            f.write(result)

        file_size = len(result) / 1024
        print(f"Saved 3D model to: {output_path}")
        print(f"File size: {file_size:.1f} KB")
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", errors="replace")
        print(f"ERROR: API returned {e.code}: {body}")
        sys.exit(1)
    except Exception as e:
        print(f"ERROR: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
