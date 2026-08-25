COMANDOS DE ADB PARA PROBAR LA APP EN UN TELÉFONO REAL

(Los pasos 1 a 6 son para la máquina Fedora/Linux. Si estás en la de Windows 10, saltá al paso 7: son los mismos comandos traducidos a PowerShell.)

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

*------7. Lo mismo, pero en la máquina de Windows 10------*

Los pasos 1 a 6 de arriba son para Fedora/Linux. Acá van los equivalentes en PowerShell.

Dos diferencias importantes antes de empezar:
- JAVA_HOME ya está configurada a nivel de sistema (Temurin 21), así que NO hace falta definirla en cada terminal.
- El Android SDK todavía NO está instalado en esta máquina (no hay adb ni Android Studio). Hasta que se instale, ni la compilación ni estos comandos funcionan acá.

*---Definir dónde está el SDK (hace falta en cada terminal nueva de PowerShell):---*

$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"

*---Ver si el teléfono está conectado (tiene que decir "device" al lado, no "unauthorized" ni "offline"):---*

& "$env:ANDROID_HOME\platform-tools\adb.exe" devices

*---Compilar la app (genera app\build\outputs\apk\debug\app-debug.apk):---*

.\gradlew.bat assembleDebug

*---Instalar en el teléfono (-r reinstala encima si ya estaba, sin perder datos):---*

& "$env:ANDROID_HOME\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk

*---Abrir la app en la pantalla del menú principal:---*

& "$env:ANDROID_HOME\platform-tools\adb.exe" shell am start -n com.example.mislugares/.presentacion.MenuActivity

*---Ver los logs en vivo (Ctrl+C para cortar):---*

& "$env:ANDROID_HOME\platform-tools\adb.exe" logcat

*---Limpiar los logs viejos antes de repetir una prueba:---*

& "$env:ANDROID_HOME\platform-tools\adb.exe" logcat -c

*---Filtrar logs por una palabra clave (Select-String es el "grep" de PowerShell):---*

& "$env:ANDROID_HOME\platform-tools\adb.exe" logcat -d | Select-String -Pattern "palabra_clave"

*---Todo junto (compilar + instalar + abrir):---*

$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat assembleDebug
& "$env:ANDROID_HOME\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
& "$env:ANDROID_HOME\platform-tools\adb.exe" shell am start -n com.example.mislugares/.presentacion.MenuActivity

*------8. Cosas para tener en cuenta en Windows------*

- En PowerShell 5.1 NO existe el && para encadenar comandos (tira error de sintaxis). Se encadena con ; o, si el segundo comando solo debe correr cuando el primero salió bien, con: comando1; if ($?) { comando2 }
- El & del principio de las líneas de adb no es un adorno: PowerShell lo necesita para ejecutar un programa cuya ruta va entre comillas.
- Si preferís Git Bash en vez de PowerShell, sirven casi todos los comandos de los pasos 1 a 6, con dos cambios: saltear el export JAVA_HOME (ya está definida) y usar $LOCALAPPDATA/Android/Sdk en vez de ~/Android/Sdk.
- Solo MenuActivity se puede abrir con "am start" desde afuera: es la única con android:exported="true" en el AndroidManifest.xml (es la de arranque). Si intentás lanzar VistaLugarActivity o MainActivity directo, adb responde con un error de permisos. Para llegar a ellas hay que abrir la app y navegar desde el menú.
