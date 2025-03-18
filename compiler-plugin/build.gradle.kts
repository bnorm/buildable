plugins {
  kotlin("jvm")
  java
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xwhen-guards")
  }
}

val buildableRuntimeClasspath: Configuration by configurations.creating

dependencies {
  compileOnly(kotlin("compiler"))

  testImplementation(kotlin("test-junit5"))
  testImplementation(kotlin("compiler-internal-test-framework"))
  testImplementation(kotlin("compiler"))

  buildableRuntimeClasspath(project(":runtime"))

  // Dependencies required to run the internal test framework.
  testRuntimeOnly(kotlin("reflect"))
  testRuntimeOnly(kotlin("test"))
  testRuntimeOnly(kotlin("script-runtime"))
  testRuntimeOnly(kotlin("annotations-jvm"))
}

tasks.withType<Test> {
  dependsOn(buildableRuntimeClasspath)
  inputs.dir(layout.projectDirectory.dir("src/test/data"))
    .withPropertyName("testData")
    .withPathSensitivity(PathSensitivity.RELATIVE)

  workingDir = rootDir

  useJUnitPlatform()

  systemProperty("buildableRuntime.classpath", buildableRuntimeClasspath.asPath)

  // Properties required to run the internal test framework.
  systemProperty("idea.ignore.disabled.plugins", "true")
  systemProperty("idea.home.path", project.rootDir)
}

tasks.create<JavaExec>("generateTests") {
  classpath = sourceSets.test.get().runtimeClasspath
  mainClass.set("dev.bnorm.buildable.plugin.GenerateTestsKt")
  workingDir = rootDir

  inputs.dir(layout.projectDirectory.dir("src/test/data"))
    .withPropertyName("testData")
    .withPathSensitivity(PathSensitivity.RELATIVE)
  outputs.dir(layout.projectDirectory.dir("src/test/java"))
    .withPropertyName("generatedTests")
}
