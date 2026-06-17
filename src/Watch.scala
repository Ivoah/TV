package net.ivoah.tv

import java.time.LocalDate

case class Watch(show: String, episode: String, date: LocalDate, watched_with: Set[String])

object Watch {
  def get(show: Option[String] = None, person: Option[String] = None)(implicit db: Connector): Seq[Watch] = {
    val order_by = "ORDER BY date ASC".raw
    (
      if (show.nonEmpty) sql"SELECT * FROM tv WHERE `show` = ${show.get} $order_by"
      else if (person.nonEmpty) sql"SELECT * FROM tv WHERE LOCATE(${person.get}, watched_with) $order_by"
      else sql"SELECT * FROM tv $order_by"
    ).query(r => Watch(
      r.getString("show"),
      r.getString("episode"),
      r.getDate("date").toLocalDate,
      r.getString("watched_with").split(", ").filter(_.nonEmpty).toSet
    ))
  }
}
