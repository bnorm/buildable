import dev.bnorm.buildable.Buildable

fun box() = "OK"

class Person <!BUILDABLE_MULTIPLE_CONSTRUCTORS!>@Buildable<!> constructor(
  val name: String,
  val nickname: String? = name,
  val age: Int = 0,
) {
  <!BUILDABLE_MULTIPLE_CONSTRUCTORS!>@Buildable<!>
  constructor(
    first: String,
    last: String,
  ) : this("$first $last")
}
