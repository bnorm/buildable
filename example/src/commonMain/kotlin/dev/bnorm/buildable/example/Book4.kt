package dev.bnorm.buildable.example.four

import dev.bnorm.buildable.Buildable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

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

class Book @Buildable constructor(
  val title: String,
  val series: String? = null,
  val author: String,
  val publication: LocalDate,
)
