@router.post("/analyze-image-url")
async def analyze_image_from_url(request: ImageUrlRequest) -> Dict[str, Any]:
    """S3 URL로부터 이미지를 분석하고 결과를 반환합니다."""
    try:
        # URL에서 이미지 다운로드
        image_url = request.image_url
        if not image_url:
            raise HTTPException(
                status_code=400, 
                detail={
                    "error_code": "MISSING_IMAGE_URL",
                    "message": "이미지 URL이 제공되지 않았습니다"
                }
            )
            
        logger.info(f"이미지 URL로부터 분석 요청: {image_url}")
        
        try:
            # URL 인코딩 확인 및 처리
            # 이미 인코딩된 URL은 그대로 사용, 인코딩되지 않은 URL은 인코딩 처리
            from urllib.parse import urlparse, quote, unquote
            
            parsed_url = urlparse(image_url)
            path = parsed_url.path
            
            # 경로에 한글이 포함되어 있고 인코딩되지 않은 경우 인코딩
            if '%' not in path and any(ord(c) > 127 for c in path):
                # 경로 부분만 인코딩
                encoded_path = quote(path)
                # URL 재구성
                image_url = f"{parsed_url.scheme}://{parsed_url.netloc}{encoded_path}"
                if parsed_url.query:
                    image_url += f"?{parsed_url.query}"
                logger.info(f"URL 인코딩 처리: {image_url}")
            
            async with httpx.AsyncClient() as client:
                response = await client.get(image_url, timeout=30.0)
                if response.status_code != 200:
                    raise HTTPException(
                        status_code=400, 
                        detail={
                            "error_code": "DOWNLOAD_FAILED",
                            "message": f"이미지를 다운로드할 수 없습니다. 상태 코드: {response.status_code}"
                        }
                    )
                
                image_content = response.content
        except httpx.RequestError as e:
            logger.error(f"이미지 다운로드 오류: {str(e)}")
            raise HTTPException(
                status_code=400, 
                detail={
                    "error_code": "DOWNLOAD_FAILED",
                    "message": f"이미지를 다운로드할 수 없습니다: {str(e)}"
                }
            )
            
        # 이미지 분석
        try:
            analysis_result = await vision_client.analyze_image(image_content)
            logger.info("이미지 URL 분석 완료")
        except Exception as e:
            logger.error(f"이미지 URL 분석 오류: {str(e)}")
            raise HTTPException(
                status_code=500, 
                detail={
                    "error_code": "ANALYSIS_FAILED",
                    "message": "이미지 분석 중 오류가 발생했습니다."
                }
            )
        
        # 분석 결과를 기반으로 질문 생성
        generated_questions = question_generator.generate_questions(analysis_result)
        
        # Spring이 기대하는 응답 구조로 데이터 생성
        response_data = {
            "analysis_result": analysis_result,
            "questions": [
                {
                    "category": q["category"],
                    "level": q["level"],
                    "question": q["question"]
                } for q in generated_questions
            ]
        }
        
        # 분석 결과를 JSON 파일로 저장
        filename = image_url.split('/')[-1].split('?')[0]  # URL에서 파일명 추출
        result_file = await save_analysis_result(
            response_data,
            f"analysis_url_{filename}"
        )
        
        # 백엔드로 전송 (인증 토큰이 있으면 함께 전송)
        backend_response = await send_to_backend(
            response_data,
            "/api/v1/questions/create",
            auth_token=request.auth_token
        )
        
        return response_data
            
    except HTTPException as http_exc:
        # 이미 HTTPException인 경우 그대로 전달
        logger.error(f"HTTP 예외 발생: {http_exc.detail}")
        raise http_exc
    except Exception as e:
        logger.error(f"이미지 URL 분석 중 오류 발생: {str(e)}")
        raise HTTPException(
            status_code=500, 
            detail={
                "error_code": "UNKNOWN_ERROR",
                "message": f"이미지 URL 분석 중 오류가 발생했습니다: {str(e)}"
            }
        ) 