// FIR_DUMP
// DUMP_IR

import dev.bnorm.buildable.Buildable

fun box() = "OK"

class Person @Buildable constructor(
  val name: String,
  val nickname: String? = name,
  val age: Int = 0,
)

