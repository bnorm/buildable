package dev.bnorm.buildable.plugin.fir

import dev.bnorm.buildable.plugin.fir.checkers.BuildableFirAdditionalCheckersExtension
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
