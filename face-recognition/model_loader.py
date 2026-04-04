# model_loader.py
from typing import Optional
import insightface
from insightface.app import FaceAnalysis
import threading

class ModelLoader:
    """
    Singleton loader for the InsightFace FaceAnalysis app.
    Prepares the model once for CPU inference (ctx_id=-1).
    """
    _instance = None
    _lock = threading.Lock()

    def __init__(self, model_name: str = "buffalo_l", det_size=(640, 640)):
        # Public attributes
        self.model_name = model_name
        self.det_size = det_size
        self.app: Optional[FaceAnalysis] = None

    @classmethod
    def get_instance(cls) -> "ModelLoader":
        with cls._lock:
            if cls._instance is None:
                cls._instance = ModelLoader()
            return cls._instance

    def load(self, ctx_id: int = -1):
        """
        Load and prepare FaceAnalysis. Use ctx_id=-1 for CPU.
        """
        if self.app is not None:
            return self.app

        # instantiate FaceAnalysis with both detection and recognition
        # name 'buffalo_l' is a modern pack with detection+recognition components.
        # ctx_id=-1 -> CPU.
        self.app = FaceAnalysis(name=self.model_name, allowed_modules=["detection", "recognition", "landmark"])
        # det_size controls the detection input size
        self.app.prepare(ctx_id=ctx_id, det_size=self.det_size)
        return self.app