package dev.bnorm.buildable.plugin.fir

import dev.bnorm.buildable.plugin.BuildableKey
import dev.bnorm.buildable.plugin.BuildableNames
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.plugin.createConstructor
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.plugin.createNestedClass
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.constructType
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
      LookupPredicate.BuilderContext.annotated(BuildableNames.BUILDABLE_FQ_NAME)
    //@sample-end
  }

  //@sample-start:registerPredicates
  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(ANNOTATION_PREDICATE)
  }
  //@sample-end

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

    val builderClass = createNestedClass(
      owner = owner,
      name = BuildableNames.BUILDER_CLASS_NAME,
      key = BuildableKey
    ) {
      visibility = constructorSymbol.visibility
        .takeIf { it != Visibilities.Unknown }
        ?: owner.visibility
    }

    return builderClass.symbol
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
    val builderSymbol = context.owner
    if (builderSymbol.classId !in builderClassIds) return emptyList()

    val constructor =
      createConstructor(builderSymbol, BuildableKey, isPrimary = true)
    return listOf(constructor.symbol)
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

    val property = createMemberProperty(
      owner = classSymbol,
      key = BuildableKey,
      name = parameterSymbol.name,
      returnType = parameterSymbol.resolvedReturnType,
      isVal = false,
      hasBackingField = false,
    )

    return listOf(property.symbol)
  }
  //@sample-end

  //@sample-start:generateFunctions
  override fun generateFunctions(
    callableId: CallableId,
    context: MemberGenerationContext?,
  ): List<FirNamedFunctionSymbol> {
    if (callableId.callableName != BuildableNames.BUILD_FUN_NAME)
      return emptyList()

    val builderSymbol = context?.owner ?: return emptyList()
    if (builderSymbol.classId !in builderClassIds) return emptyList()

    val buildableClassId = builderSymbol.classId.outerClassId!!
    val buildableSymbol =
      session.getRegularClassSymbolByClassId(buildableClassId)!!
    val build = createMemberFunction(
      owner = builderSymbol,
      key = BuildableKey,
      name = callableId.callableName,
      returnType = buildableSymbol.constructType(),
    )

    return listOf(build.symbol)
  }
  //@sample-end
}
//@sample-end
