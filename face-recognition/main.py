# main.py
from fastapi import FastAPI, UploadFile, File, HTTPException
from typing import List
from pydantic import BaseModel
import uvicorn

from model_loader import ModelLoader
from embedding_service import generate_embedding_from_frames_bytes
from verification_service import verify_against_stored

app = FastAPI(title="FaceEmbedService")

class EmbeddingResponse(BaseModel):
    embedding: str

class VerifyResponse(BaseModel):
    verified: bool

@app.on_event("startup")
def startup_event():
    # load models once (CPU)
    loader = ModelLoader.get_instance()
    loader.load(ctx_id=-1)  # -1 forces CPU in insightface

@app.post("/generate-embedding", response_model=EmbeddingResponse)
async def generate_embedding(frames: List[UploadFile] = File(...)):
    # read all bytes
    if not frames:
        raise HTTPException(status_code=400, detail="frames list is empty")

    bytes_list = []
    for idx, f in enumerate(frames):
        try:
            b = await f.read()
            if not b:
                raise HTTPException(status_code=400, detail=f"frame_{idx}_empty")
            bytes_list.append(b)
        finally:
            # ensure file resources are closed
            try:
                await f.close()
            except Exception:
                pass

    try:
        emb_str = generate_embedding_from_frames_bytes(bytes_list)
    except ValueError as e:
        # error messages are of form "frame_i_error:xxx" or specific codes
        msg = str(e)
        raise HTTPException(status_code=400, detail=msg)

    return EmbeddingResponse(embedding=emb_str)

@app.post("/verify", response_model=VerifyResponse)
async def verify(frames: List[UploadFile] = File(...), stored_embedding: str = File(...)):
    if not frames:
        raise HTTPException(status_code=400, detail="frames list is empty")

    bytes_list = []
    for idx, f in enumerate(frames):
        try:
            b = await f.read()
            if not b:
                # skip but if all fail it'll error later
                continue
            bytes_list.append(b)
        finally:
            try:
                await f.close()
            except Exception:
                pass

    try:
        verified = verify_against_stored(stored_embedding, bytes_list)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

    return VerifyResponse(verified=verified)

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, log_level="info")