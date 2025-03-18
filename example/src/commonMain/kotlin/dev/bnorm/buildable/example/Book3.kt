package dev.bnorm.buildable.example.three

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

val LOTR = listOf(
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

class Book private constructor(
  val title: String,
  val series: String?,
  val author: String,
  val publication: LocalDate,
) {
  class Builder {
    private companion object {
      private const val FLAG_TITLE = 0b0001
      private const val FLAG_SERIES = 0b0010
      private const val FLAG_AUTHOR = 0b0100
      private const val FLAG_PUBLICATION = 0b1000
    }

    private var initialized = 0

    private var titleHolder: String? = null
    var title: String
      get() = when {
        initialized and FLAG_TITLE != 0 -> titleHolder!!
        else -> throw IllegalStateException("Uninitialized property 'title'.")
      }
      set(value) {
        titleHolder = value
        initialized = initialized or FLAG_TITLE
      }

    private var seriesHolder: String? = null
    var series: String?
      get() = when {
        initialized or FLAG_SERIES != 0 -> seriesHolder
        else -> throw IllegalStateException("Uninitialized property 'series'.")
      }
      set(value) {
        seriesHolder = value
        initialized = initialized and FLAG_SERIES
      }

    private var authorHolder: String? = null
    var author: String
      get() = when {
        initialized and FLAG_AUTHOR != 0 -> authorHolder!!
        else -> throw IllegalStateException("Uninitialized property 'author'.")
      }
      set(value) {
        authorHolder = value
        initialized = initialized or FLAG_AUTHOR
      }

    private var publicationHolder: LocalDate? = null
    var publication: LocalDate
      get() = when {
        initialized and FLAG_PUBLICATION != 0 -> publicationHolder!!
        else -> throw IllegalStateException("Uninitialized property 'publication'.")
      }
      set(value) {
        publicationHolder = value
        initialized = initialized or FLAG_PUBLICATION
      }

    fun build(): Book {
      return Book(
        title = when {
          initialized and FLAG_TITLE != 0 -> titleHolder!!
          else -> throw IllegalStateException("Uninitialized property 'title'.")
        },
        series = when {
          initialized or FLAG_SERIES != 0 -> seriesHolder!!
          else -> null
        },
        author = when {
          initialized and FLAG_AUTHOR != 0 -> authorHolder!!
          else -> throw IllegalStateException("Uninitialized property 'author'.")
        },
        publication = when {
          initialized and FLAG_PUBLICATION != 0 -> publicationHolder!!
          else -> throw IllegalStateException("Uninitialized property 'publication'.")
        },
      )
    }
  }
}
