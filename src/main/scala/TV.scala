package net.ivoah.tv

import java.sql.Connection
import java.nio.file.*
import net.ivoah.vial.*

class TV()(implicit val db: Connection) {
//  private def person(name: String): String = {
//    Templates.person(name, Watch.get(person = Some(name)))
//  }
  
  private val rootRouter = Router {
    case ("GET", "/", request) => Response(Templates.root(
      Show.getAll(),
      request.params.getOrElse("sort_by", ""),
    ))
  }

  private val staticRouter = Router {
    case ("GET", s"/static/$file", _) => Response.forFile(Paths.get(s"static/$file"))
  }
  
  private val showsRouter = Router {
    case ("GET", s"/shows/$title", request) => Response(Templates.details(
      title,
      Watch.get(show = Some(title)),
      request.params.getOrElse("sort_by", ""),
      nav_back = true
    ))
  }
  
  private val peopleRouter = Router {
    case ("GET", s"/people/$name", request) => Response(Templates.details(
      s"TV shows watched with $name",
      Watch.get(person = Some(name)),
      request.params.getOrElse("sort_by", ""),
      nav_back = true
    ))
  }
  
  val router: Router = rootRouter ++ staticRouter ++ showsRouter ++ peopleRouter
}
