cd /d %~dp0
dir bin\XposedBridge.apk
adb push bin\XposedBridge.apk /data/xposed/XposedBridge.jar.newversion
pause