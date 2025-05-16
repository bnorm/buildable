package dev.bnorm.buildable.plugin.fir

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtAnnotation

//@sample-start:BuildableErrors
object BuildableErrors : BaseDiagnosticRendererFactory() {
  val BUILDABLE_MULTIPLE_CONSTRUCTORS by error0<KtAnnotation>()

  override val MAP = KtDiagnosticFactoryToRendererMap("Buildable").apply {
    put(
      BUILDABLE_MULTIPLE_CONSTRUCTORS,
      "'@Buildable' cannot be applied to multiple constructors in the same class."
    )
  }

  init {
    RootDiagnosticRendererFactory.registerFactory(this)
  }
}
//@sample-end
