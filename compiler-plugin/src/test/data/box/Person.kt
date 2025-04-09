// FIR_DUMP
// DUMP_IR

import dev.bnorm.buildable.Buildable
import kotlin.test.*

fun box(): String {
  val person = Person.Builder().apply {
    name = "Brian"
    age = -1
  }.build()

  assertEquals(person, Person("Brian", "Brian", -1))
  return "OK"
}

data class Person @Buildable constructor(
  val name: String,
  val nickname: String? = name,
  val age: Int = 0,
)
