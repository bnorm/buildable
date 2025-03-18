package dev.bnorm.buildable.plugin.fir.checkers

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtConstructor

//@sample-start:BuildableErrors
object BuildableErrors : BaseDiagnosticRendererFactory() {
  val BUILDABLE_PRIVATE_CONSTRUCTOR by error0<KtConstructor<*>>(
    positioningStrategy = SourceElementPositioningStrategies.VISIBILITY_MODIFIER,
  )

  override val MAP = KtDiagnosticFactoryToRendererMap("Buildable").apply {
    put(
      BUILDABLE_PRIVATE_CONSTRUCTOR,
      "'@Buildable' cannot be applied to a private constructor."
    )
  }

  init {
    RootDiagnosticRendererFactory.registerFactory(this)
  }
}
//@sample-end
