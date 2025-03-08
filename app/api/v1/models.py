from typing import Optional
from pydantic import BaseModel

class ImageUrlRequest(BaseModel):
    """이미지 URL 분석 요청 모델"""
    image_url: str
    auth_token: Optional[str] = None 