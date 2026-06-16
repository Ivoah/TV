package net.ivoah.tv

import java.sql.Connection
import java.util.Date
import scala.collection.mutable
import scala.util.Using

case class Watch(show: String, episode: String, date: Date, watched_with: Set[String])

object Watch {
  def get(show: Option[String] = None, person: Option[String] = None)(implicit db: Connection): Seq[Watch] = {
    val order_by = "ORDER BY date ASC"
    Using.resource({
      if (show.nonEmpty) db.prepareStatement(s"SELECT * FROM tv WHERE `show` = ? $order_by")
      else if (person.nonEmpty) db.prepareStatement(s"SELECT * FROM tv WHERE LOCATE(?, watched_with) $order_by")
      else db.prepareStatement(s"SELECT * FROM tv $order_by")
    }) { stmt =>
      show.foreach(stmt.setString(1, _))
      person.foreach(stmt.setString(1, _))
      val results = stmt.executeQuery()
      val buffer = mutable.Buffer[Watch]()
      while (results.next()) {
        buffer.append(Watch(
          results.getString("show"),
          results.getString("episode"),
          results.getDate("date"),
          results.getString("watched_with").split(", ").filter(_.nonEmpty).toSet
        ))
      }
      buffer.toSeq
    }
  }
}
