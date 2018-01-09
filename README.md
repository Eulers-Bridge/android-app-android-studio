# Isegoria for Android

This project is Gradle-based, and is primarily developed using [Android Studio](https://developer.android.com/studio/index.html).

A [Gitflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) branching model is followed in this repository, using the `master` branch for releases and the `dev` branch for development.

Bugs should be reported via Slack or Trello.

## Pre-Requisites
* [Android Studio](https://developer.android.com/studio/index.html)
* Gradle ([download standalone](http://www.gradle.org/downloads) or allow Android Studio to maintain)
* Android SDK (maintained by Android Studio)

## Android SDK
* Minimum SDK: API 16 (4.1 Jelly Bean)
* Target & Compile SDK: API 27 (8.1 Oreo) *[Should be latest API]*

## Credentials
* `google-services.json` supplied by Firebase, for notifications
* Amazon S3 credentials (for network photo uploads, `network/NetworkService.java`)
* Amazon AWS Access Key (for notifications, `notifications/FirebaseIDService.java`)
* Signing keystore file (to build for release)

## Instructions

* Clone the repository (`git clone ...`)
* Import the repository into Android Studio (File > Import Project, choose the root project directory)
* Build (Build > Make Project in Android Studio, to sync/fetch Gradle dependencies and compile the project)
* Run on connected device or emulator

## Building for Release
* Switch the current build variant to `release`
* Ensure path to signing keystore file is specified in `keystore.properties` in the root of the cloned project (create the file if it does not exist)
    - Example: `storeFile=/Example/Path/To/Keystore.jks`
* Build the project (Build > Build APK(s))
    - **NOTE:** Remember to keep a copy of both the built APK file and the `mapping.txt` file (to de-obfuscate stack traces received through the Google Play Developer Console)
