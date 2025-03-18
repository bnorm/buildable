package dev.bnorm.buildable.plugin.ir

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

//@sample-start:BuildableIrGenerationExtension
class BuildableIrGenerationExtension : IrGenerationExtension {
  override fun generate(
    moduleFragment: IrModuleFragment,
    pluginContext: IrPluginContext,
  ) {
    val transformers = listOf(
      BuildableIrVisitor(pluginContext),
    )

    for (transformer in transformers) {
      moduleFragment.acceptChildrenVoid(transformer)
    }
  }
}
//@sample-end
