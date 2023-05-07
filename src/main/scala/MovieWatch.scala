package net.ivoah.movies

import java.sql.Connection
import java.util.Date
import scala.collection.mutable
import scala.util.Using

case class MovieWatch(title: String, rating: Double, cried: Boolean, date: Date, watched_with: Seq[String])

object MovieWatch {
  def get(title: Option[String] = None, person: Option[String] = None)(implicit db: Connection): Seq[MovieWatch] = {
    Using.resource({
      if (title.nonEmpty) db.prepareStatement("SELECT * FROM movies WHERE title = ?")
      else if (person.nonEmpty) db.prepareStatement("SELECT * FROM movies WHERE LOCATE(?, watched_with)")
      else db.prepareStatement("SELECT * FROM movies")
    }) { stmt =>
      title.foreach(stmt.setString(1, _))
      person.foreach(stmt.setString(1, _))
      val results = stmt.executeQuery()
      val buffer = mutable.Buffer[MovieWatch]()
      while (results.next()) {
        buffer.append(MovieWatch(
          results.getString("title"),
          results.getDouble("rating"),
          results.getBoolean("cried"),
          results.getDate("date"),
          results.getString("watched_with").split(", ").filter(_.nonEmpty)
        ))
      }
      buffer.toSeq
    }
  }
}
