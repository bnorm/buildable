package dev.bnorm.buildable.plugin.ir

import dev.bnorm.buildable.plugin.BuildableKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin.GeneratedByPlugin
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

//@sample-start:BuildableIrVisitor
@OptIn(UnsafeDuringIrConstructionAPI::class)
class BuildableIrVisitor(
  private val context: IrPluginContext,
) : IrVisitorVoid() {
  //@sample-start:companion
  companion object {
    private val BUILDABLE_ORIGIN = GeneratedByPlugin(BuildableKey)

    private val ILLEGAL_STATE_EXCEPTION_FQ_NAME =
      FqName("kotlin.IllegalStateException")
    private val ILLEGAL_STATE_EXCEPTION_CLASS_ID =
      ClassId.topLevel(ILLEGAL_STATE_EXCEPTION_FQ_NAME)
  }
  //@sample-end

  //@sample-start:properties
  private val nullableStringType = context.irBuiltIns.stringType.makeNullable()
  private val illegalStateExceptionConstructor =
    context.referenceConstructors(ILLEGAL_STATE_EXCEPTION_CLASS_ID)
      .single { constructor ->
        val parameter = constructor.owner.valueParameters.singleOrNull()
          ?: return@single false
        parameter.type == nullableStringType
      }
  //@sample-end

  //@sample-start:visitElement
  override fun visitElement(element: IrElement) {
    when (element) {
      is IrDeclaration,
      is IrFile,
      is IrModuleFragment,
        -> element.acceptChildrenVoid(this)

      else -> Unit
    }
  }
  //@sample-end

  //@sample-start:visitConstructor
  override fun visitConstructor(declaration: IrConstructor) {
    if (declaration.origin == BUILDABLE_ORIGIN && declaration.body == null) {
      declaration.body = generateDefaultConstructor(declaration)
    }
  }
  //@sample-end

  //@sample-start:visitClass
  override fun visitClass(declaration: IrClass) {
    if (declaration.origin == BUILDABLE_ORIGIN) {
      val declarations = declaration.declarations

      val builderPropertyDeclarations = declarations
        .filterIsInstance<IrProperty>()
        .map { generateBacking(declaration, it) }
        .flatMap { listOf(it.flag, it.holder) }

      declarations.addAll(0, builderPropertyDeclarations)
    }

    declaration.acceptChildrenVoid(this)
  }
  //@sample-end

  //@sample-start:visitSimpleFunction
  override fun visitSimpleFunction(declaration: IrSimpleFunction) {
    if (declaration.origin == BUILDABLE_ORIGIN && declaration.body == null) {
      declaration.body = generateBuildFunction(declaration)
    }
  }
  //@sample-end

  //@sample-start:generateDefaultConstructor
  private fun generateDefaultConstructor(declaration: IrConstructor): IrBody? {
    val parentClass = declaration.parent as? IrClass ?: return null
    val anyConstructor = context.irBuiltIns.anyClass.owner.primaryConstructor
      ?: return null

    val irBuilder = DeclarationIrBuilder(context, declaration.symbol)
    return irBuilder.irBlockBody {
      +irDelegatingConstructorCall(anyConstructor)
      +IrInstanceInitializerCallImpl(
        startOffset, endOffset,
        classSymbol = parentClass.symbol,
        type = context.irBuiltIns.unitType,
      )
    }
  }
  //@sample-end

  //@sample-start:generateBacking
  private fun generateBacking(
    klass: IrClass,
    property: IrProperty,
  ): BuilderPropertyBacking {
    val getter = property.getter!!
    val setter = property.setter!!
    property.backingField = null

    val isPrimitive = getter.returnType.isPrimitiveType()
    val backingType = when {
      isPrimitive -> getter.returnType
      else -> getter.returnType.makeNullable()
    }

    val flagField = context.irFactory.buildField {
      origin = BUILDABLE_ORIGIN
      visibility = DescriptorVisibilities.PRIVATE
      name = Name.identifier("${property.name}\$BuildableFlag")
      type = context.irBuiltIns.booleanType
    }.apply {
      parent = klass
      initializer = context.irFactory.createExpressionBody(
        expression = false.toIrConst(context.irBuiltIns.booleanType)
      )
    }

    val holderField = context.irFactory.buildField {
      origin = BUILDABLE_ORIGIN
      visibility = DescriptorVisibilities.PRIVATE
      name = Name.identifier("${property.name}\$BuildableHolder")
      type = backingType
    }.apply {
      parent = klass
      initializer = context.irFactory.createExpressionBody(
        expression = when (isPrimitive) {
          true -> IrConstImpl.defaultValueForType(
            SYNTHETIC_OFFSET,
            SYNTHETIC_OFFSET,
            backingType
          )

          false -> null.toIrConst(backingType)
        }
      )
    }

    getter.origin = BUILDABLE_ORIGIN
    getter.body = DeclarationIrBuilder(context, getter.symbol).irBlockBody {
      val dispatch = getter.dispatchReceiverParameter!!
      +irIfThenElse(
        type = getter.returnType,
        condition = irGetField(irGet(dispatch), flagField),
        thenPart = irReturn(irGetField(irGet(dispatch), holderField)),
        elsePart = irThrow(irUninitializedProperty(property))
      )
    }

    setter.origin = BUILDABLE_ORIGIN
    setter.body = DeclarationIrBuilder(context, setter.symbol).irBlockBody {
      val dispatch = setter.dispatchReceiverParameter!!
      +irSetField(
        receiver = irGet(dispatch),
        field = holderField,
        value = irGet(setter.valueParameters[0])
      )
      +irSetField(
        receiver = irGet(dispatch),
        field = flagField,
        value = true.toIrConst(context.irBuiltIns.booleanType)
      )
    }

    val builderPropertyBacking = BuilderPropertyBacking(holderField, flagField)
    property.builderPropertyBacking = builderPropertyBacking
    return builderPropertyBacking
  }
  //@sample-end

  //@sample-start:generateBuildFunction
  private fun generateBuildFunction(function: IrSimpleFunction): IrBody? {
    val builderClass = function.parent
      as? IrClass ?: return null
    val irClass = function.returnType.classifierOrNull?.owner
      as? IrClass ?: return null
    val primaryConstructor = irClass.primaryConstructor
      ?: return null

    val irBuilder = DeclarationIrBuilder(context, function.symbol)
    return irBuilder.irBlockBody {
      val arguments = irConstructorArguments(
        constructorParameters = primaryConstructor.valueParameters,
        builderProperties = builderClass.declarations.filterIsInstance<IrProperty>(),
        dispatchReceiverParameter = function.dispatchReceiverParameter!!
      )

      val constructorCall = irCall(primaryConstructor).apply {
        arguments.forEachIndexed { index, variable ->
          putValueArgument(index, irGet(variable))
        }
      }

      +irReturn(constructorCall)
    }
  }
  //@sample-end

  //@sample-start:irConstructorArguments
  private fun IrBlockBodyBuilder.irConstructorArguments(
    constructorParameters: List<IrValueParameter>,
    builderProperties: List<IrProperty>,
    dispatchReceiverParameter: IrValueParameter,
  ): List<IrVariable> {
    val variables = mutableListOf<IrVariable>()

    // Transformer to substitute references to previous constructor parameters
    // with references to previous local variables.
    val transformer = object : IrElementTransformerVoid() {
      override fun visitGetValue(expression: IrGetValue): IrExpression {
        val index = constructorParameters
          .indexOfFirst { it.symbol == expression.symbol }

        return when {
          index != -1 -> irGet(variables[index])
          else -> super.visitGetValue(expression)
        }
      }
    }

    for (valueParameter in constructorParameters) {
      val builderPropertyBacking =
        builderProperties.single { it.name == valueParameter.name }
          .builderPropertyBacking!!

      variables += irTemporary(
        nameHint = valueParameter.name.asString(),
        value = irBlock {
          val defaultValue = valueParameter.defaultValue
          val elsePart = when {
            defaultValue != null ->
              defaultValue.expression.deepCopyWithoutPatchingParents()
                .transform(transformer, null)

            else -> irThrow(irUninitializedProperty(valueParameter))
          }

          +irIfThenElse(
            type = valueParameter.type,
            condition = irGetField(
              receiver = irGet(dispatchReceiverParameter),
              field = builderPropertyBacking.flag
            ),
            thenPart = irGetField(
              receiver = irGet(dispatchReceiverParameter),
              field = builderPropertyBacking.holder
            ),
            elsePart = elsePart
          )
        },
      )
    }

    return variables
  }
  //@sample-end

  //@sample-start:irUninitializedProperty
  private fun IrBuilderWithScope.irUninitializedProperty(
    property: IrDeclarationWithName,
  ): IrConstructorCall {
    return irCall(illegalStateExceptionConstructor).apply {
      putValueArgument(
        0, irString("Uninitialized property '${property.name}'.")
      )
    }
  }
  //@sample-end
}
//@sample-end
