package dev.bnorm.buildable.plugin

import dev.bnorm.buildable.plugin.fir.BuildableFirExtensionRegistrar
import dev.bnorm.buildable.plugin.ir.BuildableIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

//@sample-start:BuildableCompilerPluginRegistrar
@OptIn(ExperimentalCompilerApi::class)
class BuildableCompilerPluginRegistrar : CompilerPluginRegistrar() {
   override val supportsK2: Boolean get() = true

  override fun ExtensionStorage.registerExtensions(
    configuration: CompilerConfiguration,
  ) {
    FirExtensionRegistrarAdapter.registerExtension(
      BuildableFirExtensionRegistrar()
    )
    IrGenerationExtension.registerExtension(
      BuildableIrGenerationExtension()
    )
  }
}
//@sample-end
