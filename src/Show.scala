package net.ivoah.tv

import net.ivoah.sqala.*
import java.time.LocalDate

case class Show(title: String, episodes: Int, last_watched: (String, LocalDate), watched_with: Set[String])

object Show {
  def getAll()(using Connector): Seq[Show] = {
    sql"""
      SELECT
        t1.`show` `title`,
        t2.`episodes`,
        t1.`date` `last_watched`,
        t1.`episode`,
        t2.`watched_with`
      FROM tv t1
      RIGHT JOIN (
        SELECT
          max(id) `max_id`,
          `show`,
          count(*) `episodes`,
          group_concat(watched_with SEPARATOR ', ') `watched_with`
        FROM tv
        GROUP BY `show`
      ) t2
      ON t1.id = t2.max_id AND t1.show = t2.show
      ORDER BY date DESC
    """.query(r => Show(
      r.getString("title"),
      r.getInt("episodes"),
      (r.getString("episode"), r.getDate("last_watched").toLocalDate),
      r.getString("watched_with").split(", ").filter(_.nonEmpty).toSet
    ))
  }
}
