package dev.bnorm.buildable.plugin.fir

import dev.bnorm.buildable.plugin.BuildableKey
import dev.bnorm.buildable.plugin.BuilderClassKey
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.plugin.createConstructor
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.plugin.createNestedClass
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.scopes.getDeclaredConstructors
import org.jetbrains.kotlin.fir.scopes.impl.declaredMemberScope
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

//@sample-start:BuildableFirDeclarationGenerationExtension
class BuildableFirDeclarationGenerationExtension(
  session: FirSession,
) : FirDeclarationGenerationExtension(session) {
  companion object {
    val BUILDER_CLASS_NAME = Name.identifier("Builder")
    val BUILD_FUN_NAME = Name.identifier("build")

    private val BUILDABLE_PREDICATE = DeclarationPredicate.create {
      annotated(FqName("dev.bnorm.buildable.Buildable"))
    }

    private val HAS_BUILDABLE_PREDICATE = DeclarationPredicate.create {
      hasAnnotated(FqName("dev.bnorm.buildable.Buildable"))
    }
  }

  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(BUILDABLE_PREDICATE)
    register(HAS_BUILDABLE_PREDICATE)
  }

  override fun getNestedClassifiersNames(
    classSymbol: FirClassSymbol<*>,
    context: NestedClassGenerationContext,
  ): Set<Name> {
    val provider = session.predicateBasedProvider
    if (!provider.matches(HAS_BUILDABLE_PREDICATE, classSymbol))
      return emptySet()

    return setOf(BUILDER_CLASS_NAME)
  }

  override fun generateNestedClassLikeDeclaration(
    owner: FirClassSymbol<*>,
    name: Name,
    context: NestedClassGenerationContext,
  ): FirClassLikeSymbol<*>? {
    if (name != BUILDER_CLASS_NAME) return null

    val scope: FirScope =
      owner.declaredMemberScope(session, memberRequiredPhase = null)
    val provider = session.predicateBasedProvider
    val constructorSymbol = scope.getDeclaredConstructors()
      .singleOrNull { provider.matches(BUILDABLE_PREDICATE, it) }
      ?: return null

    val builderClass = createNestedClass(
      owner = owner,
      name = BUILDER_CLASS_NAME,
      key = BuilderClassKey(owner, constructorSymbol),
    ) {
      visibility = constructorSymbol.visibility
        .takeIf { it != Visibilities.Unknown }
        ?: owner.visibility
    }

    return builderClass.symbol
  }

  override fun getCallableNamesForClass(
    classSymbol: FirClassSymbol<*>,
    context: MemberGenerationContext,
  ): Set<Name> {
    val key = (classSymbol.origin as? FirDeclarationOrigin.Plugin)?.key
    if (key !is BuilderClassKey) return emptySet()

    return buildSet {
      add(SpecialNames.INIT)
      addAll(key.constructorSymbol.valueParameterSymbols.map { it.name })
      add(BUILD_FUN_NAME)
    }
  }

  override fun generateConstructors(
    context: MemberGenerationContext,
  ): List<FirConstructorSymbol> {
    val builderClassSymbol = context.owner
    val key = (builderClassSymbol.origin as? FirDeclarationOrigin.Plugin)?.key
    if (key !is BuilderClassKey) return emptyList()

    val constructor = createConstructor(
      owner = builderClassSymbol,
      key = BuildableKey,
      isPrimary = true,
    )

    return listOf(constructor.symbol)
  }

  override fun generateProperties(
    callableId: CallableId,
    context: MemberGenerationContext?,
  ): List<FirPropertySymbol> {
    val builderClassSymbol = context?.owner
    val key = (builderClassSymbol?.origin as? FirDeclarationOrigin.Plugin)?.key
    if (key !is BuilderClassKey) return emptyList()

    val parameterSymbol = key.constructorSymbol.valueParameterSymbols
      .singleOrNull { it.name == callableId.callableName }
      ?: return emptyList()

    val property = createMemberProperty(
      owner = builderClassSymbol,
      key = BuildableKey,
      name = parameterSymbol.name,
      returnType = parameterSymbol.resolvedReturnType,
      isVal = false,
      hasBackingField = false,
    )

    return listOf(property.symbol)
  }

  override fun generateFunctions(
    callableId: CallableId,
    context: MemberGenerationContext?,
  ): List<FirNamedFunctionSymbol> {
    if (callableId.callableName != BUILD_FUN_NAME)
      return emptyList()

    val builderClassSymbol = context?.owner
    val key = (builderClassSymbol?.origin as? FirDeclarationOrigin.Plugin)?.key
    if (key !is BuilderClassKey) return emptyList()

    val build = createMemberFunction(
      owner = builderClassSymbol,
      key = BuildableKey,
      name = callableId.callableName,
      returnType = key.ownerClassSymbol.constructType(),
    )

    return listOf(build.symbol)
  }
}
//@sample-end
