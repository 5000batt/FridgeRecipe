# 🍳 FridgeRecipe (냉장고 파먹기)

> 사용자의 냉장고 속 재료를 기반으로 맞춤형 레시피를 추천해 주는 안드로이드 애플리케이션입니다.

📱 **현재 구글 플레이 스토어 비공개 테스트 진행 중입니다.**

<br>

## 📌 주요 화면 및 기능 (Preview)
| 메인 화면(AI 추천) | 재료 화면 | 레시피 화면 |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/5a6d4764-41e6-4576-adca-65635431a144" width="200"/> | <img src="https://github.com/user-attachments/assets/46998d2f-917b-4dd5-ba39-a37a63384b1d" width="200"/> | <img src="https://github.com/user-attachments/assets/9e8e96d9-e01b-42a0-947f-29cf8c2002cb" width="200"/> |

- **AI 레시피 추천:** 선택한 재료를 활용해 만들 수 있는 최적의 레시피 제안
- **재료 관리:** 현재 보유 중인 재료의 유통기한 및 보관 상태 관리
- **레시피 관리:** AI로 생성한 레시피 또는 사용자가 만든 레시피 관리

<br>

## 🛠 Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** Clean Architecture, MVVM Pattern
- **Asynchronous:** Coroutines, Flow
- **Local Database:** Room
- **Dependency Injection:** Hilt

<br>

## 🏗 Architecture
이 프로젝트는 유지보수성과 확장성을 고려하여 **Clean Architecture** 기반으로 설계되었습니다.
- **Presentation Layer:** UI(Compose)와 상태 관리(ViewModel) 담당
- **Domain Layer:** 비즈니스 로직 및 UseCase, Repository Interface 포함
- **Data Layer:** Remote/Local 데이터 소스 통신 및 Repository 구현

<br>

## 🔥 Trouble Shooting & Learnings
> 개발 과정에서 마주친 문제들과 해결 과정을 기록했습니다.

#### 1. 서버리스(Serverless) 환경의 배포 병목 현상 극복: Firebase Remote Config 도입
- **Issue (문제 배경)**:
  본 프로젝트는 별도의 백엔드 서버 없이 클라이언트에서 직접 외부 API(Gemini LLM)와 통신하는 서버리스(Serverless) 아키텍처를 띄고 있습니다.
  개발 과정에서 AI 모델의 버전을 업데이트해야하거나, 프롬프트 파라미터 등 사소한 설정값을 변경해야 할 일이 잦았습니다. 하지만 안드로이드 생태계 특성상, 모델명 하나를 변경하기 위해서도 **코드를 수정하고 👉 빌드하고 👉 스토어 심사 👉 사용자의 앱 업데이트**라는 길고 비효율적인 사이클을 거쳐야만 했습니다. 만약 기존 모델이 갑자기 서비스 종료라도 된다면, 심사 기간 동안 앱 서비스가 마비될 수 있는 치명적인 리스크였습니다.

- **Solution (해결 과정)**:
  이러한 **모바일 앱의 정적인 배포 한계를 극복**하기 위해 **Firebase Remote Config**를 도입했습니다.
  AI 모델명(예: `gemini-2.5-flash`), 프롬프트 템플릿, 그리고 동의어 사전 등의 변동 가능성이 높은 데이터들을 앱 내부에 하드코딩(Hard-coding)하지 않고 Remote Config 서버에서 동적으로 주입받도록 아키텍처를 개선했습니다.

- **Learnings (배운 점)**:
  이를 통해 스토어 재심사나 사용자의 앱 업데이트 없이도 **실시간으로 AI 모델을 교체하거나 서비스를 제어**할 수 있는 유연성을 확보했습니다. 단순히 기능을 구현하는 것을 넘어, 모바일 앱의 라이프사이클을 이해하고 운영 및 유지보수성(Maintainability)을 고려한 아키텍처 설계가 얼마나 중요한지 깨닫는 계기가 되었습니다.

#### 2. 관심사의 분리(Separation of Concerns)와 아키텍처의 필요성 체감
- **Issue (문제 배경)**:
  프로젝트 초기에는 ViewModel 내부에 UI 상태를 업데이트하는 코드뿐만 아니라, 데이터를 필터링하고 가공하는 핵심 로직까지 혼재되어 있었습니다. 시간이 지날수록 ViewModel의 코드가 비대해져서 가독성이 떨어지고, 특정 기능을 수정할 때 어디를 건드려야 할지 파악하기 어려워지는 한계를 느꼈습니다.

