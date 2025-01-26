plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin)
}

android {
  namespace = "com.github.droibit.oss_licenses.ui.compose.navigation"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = 24

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  packaging {
    resources {
      excludes += listOf(
        "/META-INF/{AL2.0,LGPL2.1}",
        "/META-INF/core_debug.kotlin_module"
      )
    }
  }
}

dependencies {
  api(libs.androidx.navigation.runtime)

  implementation(libs.androidx.annotation)
}

apply(from = "$rootDir/gradle/gradle-mvn-push.gradle.kts")
