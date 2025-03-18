package dev.bnorm.buildable.example.two

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

val LOTR_CONSTRUCTOR = listOf(
  Book(
    title = "The Fellowship of the Ring",
    series = "The Lord of the Rings",
    author = "J. R. R. Tolkien",
    publication = LocalDate(year = 1954, Month.JULY, dayOfMonth = 2),
  ),
  Book(
    title = "The Two Towers",
    series = "The Lord of the Rings",
    author = "J. R. R. Tolkien",
    publication = LocalDate(year = 1954, Month.NOVEMBER, dayOfMonth = 11),
  ),
  Book(
    title = "The Return of the King",
    series = "The Lord of the Rings",
    author = "J. R. R. Tolkien",
    publication = LocalDate(year = 1955, Month.OCTOBER, dayOfMonth = 20),
  ),
)

val LOTR_BUILDER = listOf(
  Book.Builder().apply {
    title = "The Fellowship of the Ring"
    series = "The Lord of the Rings"
    author = "J. R. R. Tolkien"
    publication = LocalDate(year = 1954, Month.JULY, dayOfMonth = 2)
  }.build(),
  Book.Builder().apply {
    title = "The Two Towers"
    series = "The Lord of the Rings"
    author = "J. R. R. Tolkien"
    publication = LocalDate(year = 1954, Month.NOVEMBER, dayOfMonth = 11)
  }.build(),
  Book.Builder().apply {
    title = "The Return of the King"
    series = "The Lord of the Rings"
    author = "J. R. R. Tolkien"
    publication = LocalDate(year = 1955, Month.OCTOBER, dayOfMonth = 20)
  }.build(),
)

class Book(
    val title: String,
    val series: String? = null,
    val author: String,
    val publication: LocalDate,
) {
  class Builder {
    private var title_flag: Boolean = false
    private var title_holder: String? = null
    var title: String
      get() = when {
        title_flag -> title_holder!!
        else -> throw IllegalStateException("Uninitialized property 'title'.")
      }
      set(value) {
        title_holder = value
        title_flag = true
      }

    private var series_flag: Boolean = false
    private var series_holder: String? = null
    var series: String?
      get() = when {
        series_flag -> series_holder
        else -> throw IllegalStateException("Uninitialized property 'series'.")
      }
      set(value) {
        series_holder = value
        series_flag = true
      }

    private var author_flag: Boolean = false
    private var author_holder: String? = null
    var author: String
      get() = when {
        author_flag -> author_holder!!
        else -> throw IllegalStateException("Uninitialized property 'author'.")
      }
      set(value) {
        author_holder = value
        author_flag = true
      }

    private var publication_flag: Boolean = false
    private var publication_holder: LocalDate? = null
    var publication: LocalDate
      get() = when {
        publication_flag -> publication_holder!!
        else -> throw IllegalStateException("Uninitialized property 'publication'.")
      }
      set(value) {
        publication_holder = value
        publication_flag = true
      }

    fun build() = Book(
      title = when {
        title_flag -> title_holder!!
        else -> throw IllegalStateException("Uninitialized property 'title'.")
      },
      series = when {
        series_flag -> series_holder!!
        else -> null
      },
      author = when {
        author_flag -> author_holder!!
        else -> throw IllegalStateException("Uninitialized property 'author'.")
      },
      publication = when {
        publication_flag -> publication_holder!!
        else -> throw IllegalStateException("Uninitialized property 'publication'.")
      },
    )
  }
}
