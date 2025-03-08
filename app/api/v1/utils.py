import json
import os
import logging
from datetime import datetime
from typing import Dict, Any, Optional
import httpx

logger = logging.getLogger(__name__)

# 결과 저장 디렉토리 설정
RESULTS_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), "results")
os.makedirs(RESULTS_DIR, exist_ok=True)

async def save_analysis_result(result: Dict[str, Any], prefix: str = "analysis") -> str:
    """분석 결과를 JSON 파일로 저장합니다."""
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"{prefix}_{timestamp}.json"
    filepath = os.path.join(RESULTS_DIR, filename)
    
    try:
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(result, f, ensure_ascii=False, indent=2)
        logger.info(f"분석 결과가 저장되었습니다: {filepath}")
        return filepath
    except Exception as e:
        logger.error(f"분석 결과 저장 중 오류 발생: {str(e)}")
        return ""

async def send_to_backend(data: Dict[str, Any], endpoint: str, auth_token: Optional[str] = None) -> Dict[str, Any]:
    """분석 결과를 Spring 백엔드로 전송합니다."""
    # 백엔드 URL 설정 (환경 변수에서 가져오거나 기본값 사용)
    backend_url = os.getenv("BACKEND_URL", "http://localhost:8080")
    full_url = f"{backend_url}{endpoint}"
    
    headers = {
        "Content-Type": "application/json"
    }
    
    # 인증 토큰이 제공된 경우 헤더에 추가
    if auth_token:
        headers["Authorization"] = f"Bearer {auth_token}"
    
    try:
        logger.info(f"백엔드로 데이터 전송 중: {full_url}")
        async with httpx.AsyncClient() as client:
            response = await client.post(
                full_url,
                json=data,
                headers=headers,
                timeout=30.0
            )
            
            if response.status_code >= 400:
                logger.error(f"백엔드 응답 오류: {response.status_code}, {response.text}")
                return {
                    "success": False,
                    "status_code": response.status_code,
                    "message": f"백엔드 응답 오류: {response.text}"
                }
            
            logger.info(f"백엔드 응답 성공: {response.status_code}")
            return {
                "success": True,
                "status_code": response.status_code,
                "data": response.json() if response.text else {}
            }
    except Exception as e:
        logger.error(f"백엔드 통신 중 오류 발생: {str(e)}")
        return {
            "success": False,
            "message": f"백엔드 통신 중 오류 발생: {str(e)}"
        } 