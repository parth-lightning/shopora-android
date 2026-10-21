# Shopora for Android

A small Jetpack Compose shop used as the demo app for [QApybara](https://github.com/shahidhustles/apex-hackathon): browse products, fill a cart and check out.

Every push and pull request builds a debug APK in GitHub Actions and uploads it as the `shopora-apk` artifact. Label a pull request `qa` and QApybara installs that APK on a real phone, tests the change like a user and posts the verdict on the PR.

Build it yourself with `./gradlew assembleDebug`.
