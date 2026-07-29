    import org.gradle.kotlin.dsl.implementation


    plugins {
        alias(libs.plugins.android.application)
        id("com.google.gms.google-services")
    }

android {
    namespace = "com.example.pixivo"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.pixivo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    implementation(libs.material)


    //noinspection UseTomlInstead
    //noinspection UseTomlInstead,GradleDependency
    implementation ("com.google.firebase:firebase-firestore:26.1.1")
    //noinspection UseTomlInstead
    //noinspection UseTomlInstead,NewerVersionAvailable
    implementation ("com.github.bumptech.glide:glide:5.0.5")
    //noinspection UseTomlInstead
    annotationProcessor ("com.github.bumptech.glide:compiler:5.0.5")

    implementation("com.hbb20:ccp:2.7.3")

    implementation("com.google.firebase:firebase-auth:22.3.0")

    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.github.MikeOrtiz:TouchImageView:3.6")
    implementation("com.github.yalantis:ucrop:2.2.8")


}