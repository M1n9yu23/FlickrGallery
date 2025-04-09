![gallery_main](https://github.com/user-attachments/assets/967dbf31-c0cc-431a-ac97-3ac370838c12)

# 💡 프로젝트 설명

- Flickr API를 활용하여 플리커 홈페이지의 최근 사진을 보여주고, 검색 기능을 활용하여 이미지 검색을 하거나, 플리커의 기본 태그를 활용하여 태그별 탐색을 통해 이미지를 확인 할 수 있다.
- 이미지를 공유할 수 있고, 사진이 업데이트 될 때마다 알림을 받을 수 있다. 포그라운드 상태일 때는 알림을 받지 않는다.
- 이미지를 선택하여 해당 이미지의 상세 설명을 WebView를 통해 플리커 홈페이지로 이동하게 된다.


# ⭐️ 주요 기능

### 1️⃣ **Flickr API 연동**

- Retrofit을 활용하여 Flickr API에서 사진을 가져옴.
- 사용자가 검색창을 활용하여 입력하면 관련된 이미지를 검색
- 플리커 홈페이지의 기본 태그를 활용하여 관련된 이미지를 표시

### 2️⃣ 이미지 로딩

- 이미지가 로딩 될 때 시각적 ProgressBar를 보여줌
- Glide 라이브러리를 사용하여 **이미지 로드 최적화** .

### 3️⃣ **검색 기능**

- `SearchView`를 활용하여 **검색어 입력 및 결과 필터링**.
- `SharedPreferences`를 이용해 **최근 검색어를 저장하고 유지**.

### 4️⃣ 플리커 **백그라운드 폴링(Polling) (주기적 사진 업데이트)**

- 일정 시간이 지나면 새로운 사진을 가져오는 백그라운드 작업을 추가.
- WorkManager를 활용하여 앱이 실행되지 않은 상태에서도 주기적으로 업데이트할 수 있도록 설정.
- 사용자가 백그라운드 업데이트 기능을 ON/OFF할 수 있도록 설정.

### 5️⃣ **알림(Notification)**

- 새로운 사진이 추가되면 **Notification을 통해 사용자에게 알림**.

### 6️⃣ 공유

- 플리커의 이미지를 공유 가능.

### 7️⃣ 플리커 홈페이지

- 사진의 상세 페이지 즉 플리커 홈페이지로 이동 가능, 암시적 인텐트를 사용하여 구현 가능했지만, WebView를 통해 해당 앱에서 홈페이지를 볼 수 있도록 구현.


# 🛠 사용 기술 & 스택

| 기술 | 설명 |
| --- | --- |
| 언어 | Kotlin |
| UI | XML |
| UI와 데이터 연결 | LiveData & ViewModel |
| 네트워크 | Retrofit2 + OkHttp3 + GsonConverterFactory |
| 이미지 로딩 | Glide |
| 백그라운드 작업 (폴링) | WorkManager |
| 알림 | NotificationManagerCompat |
| 공유프리퍼런스 | SharedPreferences (암호나 민감한 정보를 저장하는 것이 아닌 검색어와 폴링 여부를 저장하기 때문에 파일 시스템에 저장) |
| Interceptor | 플리커API 메서드에 URL에 사용되는 매개변수와 값이 반복되는 것이 있어서 Interceptor를 사용 |
| 브로드캐스트 인텐트와 수신자  | 백그라운드 작업과 UI가 직접 연결되지 않아도 정보를 주고 받을 수 있게 하기 위해 사용. 포그라운드에서 실행 중인지를 결정하기 위해 사용. |


# ‼️ 배운점

- Retrofit과 OkHttp를 활용하여 네트워크 요청을 어떻게 하는지 알게 됨.
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
