package net.ivoah.tv

import java.nio.file.*
import net.ivoah.vial.*
import net.ivoah.tv.Extensions.urlEncoded
import com.typesafe.config.ConfigFactory

class TV()(implicit val db: Connector) {
  private val AUTH = Some(Config.auth.username, Config.auth.password)

  val router = Router {
    case ("GET", "/", request) => Response(Templates.root(
      Show.getAll(),
      request.params.getOrElse("sort_by", ""),
    ))

    case ("GET", s"/static/$file", _) => Response.forFile(Paths.get("static"), Paths.get(file))

    case ("GET", s"/shows/$show", request) => Response(Templates.details(
      show,
      Watch.forShow(show),
      request.params.getOrElse("sort_by", ""),
      nav_back = true
    ))

    case ("POST", s"/shows/$show", request) => request.auth match {
      case AUTH =>
        request.form.expect("episode", "date", "watched_with") { (episode: String, date: String, watched_with: String) =>
          val id = sql"""
            INSERT INTO `tv` (`show`, `episode`, `date`, `watched_with`)
            VALUES ($show, $episode, $date, $watched_with)
          """.updateGetKey()
          Response.Redirect(s"/shows/$show")
        }.getOrElse(Response.BadRequest())
      case _ => Response.Unauthorized()
    }

    case ("DELETE", s"/shows/$show/$id", request) => request.auth match {
      case AUTH =>
        sql"DELETE FROM tv WHERE id=$id".update()
        Response.Redirect(s"/shows/$show")
      case _ => Response.Unauthorized()
    }

    case ("GET", s"/people/$name", request) => Response(Templates.details(
      s"TV shows watched with $name",
      Watch.forPerson(name),
      request.params.getOrElse("sort_by", ""),
      nav_back = true
    ))
  }
}
