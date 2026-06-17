package net.ivoah.tv

import net.ivoah.vial.*
import org.rogach.scallop.*

import scala.io.Source
import scala.util.Using

@main
def main(args: String*): Unit = {
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

  implicit val db: Connector = Connector(Config.database.connectionString, Config.database.user, Config.database.password)

  val tv = TV()
  val server = if (conf.socket.isDefined) {
    println(s"Using unix socket: ${conf.socket()}")
    Server(tv.router, conf.socket())
  } else {
    println(s"Using host/port: ${conf.host()}:${conf.port()}")
    Server(tv.router, (conf.host(), conf.port()))
  }
  server.serve()
}
