# verification_service.py
import numpy as np
import json
from typing import List
from model_loader import ModelLoader
from embedding_service import bytes_to_bgr_image, detect_faces, l2_normalize

DEFAULT_THRESHOLD = 0.65  # tune this with validation data

def parse_embedding_string(embedding_str: str) -> np.ndarray:
    """
    Parse the JSON string back to numpy array and L2-normalize.
    Raises ValueError if invalid JSON or shapes mismatch.
    """
    try:
        parsed = json.loads(embedding_str)
    except json.JSONDecodeError:
        raise ValueError("invalid_embedding_string")

    if not isinstance(parsed, list):
        raise ValueError("embedding_not_list")

    arr = np.asarray(parsed, dtype=np.float32)
    if arr.size == 0:
        raise ValueError("empty_embedding")
    arr = l2_normalize(arr)
    return arr

def _face_quality_score(face, img_shape) -> float:
    """
    Compute a heuristic quality score for a detected face:
    - Prefer detection score if present (face.det_score)
    - Otherwise use bbox area normalized by image area
    """
    # detection score (some face objects expose det_score or score)
    score = 0.0
    if hasattr(face, "det_score") and face.det_score is not None:
        score = float(face.det_score)
    elif hasattr(face, "score") and face.score is not None:
        score = float(face.score)
    else:
        # fallback to bbox area
        bbox = getattr(face, "bbox", None)
        if bbox is None:
            try:
                bbox = face["bbox"]
            except Exception:
                bbox = None
        if bbox is not None:
            x1, y1, x2, y2 = bbox[:4]
            area = max(0.0, (x2 - x1) * (y2 - y1))
            img_area = max(1.0, img_shape[0] * img_shape[1])
            score = float(area / img_area)
    return score

def select_best_face_embedding_from_frames(list_of_image_bytes: List[bytes]):
    """
    For /verify: from multiple frames, detect faces, pick the face with highest quality
    (across frames) and return its normalized embedding.
    Raises ValueError on invalid frames or no faces.
    """
    if not list_of_image_bytes:
        raise ValueError("empty_frame_list")

    app = ModelLoader.get_instance().load()

    best_score = -1.0
    best_emb = None

    for idx, b in enumerate(list_of_image_bytes):
        try:
            img = bytes_to_bgr_image(b)
        except Exception as e:
            # skip invalid images but record error if nothing left
            continue

        faces = detect_faces(app, img)
        if not faces:
            continue

        # choose the best face in this frame if multiple (but we prefer single-face per frame generally)
        local_best = None
        local_best_score = -1.0
        for face in faces:
            score = _face_quality_score(face, img.shape)
            if score > local_best_score:
                local_best_score = score
                local_best = face

        if local_best is None:
            continue

        # extract embedding
        emb = getattr(local_best, "embedding", None)
        if emb is None:
            try:
                emb = local_best["embedding"]
            except Exception:
                emb = None

        if emb is None:
            continue

        emb = np.asarray(emb, dtype=np.float32)
        emb = l2_normalize(emb)

        # update global best if this face is higher quality
        if local_best_score > best_score:
            best_score = local_best_score
            best_emb = emb

    if best_emb is None:
        raise ValueError("no_valid_face_found")

    return best_emb

def verify_against_stored(stored_embedding_str: str, frames_bytes: List[bytes], threshold: float = DEFAULT_THRESHOLD) -> bool:
    """
    Parse stored embedding, extract best incoming embedding, compare via dot product (cosine).
    Return True if similarity >= threshold.
    """
    stored = parse_embedding_string(stored_embedding_str)
    incoming = select_best_face_embedding_from_frames(frames_bytes)

    # both are already L2-normalized
    if stored.shape != incoming.shape:
        # try to be robust: if stored is 1D but different length -> invalid
        raise ValueError("embedding_shape_mismatch")

    similarity = float(np.dot(stored, incoming))
    verified = similarity >= float(threshold)
    return verified