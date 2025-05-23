package dev.bnorm.buildable.plugin.ir

import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.irAttribute

//@sample-start:BuilderPropertyBacking
class BuilderPropertyBacking(
  val holder: IrField,
  val flag: IrField
)

var IrProperty.builderPropertyBacking: BuilderPropertyBacking?
  by irAttribute(copyByDefault = false)
//@sample-end
