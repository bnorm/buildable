package dev.bnorm.buildable.plugin

import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5

fun main() {
  generateTestGroupSuiteWithJUnit5 {
    testGroup(
      testDataRoot = "compiler-plugin/testData",
      testsRoot = "compiler-plugin/test-gen",
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
