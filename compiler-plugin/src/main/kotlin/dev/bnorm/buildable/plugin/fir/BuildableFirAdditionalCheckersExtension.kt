package dev.bnorm.buildable.plugin.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

//@sample-start:BuildableFirAdditionalCheckersExtension
class BuildableFirAdditionalCheckersExtension(
  session: FirSession,
) : FirAdditionalCheckersExtension(session) {
  override val declarationCheckers = object : DeclarationCheckers() {
    override val classCheckers: Set<FirClassChecker>
      get() = setOf(
        BuildableConstructorChecker,
      )
  }
}
//@sample-end
