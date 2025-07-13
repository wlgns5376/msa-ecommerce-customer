# Custom GitHub Actions

이 디렉토리는 재사용 가능한 커스텀 GitHub Actions를 포함합니다.

## Actions

### 1. parse-test-results
JUnit 테스트 결과를 파싱하고 요약을 생성합니다.

**입력:**
- `test-results-path`: 테스트 결과 파일 패스 (기본값: `**/build/test-results/test/TEST-*.xml`)
- `fail-on-error`: 테스트 실패 시 액션 실패 여부 (기본값: `true`)

**출력:**
- `total-tests`: 전체 테스트 수
- `passed-tests`: 통과한 테스트 수
- `failed-tests`: 실패한 테스트 수
- `error-tests`: 에러가 발생한 테스트 수

### 2. parse-coverage
JaCoCo 커버리지 리포트를 파싱하고 요약을 생성합니다.

**입력:**
- `coverage-path`: 커버리지 리포트 파일 패스 (기본값: `**/build/reports/jacoco/test/jacocoTestReport.xml`)
- `min-coverage`: 최소 커버리지 비율 (기본값: `80`)

**출력:**
- `overall-coverage`: 전체 커버리지 비율
- `coverage-passed`: 커버리지 임계값 통과 여부

**참고:** JaCoCo XML의 마지막 INSTRUCTION counter에서 전체 프로젝트 커버리지를 추출합니다.

## 사용 예시

```yaml
- name: Parse test results
  uses: ./.github/actions/parse-test-results
  with:
    test-results-path: '**/build/test-results/test/TEST-*.xml'
    fail-on-error: 'true'

- name: Parse coverage
  uses: ./.github/actions/parse-coverage
  with:
    coverage-path: '**/build/reports/jacoco/test/jacocoTestReport.xml'
    min-coverage: '80'

- name: Analyze PR
  uses: ./.github/actions/analyze-pr
  with:
    pr-number: ${{ github.event.number }}
    pr-title: ${{ github.event.pull_request.title }}
    # ... 기타 필수 입력들
```