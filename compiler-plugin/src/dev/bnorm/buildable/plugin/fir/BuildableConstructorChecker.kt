package dev.bnorm.buildable.plugin.fir

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaredMemberScope
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

//@sample-start:BuildableConstructorChecker
object BuildableConstructorChecker : FirClassChecker(MppCheckerKind.Common) {

  private val BUILDABLE_CLASS_ID = ClassId.topLevel(
    FqName("dev.bnorm.buildable.Buildable")
  )

  context(context: CheckerContext, reporter: DiagnosticReporter)
  override fun check(declaration: FirClass) {
    val annotations = buildList {
      val scope = declaration.symbol.declaredMemberScope(context)
      scope.processDeclaredConstructors { constructor ->
        for (annotation in constructor.resolvedAnnotationsWithClassIds) {
          if (annotation.resolvedType.classId == BUILDABLE_CLASS_ID) {
            add(annotation)
          }
        }
      }
    }

    if (annotations.size > 1) {
      for (annotation in annotations) {
        reporter.reportOn(
          source = annotation.source,
          factory = BuildableErrors.BUILDABLE_MULTIPLE_CONSTRUCTORS,
        )
      }
    }
  }
}
//@sample-end
