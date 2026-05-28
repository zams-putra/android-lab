# Android Lab 

### Disclaimer
- sebagian disini aku pakai AI, soal nya aku ga terlalu jago ngoding android, dulu pernah tapi cuman beberapa kali dan udah lupa, makanya itu sekarang buat lagi sekalian belajar lagi
- This app is intentionally vulnerable for educational purposes
- Do not install on production devices
- buat berjaga jaga, semua aplikasi disini vuln soalnya


## Desc
jadi ini repository dari belajar DEV android and Pentestnya, aku buat beberapa Vuln APK disini buat belajar, bisa kalian pakai juga kedepannya
dah itu aja sih


# 1 - Rahasia Sidi (static analysis)

## Desc 
Lab pertama aja sih ini buat uji coba juga, ibarat kalau versi web tuh kalian baca source code html nya, yang ngarah ke js nya terus ada hardcode credential disitu dan logic login true nya disitu

## Tech Stack
- Language  : Kotlin
- Min SDK   : 21 (Android 5.0)
- Target SDK: 33 (Android 13)
- Build Tool: AGP 7.4.2
## Link
[link disini](https://github.com/zams-putra/android-lab/releases/download/lab-1/rahasiaSidi_VulnLab.apk)


# 1 - Dark Memories (static + dynamic analysis)

## Desc 
Lab kedua, disini udah mulai fetching fetching server nih, vuln nya cuman hardcode secret page aja sih sama intercept network di apk nya also with API KEY to get the flag

## Tech Stack
### Android
- Language   : Kotlin
- Min SDK    : 24 (Android 7.0)
- Target SDK : 36
- Build Tool : AGP 8.x
- Libraries  : Retrofit 2.9.0, Glide 4.15.1

### Backend
- Language   : Go

## Link
[Link Android](https://github.com/zams-putra/android-lab/releases/download/lab-2/DarkMemories.apk)
[Link zip Backend Server with img](https://github.com/zams-putra/android-lab/releases/download/lab-2/darkmemories-server-build.zip)