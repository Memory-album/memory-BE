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

# JAR 파일 찾기 - 빌드 디렉토리를 포함한 모든 경로 검색
JAR_FILE=$(find ~/deploy -type f -name "memory-BE-*.jar" | grep -v "plain" | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "배포할 JAR 파일을 찾을 수 없습니다!" | tee -a $LOG_FILE
  # 디렉토리 내용 확인
  echo "디렉토리 내용:" | tee -a $LOG_FILE
  find ~/deploy -type f -name "*.jar" | tee -a $LOG_FILE
  exit 1
fi

echo "배포할 JAR 파일: $JAR_FILE" | tee -a $LOG_FILE

# 실행 환경 설정
JAVA_OPTS="-Xms512m -Xmx1024m"
SPRING_OPTS="-Dspring.profiles.active=prod"

# JAR 파일 실행
echo "애플리케이션 시작 중..." | tee -a $LOG_FILE
nohup java $JAVA_OPTS $SPRING_OPTS -jar $JAR_FILE > $LOG_DIR/app_$TIMESTAMP.log 2>&1 &

# 프로세스 ID 저장
PID=$!
echo "애플리케이션이 PID=$PID로 시작되었습니다." | tee -a $LOG_FILE

# 애플리케이션이 성공적으로 시작되었는지 확인
sleep 10
if ps -p $PID > /dev/null; then
  echo "애플리케이션이 성공적으로 실행 중입니다." | tee -a $LOG_FILE
  echo "로그 확인: tail -f $LOG_DIR/app_$TIMESTAMP.log" | tee -a $LOG_FILE
else
  echo "애플리케이션 시작에 실패했습니다! 로그를 확인하세요." | tee -a $LOG_FILE
  echo "마지막 로그 확인:" | tee -a $LOG_FILE
  tail -n 50 $LOG_DIR/app_$TIMESTAMP.log | tee -a $LOG_FILE
  exit 1
fi

echo "======== 배포 완료: $(date) ========" | tee -a $LOG_FILE 