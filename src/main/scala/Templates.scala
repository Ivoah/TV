package net.ivoah.movies

import java.text.SimpleDateFormat
import scalatags.Text.all._
import scalatags.Text.tags2.title

import Extensions._

object Templates {
  private val doctype = "<!DOCTYPE html>\n"
  private val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

  private def pluralize(count: Int, singular: String, plural: String): String = s"$count ${if (count == 1) singular else plural}"

  def root(_title: String, movies: Seq[MovieWatch], sort_by: String): String = doctype + html(
    head(
      title(_title),
      link(rel:="icon", href:="/static/favicon.png"),
      link(rel:="stylesheet", href:="/static/style.css")
    ),
    body(
      h1(_title),
      p(pluralize(movies.length, "result", "results")),
      table(
        thead(
          tr(Seq("Title", "Rating", "Cried?", "Date", "Watched with").map { header =>
            th(a(href:=s"?sort_by=${header.toLowerCase.replace(" ", "_").replace("?", "")}", header))
          })
        ),
        for (MovieWatch(title, rating, cried, date, watched_with) <- sort_by match {
          case "title" => movies.sortBy(_.title)
          case "rating" => movies.sortBy(_.rating).reverse
          case "cried" => movies.sortBy(!_.cried)
          case "date" => movies.sortBy(_.date).reverse
          case "watched_with" => movies.sortBy(_.watched_with.length).reverse
          case _ => movies.sortBy(_.date).reverse
        }) yield {
          tr(
            td(a(href:=s"/movies/${title.urlEncoded}", title)),
            td(rating),
            td(if (cried) "✓" else "✗"),
            td(dateFormatter.format(date)),
            td(watched_with.flatMap(name => Seq(a(href:=s"/people/${name.urlEncoded}", name), frag(", "))).dropRight(1))
          )
        }
      )
    )
  )

  def movie(movie: Seq[MovieWatch]): String = doctype + html(
    head(
      tag("title")(s"${movie.head.title}"),
      link(rel:="icon", href:="/static/favicon.png"),
      link(rel:="stylesheet", href:="/static/style.css")
    ),
    body(
      a(href:="/", "All movies"),
      h1(movie.head.title),
      ul(
        for (MovieWatch(title, rating, cried, date, people) <- movie) yield {
          li(s"${dateFormatter.format(date)}: ", people.flatMap(name => Seq(a(href:=s"https://journal.ivoah.net/people/$name", name), frag(", "))).dropRight(1))
        }
      )
    )
  )
  
  def person(name: String, movies: Seq[MovieWatch]): String = doctype + html()
}
