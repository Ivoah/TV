package net.ivoah.tv

import java.text.SimpleDateFormat
import scalatags.Text.all._
import scalatags.Text.tags2.title

import Extensions._

object Templates {
  private val doctype = "<!DOCTYPE html>\n"
  private val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

  private def pluralize(count: Int, singular: String, plural: String): String = s"$count ${if (count == 1) singular else plural}"

  def root(shows: Seq[Show], sort_by: String): String = doctype + html(
    head(
      title("Noah's TV shows"),
      link(rel := "icon", href := "/static/favicon.png"),
      link(rel := "stylesheet", href := "/static/style.css")
    ),
    body(
      h1("Noah's TV shows"),
      p(pluralize(shows.length, "result", "results")),
      table(
        thead(
          tr(Seq("Show", "Episodes", "Last watched", "Watched with").map { header =>
            th(a(href := s"?sort_by=${header.toLowerCase.replace(" ", "_")}", header))
          })
        ),
        for (Show(title, episodes, last_watched, watched_with) <- sort_by match {
          case "show" => shows.sortBy(_.title)
          case "episodes" => shows.sortBy(_.episodes).reverse
          case "last_watched" => shows.sortBy(_.last_watched._2).reverse
          case "watched_with" => shows.sortBy(_.watched_with.size).reverse
          case _ => shows.sortBy(_.last_watched._2).reverse
        }) yield {
          tr(
            td(a(href := s"/shows/${title.urlEncoded}", title)),
            td(episodes),
            td(s"${last_watched._1} (${dateFormatter.format(last_watched._2)})"),
            td(watched_with.toSeq.flatMap(name => Seq(a(href := s"/people/${name.urlEncoded}", name), frag(", "))).dropRight(1))
          )
        }
      )
    )
  )

  def details(_title: String, watches: Seq[Watch], sort_by: String, nav_back: Boolean): String = doctype + html(
    head(
      title(_title),
      link(rel := "icon", href := "/static/favicon.png"),
      link(rel := "stylesheet", href := "/static/style.css")
    ),
    body(
      if (nav_back) a(`class` := "lnav", href := "/", "< all shows") else frag(),
      h1(_title),
      p(pluralize(watches.length, "result", "results")),
      table(
        thead(
          tr(Seq("Show", "Episode", "Date", "Watched with").map { header =>
            th(a(href := s"?sort_by=${header.toLowerCase.replace(" ", "_")}", header))
          })
        ),
        for (Watch(title, episode, date, watched_with) <- sort_by match {
          case "show" => watches.sortBy(_.show)
          case "episode" => watches.sortBy(_.episode).reverse
          case "date" => watches.sortBy(_.date).reverse
          case "watched_with" => watches.sortBy(_.watched_with.size).reverse
          case _ => watches.sortBy(_.date).reverse
        }) yield {
          tr(
            td(a(href := s"/shows/${title.urlEncoded}", title)),
            td(episode),
            td(dateFormatter.format(date)),
            td(watched_with.toSeq.flatMap(name => Seq(a(href := s"/people/${name.urlEncoded}", name), frag(", "))).dropRight(1))
          )
        }
      )
    )
  )

//  def movie(movie: Seq[Watch]): String = doctype + html(
//    head(
//      tag("title")(s"${movie.head.title}"),
//      link(rel:="icon", href:="/static/favicon.png"),
//      link(rel:="stylesheet", href:="/static/style.css")
//    ),
//    body(
//      a(href:="/", "All movies"),
//      h1(movie.head.title),
//      ul(
//        for (Watch(title, rating, cried, date, people) <- movie) yield {
//          li(s"${dateFormatter.format(date)}: ", people.flatMap(name => Seq(a(href:=s"https://journal.ivoah.net/people/$name", name), frag(", "))).dropRight(1))
//        }
//      )
//    )
//  )

//  def person(name: String, movies: Seq[Watch]): String = doctype + html()
}
