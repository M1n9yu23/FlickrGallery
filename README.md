![gallery_main](https://github.com/user-attachments/assets/967dbf31-c0cc-431a-ac97-3ac370838c12)

# 💡 프로젝트 설명

Flickr API를 활용하여 최근 사진을 탐색하고, 키워드 검색과 태그 기반 탐색이 가능한 이미지 뷰어 앱입니다. 
백그라운드에서 사진을 주기적으로 업데이트하고, 새로운 이미지가 등록되면 알림을 통해 사용자에게 전달합니다. 
WebView를 통해 사진 상세 페이지로 이동할 수 있으며, 공유 기능도 지원합니다.

# 📘 프로젝트 개요

이 프로젝트는 가장 인상 깊게 읽은 책인 *Android Programming: The Big Nerd Ranch Guide (4th Edition)*를 기반으로 시작하였습니다.

책의 내용을 참고하되, **지원이 중단된 API를 최신 버전으로 교체하고**, **Glide를 활용한 이미지 최적화**, **UI/UX 개선**, 그리고 **여러 기능을 확장** 하였습니다.

# ⭐️ 주요 기능

- **Flickr API 연동**: Retrofit으로 사진 목록,  검색, 태그 기반 탐색 기능 구현
- **이미지 로딩 최적화**: Glide와 ProgressBar로 부드러운 이미지 렌더링 제공
- **검색 기록 저장**: `SearchView` + `SharedPreferences`로 최근 검색어 유지
- **백그라운드 폴링**: WorkManager로 주기적 사진 업데이트, 사용자가 ON/OFF 설정 가능
- **알림(Notification)**: 새로운 사진이 감지되면 알림 전송 (포그라운드일 땐 제외)
- **이미지 공유**: 인텐트로 외부 앱에 이미지 정보 전달
- **WebView 연결**: 사진 클릭 시 플리커 상세 페이지를 앱 내 WebView로 표시

# 🛠 사용 기술 & 스택

| 기술 | 설명                                                                             |
| --- |--------------------------------------------------------------------------------|
| 언어 | Kotlin                                                                         |
| UI | XML                                                                            |
| UI와 데이터 연결 | LiveData & ViewModel                                                           |
| 네트워크 | Retrofit2, GsonConverterFactory                                                |
| 이미지 로딩 | Glide                                                                          |
| 백그라운드 작업 (폴링) | WorkManager                                                                    |
| 알림 | NotificationManagerCompat                                                      |
| 공유프리퍼런스 | SharedPreferences (암호나 민감한 정보를 저장하는 것이 아닌 검색어와 폴링 여부를 저장하기 때문에 파일 시스템에 저장)     |
| Interceptor | 플리커API 메서드에 URL에 사용되는 매개변수와 값이 반복되는 것이 있어서 Interceptor를 사용                     |
| 브로드캐스트 인텐트와 수신자  | 백그라운드 작업과 UI가 직접 연결되지 않아도 정보를 주고 받을 수 있게 하기 위해 사용. 포그라운드에서 실행 중인지를 결정하기 위해 사용. |


# ‼️ 배운점

- Retrofit을 활용하여 네트워크 처리 학습
- LiveData와 ViewModel를 활용하여 UI와 데이터 간의 업데이트를 효율적으로 할 수 있었음.
- Glide를 통해 효율적인 이미지 로딩.
- 백그라운드 작업을 WorkManager 아키텍처 컴포넌트 라이브러리를 사용해서 처리 할 수 있다는 것을 알게 됨.
- 암시적 인텐트 대신에 WebView를 사용함으로써 해당 앱에서 홈페이지가 실행될 수 있다는 것을 알게 됨
- 공유 프리퍼런스를 사용해서 장치의 파일 시스템에 데이터를 쉽게 저장하는 것을 알게 됨.
- 공유 되는 매개변수와 값의 쌍은 인터셉터를 사용해서 처리하면 요청이나 응답의 정보를 가로채서 원하는 처리를 할 수 있다는 것을 알게 됨.

# 📷 Screenshot

<p float="left">
<img width="40%" src="https://github.com/user-attachments/assets/21e8c2f2-0ccf-4e77-9db4-c96ae6a9f118">

<img width="40%" src="https://github.com/user-attachments/assets/7ff7b7c5-44b8-4650-a0ec-8edba9102d22">
</p>


<p float="left">
<img width="40%" src="https://github.com/user-attachments/assets/5358e8be-129b-4bfe-9fb5-2a4818b5f115">

<img width="40%" src="https://github.com/user-attachments/assets/f7cc5842-0770-493f-bdee-889f6b4fcfff">
</p>


<p float="left">
<img width="40%" src="https://github.com/user-attachments/assets/a016f377-2368-4a63-91ac-29987c95216d">

<img width="40%" src="https://github.com/user-attachments/assets/963c3494-7f38-4f16-a335-41cb77f37999">
</p>
