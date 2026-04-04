# embedding_service.py
import numpy as np
import cv2
import json
from typing import List

from model_loader import ModelLoader

# helper: l2 normalize safely
def l2_normalize(x: np.ndarray, eps: float = 1e-12) -> np.ndarray:
    norm = np.linalg.norm(x)
    if norm < eps:
        return x
    return x / norm

def bytes_to_bgr_image(image_bytes: bytes) -> np.ndarray:
    arr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(arr, cv2.IMREAD_COLOR)
    if img is None:
        raise ValueError("Unable to decode image bytes")
    return img

def detect_faces(app, img: np.ndarray):
    """
    Returns list of face objects detected by insightface FaceAnalysis.get()
    """
    faces = app.get(img)
    return faces

def extract_embedding_from_single_image_bytes(image_bytes: bytes):
    """
    Detect exactly one face; extract embedding and return L2-normalized numpy array.
    Raises ValueError on no face / multiple faces / decode errors.
    """
    app = ModelLoader.get_instance().load()
    img = bytes_to_bgr_image(image_bytes)
    faces = detect_faces(app, img)

    if not faces:
        raise ValueError("no_face_detected")
    if len(faces) > 1:
        raise ValueError("multiple_faces_detected")

    face = faces[0]
    # face.embedding should be a numpy array (512-d typically)
    emb = getattr(face, "embedding", None)
    if emb is None:
        # fallback: some wrappers may expose embedding as a field
        emb = np.array(face["embedding"]) if isinstance(face, dict) and "embedding" in face else None
    if emb is None:
        raise ValueError("embedding_not_found")

    emb = np.asarray(emb, dtype=np.float32)
    emb = l2_normalize(emb)
    return emb

def average_normalized_embeddings(embeddings: List[np.ndarray]) -> np.ndarray:
    if not embeddings:
        raise ValueError("empty_embeddings")
    stacked = np.stack(embeddings, axis=0)
    avg = np.mean(stacked, axis=0)
    avg = l2_normalize(avg)
    return avg

def generate_embedding_from_frames_bytes(list_of_image_bytes: List[bytes]) -> str:
    """
    For /generate-embedding endpoint:
    - For each frame: detect exactly one face and extract normalized embedding.
    - Average all normalized embeddings and normalize again.
    - Return the JSON-stringified float array (e.g. json.dumps([...])).
    """
    if not list_of_image_bytes:
        raise ValueError("empty_frame_list")

    embeddings = []
    for idx, b in enumerate(list_of_image_bytes):
        try:
            emb = extract_embedding_from_single_image_bytes(b)
        except ValueError as e:
            # bubble message up with context
            raise ValueError(f"frame_{idx}_error:{str(e)}")
        embeddings.append(emb)

    final_emb = average_normalized_embeddings(embeddings)
    emb_list = final_emb.astype(float).tolist()
    return json.dumps(emb_list)