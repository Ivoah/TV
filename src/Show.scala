package net.ivoah.tv

import java.sql.Connection
import java.util.Date
import scala.collection.mutable
import scala.util.Using

case class Show(title: String, episodes: Int, last_watched: (String, Date), watched_with: Set[String])

object Show {
  def getAll()(implicit db: Connection): Seq[Show] = {
    Using.resource({
      db.prepareStatement(
        """SELECT
          |  t1.`show` `title`,
          |  t2.`episodes`,
          |  t1.`date` `last_watched`,
          |  t1.`episode`,
          |  t2.`watched_with`
          |FROM tv t1
          |RIGHT JOIN (
          |  SELECT
          |    max(id) `max_id`,
          |    `show`,
          |    count(*) `episodes`,
          |    group_concat(watched_with SEPARATOR ', ') `watched_with`
          |  FROM tv
          |  GROUP BY `show`
          |) t2
          |ON t1.id = t2.max_id AND t1.show = t2.show
          |ORDER BY date DESC
          |""".stripMargin)
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