- **Solution (해결 과정)**:
  이러한 문제를 해결하기 위해 Clean Architecture의 계층 분리 개념을 프로젝트에 도입했습니다.
  완벽한 형태는 아니더라도, 데이터 출처(Local/Remote)를 결정하는 역할은 `Repository`로, 앱의 핵심 비즈니스 로직은 `UseCase`로 추출하여 위임했습니다. 결과적으로 `ViewModel`은 오직 'UI에 데이터를 전달하고 이벤트를 처리하는 역할'에만 집중하도록 책임과 역할을 나누도록 작업을 진행했습니다.

- **Learnings (배운 점)**:
  어떤 코드가 순수한 비즈니스 로직(UseCase)인지, UI 로직(ViewModel)인지 그 완벽한 경계선에 대해서는 여전히 끊임없이 학습하고 고민하는 단계입니다. 하지만 코드를 각자의 역할에 맞게 분리함으로써 얻게 되는 **'코드 가독성 향상'과 '유지보수의 편리함'**을 온몸으로 체감할 수 있었습니다.
  이러한 경험을 바탕으로, 향후 규모가 큰 실무 환경에 투입되더라도 팀의 아키텍처 컨벤션을 빠르게 이해하고 적응할 수 있는 시야와 기본기를 다졌습니다.

#### 3. 지속적 통합(CI) 파이프라인 구축: GitHub Actions, Ktlint, Lint, Slack 자동화
- **Issue (문제 배경)**:
  1인 개발 환경이지만 코드의 품질을 일정하게 유지하고, 병합(Merge) 전 기존 기능을 망가뜨리지 않았는지 검증하는 과정이 필요했습니다. 매번 로컬에서 수동으로 린트(Lint)를 돌리고 앱을 빌드하는 것은 번거로웠으며, 실수로 검증되지 않은 코드를 `main` 브랜치에 덮어씌우는 휴먼 에러가 발생할 위험이 있었습니다.

- **Solution (해결 과정)**:
  이러한 불안감을 없애고 안정적인 배포 환경을 마련하기 위해 **GitHub Actions**를 활용한 CI 파이프라인을 구축했습니다.
  1. **1인 개발 브랜치 전략에 맞춘 워크플로우 최적화**: 모든 Push마다 CI를 돌리는 대신, `develop`에서 `main` 브랜치로 PR을 올릴 때만 워크플로우가 실행되도록 1인 개발 사이클에 맞게 최적화했습니다. 또한 `.gitignore`에 등록된 `local.properties`, `google-services.json` 등의 보안 파일은 GitHub Secrets와 `env` 환경 변수를 활용해 CI 가상 머신 내부에서 안전하게 주입되도록 처리했습니다.
  2. **Ktlint 도입 및 Compose 예외 처리**: Version Catalog(`libs.versions.toml`)를 통해 Ktlint(14.0.1)를 연동했습니다. 검사 과정에서 Jetpack Compose의 `@Composable` 함수(PascalCase)와 코틀린 표준 함수 규칙(camelCase)이 충돌하는 문제는 프로젝트 최상단에 `.editorconfig`를 생성해 예외 처리(`ktlint_function_naming_ignore_when_annotated_with=Composable`)하여 해결했습니다.
  3. **무결점 코드 유지를 위한 브랜치 보호 (Branch Protection)**: GitHub의 'Branch protection rules'를 적용하여 다이렉트 Push를 원천 차단했습니다. PR 생성을 강제하고, CI 빌드 검사(Status Check)를 통과하지 못하면 `main` 브랜치로의 Merge 버튼이 비활성화되도록 시스템적인 안전망을 구축했습니다.
  4. **Slack 알림 연동을 통한 모니터링 자동화**: GitHub Actions의 결과(성공/실패 여부, 소요 시간, 커밋 정보 등)를 Webhook을 통해 개인 Slack 채널로 즉시 전송되도록 연동하여, 매번 웹페이지를 확인하지 않아도 되는 편리한 모니터링 환경을 구성했습니다.

- **Learnings (배운 점)**:
  단순히 기능 구현에만 집중하는 것을 넘어, 파이프라인을 구축해 봄으로써 코드의 무결성을 시스템적으로 보장받는 든든함을 체감했습니다. GitHub Actions의 쉘 스크립트 작성법부터 보안 데이터의 안전한 격리, 그리고 각종 컨벤션 에러를 추적하고 예외 처리하는 과정 전반을 깊이 있게 이해하는 계기가 되었습니다.

## 👨‍💻 Developer
- **곽정원** - Android Developer
- Email: jw208274@gmail.com
