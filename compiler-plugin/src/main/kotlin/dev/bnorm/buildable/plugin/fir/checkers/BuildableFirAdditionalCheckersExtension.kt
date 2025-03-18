package dev.bnorm.buildable.plugin.fir.checkers

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirConstructorChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

//@sample-start:BuildableFirAdditionalCheckersExtension
class BuildableFirAdditionalCheckersExtension(
  session: FirSession,
) : FirAdditionalCheckersExtension(session) {
  override val declarationCheckers = object : DeclarationCheckers() {
    override val constructorCheckers: Set<FirConstructorChecker>
      get() = setOf(
        BuildableConstructorVisibilityChecker,
      )
  }
}
//@sample-end
