package net.ivoah.tv

import scalatags.Text.all.*
import java.time.LocalDate

import net.ivoah.tv.Extensions.*
import java.sql.ResultSet

case class Watch(id: Int, show: String, episode: String, date: LocalDate, watched_with: Set[String])

object Watch {
  def fromResultSet(r: ResultSet): Watch = Watch(
    r.getInt("id"),
    r.getString("show"),
    r.getString("episode"),
    r.getDate("date").toLocalDate,
    r.getString("watched_with").split(", ").filter(_.nonEmpty).toSet
  )

  def get(id: Int)(implicit db: Connector): Option[Watch] = sql"SELECT * FROM tv WHERE id=$id".query(fromResultSet).headOption
  def forShow(show: String)(implicit db: Connector): Seq[Watch] = sql"SELECT * FROM tv WHERE `show` = $show ORDER BY date ASC".query(fromResultSet)
  def forPerson(person: String)(implicit db: Connector): Seq[Watch] = sql"SELECT * FROM tv WHERE LOCATE($person, watched_with) ORDER BY date ASC".query(fromResultSet)
}
