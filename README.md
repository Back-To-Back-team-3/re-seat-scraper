# re-seat-scraper

KBO 공식 홈페이지의 경기 일정을 수집해서 `re-seat` 프로젝트에서 사용할 CSV 파일로 내보내는 내부 도구입니다.

이 프로젝트는 서비스 API가 아니라 데이터 준비용 스크래퍼입니다. 실행하면 팀/구장 기준데이터를 DB에 준비하고, 설정한 연월 범위의 경기 일정을 수집한 뒤 `output/` 아래에 CSV 파일을 생성합니다.

## 사용 목적

`Back-To-Back-team-3/re-seat` 프로젝트에서 필요한 야구 경기 데이터를 준비하기 위해 만들었습니다.

생성되는 파일은 다음과 같습니다.

```text
output/stadiums.csv
output/teams.csv
output/games.csv
```

## 기술 스택

- Java 17
- Spring Boot 3.5
- Spring Data JPA
- MySQL
- Playwright
- Gradle

## 실행 전 준비

### 1. MySQL 준비

기본 설정은 `localhost:3306`의 MySQL을 사용합니다. Docker로 띄우거나, 로컬에 직접 설치한 MySQL을 사용할 수 있습니다.

#### Docker로 실행

```bash
docker run --name reseat-scraper-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=reseat_scraper \
  -p 3306:3306 \
  -d mysql:8.0
```

Windows PowerShell에서는 줄바꿈 문자가 다르므로 한 줄로 실행해도 됩니다.

```powershell
docker run --name reseat-scraper-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=reseat_scraper -p 3306:3306 -d mysql:8.0
```

#### 로컬 MySQL 직접 사용

로컬에 MySQL을 직접 설치해서 사용할 경우 `reseat_scraper` 데이터베이스를 만들고, [application.yaml](src/main/resources/application.yaml)의 접속 정보를 맞춥니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/reseat_scraper?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root
    password: root
```

Docker 예시를 그대로 사용하면 위 기본 설정과 맞습니다. 로컬 환경이 다르면 DB URL, 계정, 비밀번호를 본인 환경에 맞게 수정합니다.

### 2. 수집 범위 설정

[application.yaml](src/main/resources/application.yaml)의 `scraper.target`에서 시작 연월과 종료 연월을 선택합니다.

```yaml
scraper:
  collect:
    enabled: true
    month-from: 3
    month-to: 10

  target:
    start-season: 2026
    start-month: 7
    end-season: 2026
    end-month: 7
```

위 설정은 `2026년 7월`만 수집합니다.

```yaml
scraper:
  collect:
    month-from: 3
    month-to: 10

  target:
    start-season: 2025
    start-month: 3
    end-season: 2026
    end-month: 10
```

위 설정은 `2025년 3~10월`, `2026년 3~10월`을 수집합니다.

`month-from`, `month-to`는 실제 수집할 월 범위입니다. KBO 정규시즌 기준으로 기본값은 `3~10월`입니다.

## 실행 방법

Windows:

```powershell
.\gradlew.bat bootRun
```

macOS/Linux:

```bash
./gradlew bootRun
```

실행이 끝나면 `output/` 디렉터리에 CSV 파일이 생성됩니다.

## 기준데이터와 일정 수집 분리

팀/구장 기준데이터와 경기 일정 수집은 따로 켜고 끌 수 있습니다.

```yaml
scraper:
  seed:
    enabled: true
  collect:
    enabled: true
```

게임 일정만 다시 수집하고 싶으면 기준데이터 시드를 끕니다.

```yaml
scraper:
  seed:
    enabled: false
  collect:
    enabled: true
```

기준데이터만 확인하고 싶으면 일정 수집을 끕니다.

```yaml
scraper:
  seed:
    enabled: true
  collect:
    enabled: false
```

## CSV 설명

### stadiums.csv

구장 기준데이터입니다.

```text
id,name,address,total_capacity
```

### teams.csv

팀 기준데이터입니다.

```text
id,name,home_stadium_id
```

### games.csv

수집한 경기 일정입니다. CSV 출력 시 `scraper.target` 범위에 포함된 월만 출력합니다. DB에 예전 데이터가 남아 있어도 설정 범위 밖의 경기는 섞이지 않습니다.

```text
game_key,home_team_id,away_team_id,stadium_id,game_at,title
```

## 동작 흐름

1. `ReferenceDataSeeder`가 팀/구장 기준데이터를 준비합니다.
2. `DevCollectRunner`가 설정된 연월 범위를 월 단위로 계산합니다.
3. `ScheduleCollectService`가 KBO 일정 페이지에서 월별 경기 행을 수집합니다.
4. `KboScheduleParser`가 HTML 테이블 행을 `RawGame`으로 변환합니다.
5. 수집한 경기는 `game_key` 기준으로 새로 저장하거나 기존 행을 갱신합니다.
6. `CsvExporter`가 `stadiums.csv`, `teams.csv`, `games.csv`를 생성합니다.

## 테스트

```powershell
.\gradlew.bat test
```

macOS/Linux:

```bash
./gradlew test
```

## 주의사항

- 수집 데이터의 권리는 KBO에 있습니다.
- 이 도구는 팀 프로젝트 개발용 데이터 준비 도구입니다.
- 과도한 반복 실행으로 대상 사이트에 부하를 주지 않도록 주의합니다.
- KBO 홈페이지 HTML 구조가 바뀌면 파서나 selector 수정이 필요할 수 있습니다.
