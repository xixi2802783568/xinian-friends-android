param(
  [string]$JavaHome = $env:JAVA_HOME,
  [string]$SdkRoot = $env:ANDROID_HOME,
  [string]$BuildToolsDir,
  [string]$PlatformJar,
  [string]$SigningKey,
  [string]$SigningPasswordFile
)
$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
if (!$JavaHome) { throw 'Set JAVA_HOME to a JDK 17 installation.' }
if (!$SdkRoot) { $SdkRoot = $env:ANDROID_SDK_ROOT }
if (!$BuildToolsDir) { if (!$SdkRoot) { throw 'Set ANDROID_HOME to your Android SDK.' }; $BuildToolsDir = Join-Path $SdkRoot 'build-tools/35.0.0' }
if (!$PlatformJar) { $PlatformJar = Join-Path $SdkRoot 'platforms/android-35/android.jar' }
$jdk = $JavaHome
$bt = $BuildToolsDir
$env:JAVA_HOME = $jdk
$tools = Join-Path $project '.signing'
New-Item -ItemType Directory -Force $tools | Out-Null
$build = Join-Path $project 'build'
$release = Join-Path $project 'release'
$apkName = 'xinian-friends-0.2.3.apk'
New-Item -ItemType Directory -Force "$build/classes","$build/dex",$release | Out-Null
function Check-Exit { if ($LASTEXITCODE -ne 0) { throw "Build command failed: $LASTEXITCODE" } }
Push-Location $project
try {
  Copy-Item -LiteralPath $PlatformJar -Destination (Join-Path $build 'android.jar') -Force
  $platformRelative = 'build/android.jar'
  $manifestText = Get-Content (Join-Path $project 'app/src/main/AndroidManifest.xml') -Raw
  [IO.File]::WriteAllText((Join-Path $build 'AndroidManifest.xml'), $manifestText, [Text.UTF8Encoding]::new($false))
  & "$bt/aapt.exe" package -f -M build/AndroidManifest.xml -S app/src/main/res -A app/src/main/assets -I $platformRelative -F build/resources.apk
  Check-Exit
  $sources = @(Get-ChildItem app/src/main/java -Filter *.java -Recurse | ForEach-Object {$_.FullName})
  & "$jdk/bin/javac.exe" -encoding UTF-8 -source 8 -target 8 -classpath "$PlatformJar" -d "$build/classes" @sources
  Check-Exit
  & "$jdk/bin/jar.exe" cf "$build/classes.jar" -C "$build/classes" .
  Check-Exit
  & "$jdk/bin/java.exe" -cp "$bt/lib/d8.jar" com.android.tools.r8.D8 --lib "$PlatformJar" --min-api 29 --output "$build/dex" "$build/classes.jar"
  Check-Exit
  Copy-Item "$build/resources.apk" "$build/unsigned.apk" -Force
  & "$jdk/bin/jar.exe" uf "$build/unsigned.apk" -C "$build/dex" classes.dex
  Check-Exit
  & "$bt/zipalign.exe" -f -p 4 build/unsigned.apk build/aligned.apk
  Check-Exit
  $secret = if ($SigningPasswordFile) { $SigningPasswordFile } else { Join-Path $tools 'signing-password.txt' }
  $keystore = if ($SigningKey) { $SigningKey } else { Join-Path $tools 'development-signing.jks' }
  if ($SigningKey -and !(Test-Path $SigningKey)) { throw 'SigningKey does not exist.' }
  if ($SigningKey -and (!$SigningPasswordFile -or !(Test-Path $SigningPasswordFile))) { throw 'Provide SigningPasswordFile for your existing key.' }
  if (!(Test-Path $keystore)) {
    if (!(Test-Path $secret)) { [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32)) | Set-Content -Encoding ascii $secret }
    & "$jdk/bin/keytool.exe" -genkeypair -keystore $keystore -storepass:file $secret -keypass:file $secret -alias xinian -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=Xinian Android, O=Xinian, C=CN'
    Check-Exit
  }
  & "$jdk/bin/java.exe" -jar "$bt/lib/apksigner.jar" sign --ks $keystore --ks-key-alias xinian --ks-pass "file:$secret" --out "$release/$apkName" "$build/aligned.apk"
  Check-Exit
  & "$jdk/bin/java.exe" -jar "$bt/lib/apksigner.jar" verify --verbose "$release/$apkName"
  Check-Exit
  & "$bt/aapt.exe" dump badging "release/$apkName"
  Check-Exit
  Get-FileHash "$release/$apkName" -Algorithm SHA256 | Format-List
} finally { Pop-Location }
