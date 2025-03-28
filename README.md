# memory-BE

# Memory-BE

> Cross-Platform FFmpeg + Google Speech-to-Text Backend

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

src/main/resources/bin/ ├── ffmpeg-win.exe (Windows용) ├── ffmpeg-mac (Mac용) └── ffmpeg-linux (Linux용)

---

## ✅ Git LFS 사용 안내 (필수)

본 프로젝트는 ffmpeg 바이너리를 Git LFS로 관리합니다.

> ⚠ 팀원 및 서버는 반드시 아래를 수행해야 합니다.

### 1️⃣ Git LFS 설치 (한번만)
git lfs install

### 2️⃣ 프로젝트 Clone 및 Pull
git clone [레포주소]
cd memory-BE
git lfs pull


## ✅ 프로젝트 세팅

### 1️⃣ Google Speech API 인증키
src/main/resources/keys/google-credentials.json

### 2️⃣ Mac / Linux 필수
chmod +x src/main/resources/bin/ffmpeg-*
실행권한 부여 (Windows는 필요 없음)


## ✅ 실행 방법

./gradlew clean bootRun


## ✅ 커밋 & 푸시 가이드 (LFS 적용 후)

git add .
git add --chmod=+x src/main/resources/bin/ffmpeg-*
git commit -m "message"
git push