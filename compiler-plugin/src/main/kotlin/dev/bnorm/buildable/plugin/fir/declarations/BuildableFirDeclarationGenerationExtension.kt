package dev.bnorm.buildable.plugin.fir.declarations

import dev.bnorm.buildable.plugin.BuildableKey
import dev.bnorm.buildable.plugin.BuildableNames
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.BuilderContext.annotated
import org.jetbrains.kotlin.fir.plugin.createConstructor
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

//@sample-start:BuildableFirDeclarationGenerationExtension
class BuildableFirDeclarationGenerationExtension(
  session: FirSession,
) : FirDeclarationGenerationExtension(session) {
  companion object {
    //@sample-start:ANNOTATION_PREDICATE
    private val ANNOTATION_PREDICATE: LookupPredicate =
      annotated(BuildableNames.BUILDABLE_FQ_NAME)
    //@sample-end
  }

  //@sample-start:classIds
  private val classIds: Map<ClassId, FirConstructorSymbol> by lazy {
    session.predicateBasedProvider
      .getSymbolsByPredicate(ANNOTATION_PREDICATE)
      .filterIsInstance<FirConstructorSymbol>()
      .map { constructorSymbol ->
        val classSymbol = constructorSymbol.getContainingClassSymbol()!!
        classSymbol.classId to constructorSymbol
      }
      .groupBy({ it.first }, { it.second })
      .filterValues { it.size == 1 }
      .mapValues { it.value.single() }
  }
  //@sample-end

  //@sample-start:builderClassIds
  private val builderClassIds: Map<ClassId, FirConstructorSymbol> by lazy {
    classIds.mapKeys { (key, _) ->
      key.createNestedClassId(BuildableNames.BUILDER_CLASS_NAME)
    }
  }
  //@sample-end

  //@sample-start:registerPredicates
  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(ANNOTATION_PREDICATE)
  }
  //@sample-end

  //@sample-start:getNestedClassifiersNames
  override fun getNestedClassifiersNames(
    classSymbol: FirClassSymbol<*>,
    context: NestedClassGenerationContext,
  ): Set<Name> {
    if (classSymbol.classId !in classIds) return emptySet()

    return setOf(
      BuildableNames.BUILDER_CLASS_NAME,
    )
  }
  //@sample-end

  //@sample-start:generateNestedClassLikeDeclaration
  override fun generateNestedClassLikeDeclaration(
    owner: FirClassSymbol<*>,
    name: Name,
    context: NestedClassGenerationContext,
  ): FirClassLikeSymbol<*>? {
    if (name != BuildableNames.BUILDER_CLASS_NAME) return null
    val constructorSymbol = classIds[owner.classId] ?: return null

    return createBuilderClass(owner, constructorSymbol).symbol
  }
  //@sample-end

  //@sample-start:getCallableNamesForClass
  override fun getCallableNamesForClass(
    classSymbol: FirClassSymbol<*>,
    context: MemberGenerationContext,
  ): Set<Name> {
    val constructorSymbol = builderClassIds[classSymbol.classId]
      ?: return emptySet()

    return buildSet {
      add(SpecialNames.INIT)
      addAll(constructorSymbol.valueParameterSymbols.map { it.name })
      add(BuildableNames.BUILD_FUN_NAME)
    }
  }
  //@sample-end

  //@sample-start:generateConstructors
  override fun generateConstructors(
    context: MemberGenerationContext,
  ): List<FirConstructorSymbol> {
    val classSymbol = context.owner
    if (classSymbol.classId !in builderClassIds) return emptyList()

    return listOf(
      createConstructor(classSymbol, BuildableKey, isPrimary = true).symbol
    )
  }
  //@sample-end

  //@sample-start:generateProperties
  override fun generateProperties(
    callableId: CallableId,
    context: MemberGenerationContext?,
  ): List<FirPropertySymbol> {
    val classSymbol = context?.owner ?: return emptyList()
    val constructorSymbol = builderClassIds[classSymbol.classId]
      ?: return emptyList()

    val parameterSymbol = constructorSymbol.valueParameterSymbols
      .singleOrNull { it.name == callableId.callableName }
      ?: return emptyList()

    return listOf(
      createBuilderProperty(classSymbol, parameterSymbol).symbol
    )
  }
  //@sample-end

  //@sample-start:generateFunctions
  override fun generateFunctions(
    callableId: CallableId,
    context: MemberGenerationContext?,
  ): List<FirNamedFunctionSymbol> {
    if (callableId.callableName != BuildableNames.BUILD_FUN_NAME)
      return emptyList()

    val classSymbol = context?.owner ?: return emptyList()
    if (classSymbol.classId !in builderClassIds) return emptyList()

    return listOf(
      createBuilderBuild(classSymbol, callableId).symbol
    )
  }
  //@sample-end
}
//@sample-end
