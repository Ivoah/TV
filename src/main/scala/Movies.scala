package net.ivoah.movies

import java.sql.{Connection, DriverManager}
import java.nio.file._
import net.ivoah.vial._

import scala.io.Source
import scala.util.Using

class Movies()(implicit val db: Connection) {
  private def person(name: String): String = {
    Templates.person(name, Watch.get(person = Some(name)))
  }
  
  private val rootRouter = Router {
    case ("GET", "/", request) => Response(Templates.root(
      "Noah's movie list",
      Watch.get(),
      request.params.getOrElse("sort_by", "date")
    ))
  }

  private val staticRouter = Router {
    case ("GET", s"/static/$file", _) => Response.forFile(Paths.get(s"static/$file"))
  }
  
  private val moviesRouter = Router {
    case ("GET", s"/movies/$title", request) => Response(Templates.root(
      title,
      Watch.get(title = Some(title)),
      request.params.getOrElse("sort_by", "date")
    ))
  }
  
  private val peopleRouter = Router {
    case ("GET", s"/people/$name", request) => Response(Templates.root(
      s"Movies watched with $name",
      Watch.get(person = Some(name)),
      request.params.getOrElse("sort_by", "date")
    ))
  }
  
  val router: Router = rootRouter ++ staticRouter ++ moviesRouter ++ peopleRouter
}

object Movies {
  def main(args: Array[String]): Unit = {
    import org.rogach.scallop.*

    class Conf(args: Seq[String]) extends ScallopConf(args) {
      val host: ScallopOption[String] = opt[String](default = Some("127.0.0.1"))
      val port: ScallopOption[Int] = opt[Int](default = Some(8080))
      val socket: ScallopOption[String] = opt[String]()
      val verbose: ScallopOption[Boolean] = opt[Boolean](default = Some(false))

      conflicts(socket, List(host, port))
      verify()
    }

    val conf = new Conf(args.toSeq)
    implicit val logger: String => Unit = if (conf.verbose()) println else (msg: String) => ()

    val s"$user:$password" = Using.resource(Source.fromResource("credentials.txt"))(_.getLines().mkString("\n")): @unchecked
    implicit val db: Connection = DriverManager.getConnection("jdbc:mysql://ivoah.net/ivo?autoReconnect=true", user, password)

    val movies = Movies()
    val server = if (conf.socket.isDefined) {
      println(s"Using unix socket: ${conf.socket()}")
      Server(movies.router, socket = conf.socket.toOption)
    } else {
      println(s"Using host/port: ${conf.host()}:${conf.port()}")
      Server(movies.router, conf.host(), conf.port())
    }
    server.serve()
  }
}
