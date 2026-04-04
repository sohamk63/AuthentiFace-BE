import os
import numpy as np
from model_loader import ModelLoader
from embedding_service import generate_embedding_from_frames_bytes
from verification_service import parse_embedding_string, select_best_face_embedding_from_frames
from verification_service import DEFAULT_THRESHOLD
import json


from model_loader import ModelLoader
from embedding_service import bytes_to_bgr_image
from embedding_service import detect_faces




def load_images_from_folder(folder_path):
    images_bytes = []
    for file in os.listdir(folder_path):
        path = os.path.join(folder_path, file)
        if os.path.isfile(path):
            with open(path, "rb") as f:
                images_bytes.append(f.read())
    return images_bytes


def cosine_similarity(a, b):
    return float(np.dot(a, b))


def test():
    print("Loading InsightFace model...")
    ModelLoader.get_instance().load(ctx_id=-1)

    print("\nGenerating stored embedding from genEmb...")
    gen_images = load_images_from_folder("genEmb")
    stored_embedding_str = generate_embedding_from_frames_bytes(gen_images)

    stored_embedding = parse_embedding_string(stored_embedding_str)

    print("Stored embedding dimension:", stored_embedding.shape[0])

    # ----------------------------
    # TEST 1: Same person (check)
    # ----------------------------
    print("\nTesting SAME person (check folder)...")
    check_images = load_images_from_folder("check")
    incoming_emb = select_best_face_embedding_from_frames(check_images)

    sim_same = cosine_similarity(stored_embedding, incoming_emb)

    print("Cosine similarity (same person):", sim_same)
    print("Threshold:", DEFAULT_THRESHOLD)
    print("Verified:", sim_same >= DEFAULT_THRESHOLD)

    # ----------------------------
    # TEST 2: Different person
    # ----------------------------
    print("\nTesting DIFFERENT person (notMe folder)...")
    not_me_images = load_images_from_folder("notMe")
    
    app = ModelLoader.get_instance().load()

    print("\nChecking face detection in notMe images:")
    for idx, b in enumerate(not_me_images):
        img = bytes_to_bgr_image(b)
        faces = app.get(img)
        print(f"Image {idx} - Faces detected:", len(faces))
        
    incoming_emb_not = select_best_face_embedding_from_frames(not_me_images)

    sim_diff = cosine_similarity(stored_embedding, incoming_emb_not)

    print("Cosine similarity (different person):", sim_diff)
    print("Threshold:", DEFAULT_THRESHOLD)
    print("Verified:", sim_diff >= DEFAULT_THRESHOLD)

    print("\nDone.")


if __name__ == "__main__":
    test()
    
    
    
    
    