package dev.bnorm.buildable.plugin.fir

import dev.bnorm.buildable.plugin.BuildableNames
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.copy
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.extensions.FirStatusTransformerExtension
import org.jetbrains.kotlin.fir.scopes.FirContainingNamesAwareScope
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.scopes.getDeclaredConstructors
import org.jetbrains.kotlin.fir.scopes.impl.declaredMemberScope
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol

//@sample-start:BuildableFirStatusTransformerExtension
class BuildableFirStatusTransformerExtension(
  session: FirSession,
) : FirStatusTransformerExtension(session) {
  //@sample-start:needTransformStatus
  override fun needTransformStatus(declaration: FirDeclaration): Boolean {
    if (declaration !is FirConstructor) return false
    val classSymbol = declaration.getContainingClassSymbol()
      as? FirClassSymbol ?: return false

    // WRONG! Use scopes! What are scopes?
    // val constructorSymbols = classSymbol.declarationSymbols
    //   .filterIsInstance<FirConstructorSymbol>()

    val declaredMemberScope: FirScope =
      classSymbol.declaredMemberScope(session, memberRequiredPhase = null)
    val constructorSymbols = declaredMemberScope.getDeclaredConstructors()

    return constructorSymbols.singleOrNull {
      it.hasAnnotation(BuildableNames.BUILDABLE_CLASS_ID, session)
    } != null
  }
  //@sample-end


  //@sample-start:transformStatus
  override fun transformStatus(
    status: FirDeclarationStatus,
    constructor: FirConstructor,
    containingClass: FirClassLikeSymbol<*>?,
    isLocal: Boolean
  ): FirDeclarationStatus {
    constructor.originalVisibility = status.visibility
    return when (status.visibility) {
      Visibilities.Private -> status
      else -> status.copy(visibility = Visibilities.Private)
    }
  }
  //@sample-end
}
//@sample-end

//@sample-start:originalVisibility
private object OriginalVisibility : FirDeclarationDataKey()

var FirDeclaration.originalVisibility: Visibility?
  by FirDeclarationDataRegistry.data(OriginalVisibility)

val FirBasedSymbol<FirDeclaration>.originalVisibility: Visibility?
  by FirDeclarationDataRegistry.symbolAccessor(OriginalVisibility)
//@sample-end
