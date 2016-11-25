apply plugin: 'com.android.application'


android {
compileSdkVersion 25
buildToolsVersion "25.0.0"

defaultConfig {
applicationId "uiwear.${app.appPkgName}"
minSdkVersion 21
targetSdkVersion 21
versionCode 1
versionName "1.0"
}
}

dependencies {
compile project(':uiwearlib')
}
