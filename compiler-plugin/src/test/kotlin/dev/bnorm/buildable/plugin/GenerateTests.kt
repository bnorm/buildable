package dev.bnorm.buildable.plugin

import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5

fun main() {
  generateTestGroupSuiteWithJUnit5 {
    testGroup(
      testDataRoot = "compiler-plugin/src/test/data",
      testsRoot = "compiler-plugin/src/test/java",
    ) {
      testClass<AbstractBoxTest> {
        model("box")
      }
      testClass<AbstractDiagnosticTest> {
        model("diagnostic")
      }
    }
  }
}
