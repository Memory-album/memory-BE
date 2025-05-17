#!/bin/bash

# 로그 디렉토리 생성
LOG_DIR=~/logs
mkdir -p $LOG_DIR

# 현재 시간을 파일명에 사용
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE=$LOG_DIR/deploy_$TIMESTAMP.log

echo "======== 배포 시작: $(date) ========" | tee -a $LOG_FILE

# 이전에 실행 중인 애플리케이션이 있으면 종료
if pgrep -f "java -jar" > /dev/null; then
  echo "기존 애플리케이션 프로세스 종료 중..." | tee -a $LOG_FILE
  pkill -f "java -jar"
  sleep 5
  
  # 프로세스가 여전히 실행 중인지 확인
  if pgrep -f "java -jar" > /dev/null; then
    echo "정상 종료 실패, 강제 종료 시도 중..." | tee -a $LOG_FILE
    pkill -9 -f "java -jar"
    sleep 2
  fi
fi

# 현재 디렉토리 확인
CURRENT_DIR=$(pwd)
echo "현재 디렉토리: $CURRENT_DIR" | tee -a $LOG_FILE
echo "디렉토리 내용:" | tee -a $LOG_FILE
ls -la | tee -a $LOG_FILE

# JAR 파일 찾기 - 다양한 경로 시도
echo "JAR 파일 검색 중..." | tee -a $LOG_FILE
JAR_FILE=$(find $CURRENT_DIR -name "memory-BE-*.jar" -type f | head -n 1)

# 찾지 못했으면 빌드 디렉토리 확인
if [ -z "$JAR_FILE" ]; then
  echo "현재 디렉토리에서 JAR 파일을 찾을 수 없습니다. 빌드 디렉토리 확인 중..." | tee -a $LOG_FILE
  JAR_FILE=$(find $CURRENT_DIR/build/libs -name "memory-BE-*.jar" -type f 2>/dev/null | head -n 1)
fi

# 상위 디렉토리도 확인
if [ -z "$JAR_FILE" ]; then
  echo "빌드 디렉토리에서도 JAR 파일을 찾을 수 없습니다. 상위 디렉토리 확인 중..." | tee -a $LOG_FILE
  JAR_FILE=$(find ~/deploy -name "memory-BE-*.jar" -type f 2>/dev/null | head -n 1)
fi

# 마지막 시도
if [ -z "$JAR_FILE" ]; then
  echo "상위 디렉토리에서도 JAR 파일을 찾을 수 없습니다. 홈 디렉토리 확인 중..." | tee -a $LOG_FILE
  JAR_FILE=$(find ~ -name "memory-BE-*.jar" -type f 2>/dev/null | head -n 1)
fi

if [ -z "$JAR_FILE" ]; then
  echo "배포할 JAR 파일을 찾을 수 없습니다!" | tee -a $LOG_FILE
  echo "디렉토리 내용:" | tee -a $LOG_FILE
  find ~ -name "*.jar" | tee -a $LOG_FILE
  exit 1
fi

echo "배포할 JAR 파일: $JAR_FILE" | tee -a $LOG_FILE

# JAR 파일 상태 확인
echo "JAR 파일 정보:" | tee -a $LOG_FILE
file $JAR_FILE | tee -a $LOG_FILE

# JAR 파일의 MANIFEST 확인
echo "MANIFEST 파일 내용:" | tee -a $LOG_FILE
unzip -p $JAR_FILE META-INF/MANIFEST.MF | tee -a $LOG_FILE

# JAR 구조 확인 (문제 디버깅용)
echo "JAR 파일 구조 확인:" | tee -a $LOG_FILE
jar tf $JAR_FILE | grep -E "org/springframework/boot/loader|META-INF/MANIFEST.MF|com/min/i/memory_BE/MemoryBeApplication.class" | tee -a $LOG_FILE

# 실행 환경 설정
JAVA_OPTS="-Xms512m -Xmx1024m"
SPRING_OPTS="-Dspring.profiles.active=dev -Ddebug=true"

# Google 자격 증명 설정
echo "Google 자격 증명 설정 중..." | tee -a $LOG_FILE
GOOGLE_CREDS_DIR=~/credentials
mkdir -p $GOOGLE_CREDS_DIR
GOOGLE_CREDS_FILE="$GOOGLE_CREDS_DIR/google-credentials.json"

# 환경 변수 파일에서 자격 증명 정보 가져오기
if [ -f ~/.google-credentials ]; then
  cp ~/.google-credentials $GOOGLE_CREDS_FILE
  chmod 600 $GOOGLE_CREDS_FILE
  echo "Google 자격 증명 파일 준비 완료: $GOOGLE_CREDS_FILE" | tee -a $LOG_FILE
