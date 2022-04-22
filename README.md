# 기능 정의
## 데이터 공급자(DataProvider)
### 기능
- 공급자의 상태를 확인한다.
  - 정상, 비정상
- 데이터 공급 방식을 가져온다.
## TheMovieDB
### 정의
- 영화, TV프로그램 관련 정보 API 공급자
- 매일 일 배치로 영화, TV 프로그램, 레이팅, 키워드 등을 json.gz 형식으로 가져올 수 있다.
### 분석 내용
- tmdb에서 정의한 ID를 기반으로 상세 영화 정보, 영화 포스터, 공급자 목록을 가져올 수 있다.
- 대부분의 API에서 `language` 설정을 하여 한국어로 가져올 수 있다.
# 일별 배치 작업
## 첫번째 처리 프로세스
- tmdb를 베이스로 일 데이터를 가져오도록 한다.
- 매일 오후 5시 이후(08:00 AM UTC)에 일 데이터를 쌓는 작업을 한다.
  - 영화 ID 목록 예시: `http://files.tmdb.org/p/exports/movie_ids_MM_DD_YYYY.json.gz`
- 일 데이터를 쌓을 때, 중복된 ID가 있을 경우 갱신작업을 하지 않는다.
- 임시 테이블을 만들어, 해당 일에 새로 추가된 데이터 정보를 입력해둔다.
## 영화 상세 정보 처리 프로세스
- 배치를 두가지 유형으로 구분 짓는다.
  - ID 목록 일 데이터 임시 테이블이 존재할 경우, 일괄 작업
    - 임시 테이블을 읽어 상세 정보를 적재
    - 적재가 다 된 경우, 임시테이블에서 해당 로우의 데이터 삽입 여부를 `true`로 만든다.
    - 배치가 다 끝나면 임시테이블의 데이터 삽입 여부를 카운트 하여 다 적제가 되면 임시 테이블 삭제, 
      작업이 실패한 게 있을 경우 재 적제 요청을 한다.
  - 파라미터로 tmdb ID 값을 제공하여 해당 데이터만 갱신시키는 프로세스

## 외부 API 호출 클래스 명세
### interface
ExternalApiCaller
- call(APIClient apiClient);
AbstractExternalApiCaller
- field
  - name
  - version
  - subdomain
  - host
  - uri
  - parameter<K, V>
- method
  - call (override, abstract)
  - URI builder (implementation)

APIClient
- WebClient 또는 HttpURLConnection 같은 객체들을 선택하여 request, response를 받을 수 있도록 표준을 명세한다.
