package dev.bnorm.buildable.plugin

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

//@sample-start:BuildableNames
object BuildableNames {
  val BUILDABLE_FQ_NAME = FqName("dev.bnorm.buildable.Buildable")
  val BUILDABLE_CLASS_ID = ClassId.topLevel(BUILDABLE_FQ_NAME)

  val BUILDER_CLASS_NAME = Name.identifier("Builder")
  val BUILD_FUN_NAME = Name.identifier("build")
}
//@sample-end
