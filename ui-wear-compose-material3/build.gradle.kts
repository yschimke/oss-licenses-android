@file:Suppress("UnstableApiUsage")

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.roborazzi)
}

android {
  namespace = "com.github.droibit.oss_licenses.ui.wear.compose.material3"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = 25

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    compose = true
  }

  packaging {
    resources {
      excludes += listOf(
        "/META-INF/{AL2.0,LGPL2.1}",
        "/META-INF/core_debug.kotlin_module",
      )
    }
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }
}

dependencies {
  implementation(projects.uiViewmodel)
  implementation(projects.uiWearComposeCore)

  api(libs.androidx.activity)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  api(libs.bundles.androidx.wear.compose.material3)
  implementation(libs.bundles.androidx.wear.compose.navigation) {
    exclude(group = "androidx.wear.compose", module = "compose-material")
  }
  implementation(libs.bundles.androidx.lifecycle.viewModel.compose)

  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.tooling.preview)
  debugImplementation(libs.androidx.wear.compose.ui.tooling)
  debugImplementation(libs.androidx.wear.tooling.preview)
  debugImplementation(libs.androidx.ui.test.manifest)

  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
  testImplementation(projects.uiComposeScreenshots)
}

apply(from = "$rootDir/gradle/gradle-mvn-push.gradle.kts")
