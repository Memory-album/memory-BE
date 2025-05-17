# Memory-BE

> Cross-Platform FFmpeg + Google Speech-to-Text Backend!!

---

## ✅ 개발 환경

| Tool | Version |
|------|---------|
| Java | 17+ |
| Spring Boot | 3.x |
| Gradle | 8.x |
| Google Speech-to-Text | V1 API |
| FFmpeg | Git LFS로 관리 |

---

## ✅ 프로젝트 구조

src/main/resources/bin/ ├── ffmpeg-win.exe (Windows 용) ├── ffmpeg-mac (Mac 용) └── ffmpeg-linux (Linux 용)

---

## ✅ Git LFS 사용 안내 (필수)
본 프로젝트는 ffmpeg 바이너리를 Git LFS로 관리합니다.

### 1️⃣ Git LFS 설치 (한 번만)
git lfs install

### 2️⃣ 프로젝트 Clone 및 LFS Pull
git clone [레포주소]
cd memory-BE
git lfs pull

---

## ✅ 프로젝트 세팅

### 1️⃣ Google Speech API 인증키
다음 경로에 인증키 파일을 넣어주세요.

src/main/resources/keys/google-credentials.json
⚠ 이 파일은 Git에 포함되어 있지 않습니다. (개별 배포)

### 2️⃣ Mac / Linux 사용자 필수 설정
chmod +x src/main/resources/bin/ffmpeg-*
실행권한 부여
(Windows 환경에서는 필요 없음)

---

## ✅ 실행 방법
./gradlew clean bootRun

---

## ✅ 커밋 & 푸시 가이드 (LFS 적용 후)
git add .
git add --chmod=+x src/main/resources/bin/ffmpeg-*
git commit -m "your message"
git push

---

⚠ 참고사항
- 모든 팀원은 git lfs install을 반드시 먼저 실행해야 합니다.
- Clone 직후 git lfs pull 필수
- Mac, Linux 환경은 chmod +x로 실행 권한 부여 필수
- ffmpeg는 Git LFS로 관리되어, GitHub 파일 용량 제한을 안전하게 해결

