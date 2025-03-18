package dev.bnorm.buildable.plugin.fir

import dev.bnorm.buildable.plugin.fir.checkers.BuildableFirAdditionalCheckersExtension
import dev.bnorm.buildable.plugin.fir.declarations.BuildableFirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

//@sample-start:BuildableFirExtensionRegistrar
class BuildableFirExtensionRegistrar : FirExtensionRegistrar() {
  override fun ExtensionRegistrarContext.configurePlugin() {
    +::BuildableFirDeclarationGenerationExtension
    +::BuildableFirStatusTransformerExtension
    +::BuildableFirAdditionalCheckersExtension
  }
}
//@sample-end
