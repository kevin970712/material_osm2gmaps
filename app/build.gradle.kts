import com.android.build.api.variant.impl.VariantOutputImpl
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

// ─────────────────────────────────────────────────────────────────────────────
// 1) Load key.properties (which your Jenkinsfile drops into app/key.properties)
// ─────────────────────────────────────────────────────────────────────────────
val keystorePropsFile = file("key.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "net.retiolus.osm2gmaps"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.retiolus.osm2gmaps"
        minSdk        = 23
        targetSdk     = 35
        versionCode   = 37
        versionName   = "0.5.21"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2) Hook up your signingConfig.release using the loaded keystoreProps
    // ─────────────────────────────────────────────────────────────────────────
    signingConfigs {
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile     = file(keystoreProps.getProperty("storeFile")!!)
                storePassword = keystoreProps.getProperty("storePassword")!!
                keyAlias      = keystoreProps.getProperty("keyAlias")!!
                keyPassword   = keystoreProps.getProperty("keyPassword")!!
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // ← THIS makes Gradle actually sign the release APK
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
    dependenciesInfo {
        includeInApk    = false
        includeInBundle = false
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3) Rename the output APK to net.retiolus.osm2gmaps-v<version>.apk
// ─────────────────────────────────────────────────────────────────────────────
androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach { output ->
            if (output is VariantOutputImpl) {
                val verName = output.versionName.get()
                output.outputFileName = "${variant.applicationId.get()}-v$verName.apk"
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.openlocationcode:openlocationcode:1.0.4")
    implementation("com.what3words:w3w-android-wrapper:4.0.0")
    implementation("com.google.android.material:material:1.7.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
