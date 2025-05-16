package dev.bnorm.buildable.plugin

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol

//@sample-start:BuildableKey
data object BuildableKey : GeneratedDeclarationKey()
//@sample-end

//@sample-start:BuilderClassKey
data class BuilderClassKey(
  val ownerClassSymbol: FirClassSymbol<*>,
  val constructorSymbol: FirConstructorSymbol,
) : GeneratedDeclarationKey()
//@sample-end
