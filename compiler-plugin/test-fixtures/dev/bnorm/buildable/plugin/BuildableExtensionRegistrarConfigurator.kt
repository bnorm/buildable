package dev.bnorm.buildable.plugin

import dev.bnorm.buildable.plugin.fir.BuildableFirExtensionRegistrar
import dev.bnorm.buildable.plugin.ir.BuildableIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices

fun TestConfigurationBuilder.configurePlugin() {
  useConfigurators(
    ::BuildableExtensionRegistrarConfigurator,
    ::RuntimeEnvironmentConfigurator,
  )

  useCustomRuntimeClasspathProviders(
    ::RuntimeRuntimeClassPathProvider,
  )
}

class BuildableExtensionRegistrarConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {
  @OptIn(ExperimentalCompilerApi::class)
  override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
    module: TestModule,
    configuration: CompilerConfiguration,
  ) {
    FirExtensionRegistrarAdapter.registerExtension(BuildableFirExtensionRegistrar())
    IrGenerationExtension.registerExtension(BuildableIrGenerationExtension())
  }
}
