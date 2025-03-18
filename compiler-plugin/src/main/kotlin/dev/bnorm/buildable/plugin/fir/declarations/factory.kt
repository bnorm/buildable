package dev.bnorm.buildable.plugin.fir.declarations

import dev.bnorm.buildable.plugin.BuildableKey
import dev.bnorm.buildable.plugin.BuildableNames
import dev.bnorm.buildable.plugin.fir.originalVisibility
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.extensions.FirExtension
import org.jetbrains.kotlin.fir.plugin.ClassBuildingContext
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.plugin.createNestedClass
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.resolve.substitution.substitutorByMap
import org.jetbrains.kotlin.fir.scopes.impl.toConeType
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.types.Variance

//@sample-start:createBuilderClass
fun FirExtension.createBuilderClass(
  classSymbol: FirClassSymbol<*>,
  constructorSymbol: FirConstructorSymbol,
): FirRegularClass {
  return createNestedClass(
    owner = classSymbol,
    name = BuildableNames.BUILDER_CLASS_NAME,
    key = BuildableKey
  ) {
    visibility = constructorSymbol.originalVisibility
      ?: constructorSymbol.visibility
    copyTypeParameters(classSymbol, session)
  }
}
//@sample-end

//@sample-start:copyTypeParameters
private fun ClassBuildingContext.copyTypeParameters(
  classSymbol: FirClassSymbol<*>,
  session: FirSession,
) {
  for (parameter in classSymbol.typeParameterSymbols) {
    typeParameter(parameter.name, Variance.INVARIANT) {
      for (bound in parameter.resolvedBounds) {
        bound { typeParameters ->
          bound.coneType.substituteOrSelf(
            from = classSymbol.typeParameterSymbols,
            to = typeParameters.map { it.toConeType() },
            session = session
          )
        }
      }
    }
  }
}
//@sample-end

//@sample-start:substituteOrSelf
private fun ConeKotlinType.substituteOrSelf(
  from: List<FirTypeParameterSymbol>,
  to: List<ConeTypeParameterType>,
  session: FirSession,
): ConeKotlinType {
  val substitutor = substitutorByMap(from.zip(to).toMap(), session)
  return substitutor.substituteOrSelf(this)
}
//@sample-end

//@sample-start:createBuilderProperty
fun FirExtension.createBuilderProperty(
  builderClassSymbol: FirClassSymbol<*>,
  parameterSymbol: FirValueParameterSymbol,
): FirProperty {
  val classId = builderClassSymbol.classId.outerClassId!!
  val classSymbol = session.getRegularClassSymbolByClassId(classId)!!

  return createMemberProperty(
    owner = builderClassSymbol,
    key = BuildableKey,
    name = parameterSymbol.name,
    returnType = parameterSymbol.resolvedReturnType.substituteOrSelf(
      from = classSymbol.typeParameterSymbols,
      to = builderClassSymbol.typeParameterSymbols
        .map { it.toConeType() },
      session = session
    ),
    isVal = false,
    hasBackingField = false,
  )
}
//@sample-end

//@sample-start:createBuilderBuild
fun FirExtension.createBuilderBuild(
  builderClassSymbol: FirClassSymbol<*>,
  callableId: CallableId,
): FirSimpleFunction {
  val classId = builderClassSymbol.classId.outerClassId!!
  val classSymbol = session.getRegularClassSymbolByClassId(classId)!!

  return createMemberFunction(
    owner = builderClassSymbol,
    key = BuildableKey,
    name = callableId.callableName,
    returnType = classSymbol.constructType(
      typeArguments = builderClassSymbol.typeParameterSymbols
        .map { it.toConeType() }
        .toTypedArray(),
    ),
  )
}
//@sample-end
