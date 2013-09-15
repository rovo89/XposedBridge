cd /d %~dp0
dir bin\XposedBridge.apk
adb push bin\XposedBridge.apk /data/data/de.robv.android.xposed.installer/bin/XposedBridge.jar.newversion
pause
