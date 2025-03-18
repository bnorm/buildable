// FIR_DUMP
// DUMP_IR

import dev.bnorm.buildable.Buildable

fun box(): String {
  val holder = Holder.Builder<String>().apply {
    value = "OK"
  }.build()
  return holder.value
}

class Holder<out T : Any> @Buildable constructor(
  val value: T,
)
