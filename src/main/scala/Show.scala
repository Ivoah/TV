package net.ivoah.tv

import java.sql.Connection
import java.util.Date
import scala.collection.mutable
import scala.util.Using

case class Show(title: String, episodes: Int, last_watched: (String, Date), watched_with: Set[String])

object Show {
  def getAll()(implicit db: Connection): Seq[Show] = {
    Using.resource({
      db.prepareStatement("SELECT `show` `title`, count(*) `episodes`, max(date) `last_watched`, episode, group_concat(watched_with SEPARATOR ', ') `watched_with` FROM tv GROUP BY `show` ORDER BY `last_watched` DESC;")
    }) { stmt =>
      val results = stmt.executeQuery()
      val buffer = mutable.Buffer[Show]()
      while (results.next()) {
        buffer.append(Show(
          results.getString("title"),
          results.getInt("episodes"),
          (results.getString("episode"), results.getDate("last_watched")),
          results.getString("watched_with").split(", ").filter(_.nonEmpty).toSet
        ))
      }
      buffer.toSeq
    }
  }
}
