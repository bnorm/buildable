plugins {
  kotlin("multiplatform")
}

kotlin {
  jvm()
  js()

  sourceSets {
    commonMain {
      dependencies {
        implementation(project(":runtime"))
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
      }
    }
  }
}

dependencies {
  kotlinCompilerPluginClasspath(project(":compiler-plugin"))
}
