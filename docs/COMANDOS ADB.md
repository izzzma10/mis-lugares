COMANDOS DE ADB PARA PROBAR LA APP EN UN TELÉFONO REAL

*------1. Preparar el entorno------*

*---Definir dónde está el SDK de Android (hace falta en cada terminal nueva):---*

export ANDROID_HOME=~/Android/Sdk

*------2. Ver si el teléfono está conectado------*

*---Listar dispositivos conectados por USB (tiene que aparecer con la palabra "device" al lado, no "unauthorized" ni "offline"):---*

~/Android/Sdk/platform-tools/adb devices

Si no aparece nada: revisá el cable USB, y en el teléfono que la depuración USB esté activada y autorizada (suele salir un cartel para aceptar la conexión la primera vez).

*------3. Compilar la app------*

*---Genera el APK en app/build/outputs/apk/debug/app-debug.apk:---*

export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
./gradlew assembleDebug

*------4. Instalar y abrir en el teléfono------*

*---Instala el APK ya compilado (el -r reinstala encima si ya estaba, sin perder nada):---*

~/Android/Sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk

*---Abre la app directo en la pantalla del menú principal:---*

~/Android/Sdk/platform-tools/adb shell am start -n com.example.mislugares/.presentacion.MenuActivity

*------5. Ver qué está pasando dentro de la app (logs)------*

*---Ver los logs en vivo mientras usás la app (Ctrl+C para cortar):---*

~/Android/Sdk/platform-tools/adb logcat

*---Limpiar los logs viejos antes de repetir una prueba (para no confundirte con logs de antes):---*

~/Android/Sdk/platform-tools/adb logcat -c

*---Filtrar logs por una palabra clave (por ejemplo, errores de un componente puntual):---*

~/Android/Sdk/platform-tools/adb logcat -d | grep -i "palabra_clave"

*------6. Todo junto, en un solo bloque (compilar + instalar + abrir)------*

export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
export ANDROID_HOME=~/Android/Sdk
./gradlew assembleDebug
~/Android/Sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
~/Android/Sdk/platform-tools/adb shell am start -n com.example.mislugares/.presentacion.MenuActivity
