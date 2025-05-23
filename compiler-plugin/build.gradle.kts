plugins {
  kotlin("jvm")
  `java-test-fixtures`
}

kotlin {
  sourceSets.all {
    languageSettings {
      enableLanguageFeature("ContextParameters")
      optIn("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
      optIn("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
    }
  }
}

sourceSets {
  main {
    java.setSrcDirs(listOf("src"))
    resources.setSrcDirs(listOf("resources"))
  }
  testFixtures {
    java.setSrcDirs(listOf("test-fixtures"))
  }
  test {
    java.setSrcDirs(listOf("test", "test-gen"))
    resources.setSrcDirs(listOf("testResources"))
  }
}

val buildableRuntimeClasspath: Configuration by configurations.creating { isTransitive = false }

dependencies {
  compileOnly(kotlin("compiler"))

  testFixturesApi(kotlin("test-junit5"))
  testFixturesApi(kotlin("compiler-internal-test-framework"))
  testFixturesApi(kotlin("compiler"))

  buildableRuntimeClasspath(project(":runtime"))

  // Dependencies required to run the internal test framework.
  testRuntimeOnly(kotlin("reflect"))
  testRuntimeOnly(kotlin("test"))
  testRuntimeOnly(kotlin("script-runtime"))
  testRuntimeOnly(kotlin("annotations-jvm"))
}

tasks.withType<Test> {
  dependsOn(buildableRuntimeClasspath)
  inputs.dir(layout.projectDirectory.dir("testData"))
    .withPropertyName("testData")
    .withPathSensitivity(PathSensitivity.RELATIVE)

  workingDir = rootDir

  useJUnitPlatform()

  systemProperty("buildableRuntime.classpath", buildableRuntimeClasspath.asPath)

  // Properties required to run the internal test framework.
  systemProperty("idea.ignore.disabled.plugins", "true")
  systemProperty("idea.home.path", project.rootDir)
  setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
  setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
  setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
  setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
  setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
  setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")
}

val generateTests by tasks.registering(JavaExec::class) {
  classpath = sourceSets.testFixtures.get().runtimeClasspath
  mainClass.set("dev.bnorm.buildable.plugin.GenerateTestsKt")
  workingDir = rootDir

  inputs.dir(layout.projectDirectory.dir("testData"))
    .withPropertyName("testData")
    .withPathSensitivity(PathSensitivity.RELATIVE)
  outputs.dir(layout.projectDirectory.dir("test-gen"))
    .withPropertyName("generatedTests")
}

tasks.compileTestKotlin {
  dependsOn(generateTests)
}

fun Test.setLibraryProperty(propName: String, jarName: String) {
  val path = project.configurations
    .testRuntimeClasspath.get()
    .files
    .find { """$jarName-\d.*jar""".toRegex().matches(it.name) }
    ?.absolutePath
    ?: return
  systemProperty(propName, path)
}