else
  echo "~/.google-credentials 파일이 없습니다. JAR에서 추출을 시도합니다..." | tee -a $LOG_FILE
  # JAR 파일에서 자격 증명 파일 추출
  JAR_CREDS_PATH="BOOT-INF/classes/keys/google-credentials.json"
  
  if unzip -p "$JAR_FILE" "$JAR_CREDS_PATH" > "$GOOGLE_CREDS_FILE" 2>/dev/null; then
    chmod 600 "$GOOGLE_CREDS_FILE"
    echo "JAR 파일에서 Google 자격 증명 파일을 추출했습니다: $GOOGLE_CREDS_FILE" | tee -a $LOG_FILE
  else
    echo "Google 자격 증명 파일을 JAR에서 추출할 수 없습니다." | tee -a $LOG_FILE
  fi
fi

# 환경 변수 설정
ENV_VARS="-DDB_PASSWORD=${DB_PASSWORD:-default_password}"
ENV_VARS="$ENV_VARS -DJWT_SECRET=${JWT_SECRET:-default_secret}"
ENV_VARS="$ENV_VARS -DGMAIL_MAIL_USERNAME=${GMAIL_MAIL_USERNAME:-default_username}"
ENV_VARS="$ENV_VARS -DGMAIL_MAIL_PASSWORD=${GMAIL_MAIL_PASSWORD:-default_password}"
ENV_VARS="$ENV_VARS -DAWS_ACCESS_KEY=${AWS_ACCESS_KEY:-default_key}"
ENV_VARS="$ENV_VARS -DAWS_SECRET_KEY=${AWS_SECRET_KEY:-default_secret}"
ENV_VARS="$ENV_VARS -DAWS_REGION=${AWS_REGION:-ap-northeast-2}"
ENV_VARS="$ENV_VARS -DS3_BUCKET=${S3_BUCKET:-default_bucket}"
ENV_VARS="$ENV_VARS -DNAVER_CLIENT_ID=${NAVER_CLIENT_ID:-default_client_id}"
ENV_VARS="$ENV_VARS -DNAVER_CLIENT_SECRET=${NAVER_CLIENT_SECRET:-default_client_secret}"
ENV_VARS="$ENV_VARS -DKAKAO_CLIENT_ID=${KAKAO_CLIENT_ID:-default_client_id}"
ENV_VARS="$ENV_VARS -DKAKAO_CLIENT_SECRET=${KAKAO_CLIENT_SECRET:-default_client_secret}"
ENV_VARS="$ENV_VARS -DGOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID:-default_client_id}"
ENV_VARS="$ENV_VARS -DGOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET:-default_client_secret}"
ENV_VARS="$ENV_VARS -DFASTAPI_SERVER_URL=${FASTAPI_SERVER_URL:-http://localhost:8000}"

# Google 자격 증명 파일 환경 변수 설정
export GOOGLE_APPLICATION_CREDENTIALS=$GOOGLE_CREDS_FILE

# JAR 파일 실행 - Spring Boot JAR 직접 실행
echo "애플리케이션 시작 중..." | tee -a $LOG_FILE
echo "실행 명령어: java $JAVA_OPTS $SPRING_OPTS $ENV_VARS -jar $JAR_FILE" | tee -a $LOG_FILE

# 백그라운드에서 실행하고 로그를 파일로 리다이렉션
nohup java $JAVA_OPTS $SPRING_OPTS $ENV_VARS -jar $JAR_FILE > $LOG_DIR/app_$TIMESTAMP.log 2>&1 &

# 프로세스 ID 저장
PID=$!
echo "애플리케이션이 PID=$PID로 시작되었습니다." | tee -a $LOG_FILE

# 애플리케이션이 성공적으로 시작되었는지 확인
sleep 20  # 애플리케이션 시작 시간 증가
if ps -p $PID > /dev/null; then
  echo "애플리케이션이 성공적으로 실행 중입니다." | tee -a $LOG_FILE
  echo "로그 확인: tail -f $LOG_DIR/app_$TIMESTAMP.log" | tee -a $LOG_FILE
else
  echo "애플리케이션 시작에 실패했습니다! 로그를 확인하세요." | tee -a $LOG_FILE
  echo "마지막 로그 확인:" | tee -a $LOG_FILE
  tail -n 100 $LOG_DIR/app_$TIMESTAMP.log | tee -a $LOG_FILE
  
  # JAR 파일 내용 확인
  echo "JAR 파일 내용 확인:" | tee -a $LOG_FILE
  jar tf $JAR_FILE | grep -E "META-INF/MANIFEST.MF|\.class$" | head -20 | tee -a $LOG_FILE
  
  # MANIFEST.MF 내용 확인
  echo "MANIFEST.MF 내용:" | tee -a $LOG_FILE
  unzip -p $JAR_FILE META-INF/MANIFEST.MF | tee -a $LOG_FILE
  
  exit 1
fi

echo "======== 배포 완료: $(date) ========" | tee -a $LOG_FILE 