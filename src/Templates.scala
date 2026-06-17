package net.ivoah.tv

import scalatags.Text.all._
import scalatags.Text.tags2.title

import java.time.LocalDate

import Extensions._

object IntExtractor {
  def unapply(s: String): Option[Int] = s.toIntOption
}

object Templates {
  private val doctype = "<!DOCTYPE html>\n"

  private def pluralize(count: Int, singular: String, plural: String): String = s"$count ${if (count == 1) singular else plural}"

  private def page(_title: String)(_body: Frag*): String = doctype + html(
    head(
      title(_title),
      link(rel:="icon", href:="/static/favicon.png"),
      link(rel:="stylesheet", href:="/static/style.css"),
      script(src:="/static/jquery-4.0.0.min.js"),
      script(src:="/static/common.js")
    ),
    body(_body)
  )

  def root(shows: Seq[Show], sort_by: String): String = page("Noah's TV shows")(
    h1("Noah's TV shows"),
    p(pluralize(shows.length, "result", "results")),
    table(
      thead(
        tr(Seq("Show", "Episodes", "Last watched", "Watched with").map { header =>
          th(a(href:=s"?sort_by=${header.toLowerCase.replace(" ", "_")}", header))
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
          td(a(href:=s"/shows/${title.urlEncoded}", title)),
          td(episodes),
          td(s"${last_watched._1} (${last_watched._2})"),
          td(watched_with.toSeq.flatMap(name => Seq(a(href:=s"/people/${name.urlEncoded}", name), frag(", "))).dropRight(1))
        )
      }
    )
  )

  def details(show: String, watches: Seq[Watch], sort_by: String, nav_back: Boolean): String = page(show)(
    if (nav_back) a(`class`:="lnav", href:="/", "< all shows") else frag(),
    h1(show),
    p(pluralize(watches.length, "result", "results")),
    table(
      thead(
        tr(Seq("Show", "Episode", "Date", "Watched with").map { header =>
          th(a(href:=s"?sort_by=${header.toLowerCase.replace(" ", "_")}", header))
        })
      ),
      tbody(
        form(method:="POST", action:=s"/shows/${show.urlEncoded}",
          tr(
            td(a(href:=s"/shows/${show.urlEncoded}", show)),
            td(input(`type`:="text", name:="episode", value:={
              watches.sortBy(_.date).lastOption.map(_.episode) match {
                case Some(s"s${IntExtractor(season)}e${IntExtractor(episode)}") =>
                  f"s$season%02de${episode + 1}%02d"
                case _ => ""
              }
            })),
            td(input(`type`:="date", name:="date", value:=LocalDate.now().toString)),
            td(input(`type`:="text", name:="watched_with")),
            td(button("Add"))
          )
        ),
        for (watch <- sort_by match {
          case "show" => watches.sortBy(_.show)
          case "episode" => watches.sortBy(_.episode).reverse
          case "date" => watches.sortBy(_.date).reverse
          case "watched_with" => watches.sortBy(_.watched_with.size).reverse
          case _ => watches.sortBy(_.date).reverse
        }) yield {
          tr(
            td(a(href:=s"/shows/${show.urlEncoded}", show)),
            td(watch.episode),
            td(watch.date.toString),
            td(watch.watched_with.toSeq.flatMap(name => Seq(a(href:=s"/people/${name.urlEncoded}", name), frag(", "))).dropRight(1)),
            td(form(method:="DELETE", action:=s"/shows/$show/${watch.id}", button("Delete")))
          )
        }
      )
    )
  )
}
