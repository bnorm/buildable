package dev.bnorm.buildable.plugin.fir

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

//@sample-start:BuildableFirExtensionRegistrar
class BuildableFirExtensionRegistrar : FirExtensionRegistrar() {
  override fun ExtensionRegistrarContext.configurePlugin() {
    +::BuildableFirDeclarationGenerationExtension
    +::BuildableFirAdditionalCheckersExtension
  }
}
//@sample-end
