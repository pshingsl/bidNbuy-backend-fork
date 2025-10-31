# 1. JDK 기반 이미지 선택
# eclipse-temurin: OpenJDK(자바 개발 환경)**를 배포하는 공식 이미지 중 하나
# 사용이유 -> 이미지 경량화
FROM openjdk:17

# 2. 컨테이너 내부 작업 디렉토리
WORKDIR /app

# 3. 빌드된 JAR 파일 복사
ARG JAR_FILE=build/libs/server-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app/app.jar

# 4. 컨테이너 포트 지정
EXPOSE 8080

# 5. 실행 명령어
ENTRYPOINT ["java","-jar","/app/app.jar"]