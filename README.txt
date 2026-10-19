CALCULATOR (vault) - build instructions

OPTION A - Android Studio (PC)
1. Install Android Studio, File > Open > select this Calculator folder.
2. Let Gradle sync, then Build > Build APK(s).
3. APK: app/build/outputs/apk/debug/app-debug.apk -> copy to phone and install.

OPTION B - no PC, GitHub builds it for you
1. Create a free GitHub repo, upload ALL these files (keep the .github folder).
2. Actions tab > "Build APK" > Run workflow (wait ~3-5 min).
3. Open the finished run > Artifacts > Calculator-apk > download, unzip, install.

USE
- Opens as a normal calculator named "Calculator".
- Type the secret code 1234 then press = -> fingerprint prompt -> vault.
- In vault: "Add / remove apps" to pick apps, "Done", tap an app to launch it.
- "Change code" to set your own secret code.
- Leaving the vault locks it again.
