# 📱 Real-time Messenger App

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple?style=flat&logo=kotlin)
![Android](https://img.shields.io/badge/Android-SDK-green?style=flat&logo=android)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI-blue)
![Firebase](https://img.shields.io/badge/Backend-Firebase-orange?style=flat&logo=firebase)

A modern Android messaging application built with **Clean Architecture** and **Offline-first** principles. The app supports real-time chat, media sharing, and secure push notifications using the latest **FCM HTTP v1** protocol with OAuth2 authentication.

## 🚀 Key Features

* **Real-time Messaging:** Instant message delivery using Firebase Firestore.
* **Offline-First:** All chats are cached locally using **Room Database**. Users can view history without an internet connection.
* **Secure Push Notifications:** Implemented migration to **FCM HTTP v1 API** using Google OAuth2 (Service Account) for reliable delivery.
* **Media Sharing:** Ability to send and receive images (powered by Coil).
* **Message Management:** Local and remote message deletion.
* **Authentication:** Secure email/password login via Firebase Auth.

## 🛠 Tech Stack

* **Language:** Kotlin
* **Architecture:** Clean Architecture (Presentation, Domain, Data modules) + MVI/MVVM pattern.
* **Concurrency:** Coroutines & Flow (StateFlow, SharedFlow).
* **Dependency Injection:** Hilt.
* **Network:** Retrofit 2, OkHttp, Gson.
* **Local Storage:** Room Database (SQLite).
* **Remote Data:** Firebase Firestore, Firebase Storage.
* **UI:** Jetpack compose, Material Design 3.
* **Images:** Coil (Coroutines Image Loading).

## 🏗 Architecture Overview

The project follows the **Clean Architecture** principles to ensure separation of concerns and testability:

1.  **Presentation Layer (`:app`):** Contains UI (Activities, Fragments) and ViewModels. Handles user interactions and state rendering.
2.  **Domain Layer (`:domain`):** Pure Kotlin module. Contains UseCases, Repository Interfaces, and Business Models. Independent of Android SDK.
3.  **Data Layer (`:data`):** Handles data sources (API, Database, Firebase). Implements Repository interfaces and maps data to Domain models.

## 📸 App Screenshots

| Chat List | Search & User Info | Profile Settings | Push Notifications |
|:---------:|:------------------:|:----------------:|:------------------:|
| <img src="https://github.com/user-attachments/assets/00412c1f-231d-45aa-90b2-47138ca62d19" width="200"/> | <img src="https://github.com/user-attachments/assets/546d05f2-5bb6-4e18-b191-28093370b795" width="200"/> | <img src="https://github.com/user-attachments/assets/df59ea7a-5456-4ced-b2eb-95e8b7ad2bb4" width="200"/> | <img src="https://github.com/user-attachments/assets/12257342-cf9c-45ba-bb63-20c72250ffef" width="200"/> |

| Chat Details |
|:------------:|
| <img src="https://github.com/user-attachments/assets/22e912b2-0000-4bf0-8873-f06daf9faab8" width="200"/> | 


## 🔐 Security & Setup

This project uses sensitive configuration files (API Keys, Service Accounts) that are **excluded from version control** for security reasons.

To build and run this project, you need to:

1.  **Firebase Setup:**
    * Add your `google-services.json` to the `app/` directory.
2.  **FCM Configuration:**
    * This app uses a custom `FcmKeyStore` object to handle OAuth2 signing for notifications.
    * You must generate a **Service Account Key** in the Firebase Console and add it to `data/.../FcmKeyStore.kt`.

 ## 👨‍💻 Author
**Ramiz Galiakberov**
* Android Developer
* Astana, Kazakhstan

```kotlin
// Example of the protected FcmKeyStore object (Ignored by Git)
object FcmKeyStore {
    const val SERVICE_ACCOUNT_JSON = """
    {
      "type": "service_account",
      "project_id": "your-project-id",
      ...
    }
    """
}


