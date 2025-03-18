package dev.bnorm.buildable.plugin.fir.checkers

import dev.bnorm.buildable.plugin.BuildableNames
import dev.bnorm.buildable.plugin.fir.checkers.BuildableErrors.BUILDABLE_PRIVATE_CONSTRUCTOR
import dev.bnorm.buildable.plugin.fir.originalVisibility
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirConstructorChecker
import org.jetbrains.kotlin.fir.declarations.FirConstructor
import org.jetbrains.kotlin.fir.declarations.hasAnnotation

//@sample-start:BuildableConstructorVisibilityChecker
object BuildableConstructorVisibilityChecker :
  FirConstructorChecker(MppCheckerKind.Common) {
  override fun check(
    declaration: FirConstructor,
    context: CheckerContext,
    reporter: DiagnosticReporter,
  ) {
    val hasAnnotation = declaration.hasAnnotation(
      BuildableNames.BUILDABLE_CLASS_ID, context.session
    )
    if (!hasAnnotation) return

    if (declaration.originalVisibility == Visibilities.Private) {
      reporter.reportOn(
        declaration.source, BUILDABLE_PRIVATE_CONSTRUCTOR, context
      )
    }
  }
}
//@sample-end
