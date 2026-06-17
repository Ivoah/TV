package net.ivoah.tv

import java.sql.*
import java.time.LocalDate
import scala.util.Using

type QueryParam = Int | Double | String | Boolean | scala.Array[Byte] | LocalDate | DbEnum

case class Connector(url: String, user: String, password: String) {
  private var _connection: Connection = DriverManager.getConnection(url, user, password)

  def connection: Connection = {
    if (!_connection.isValid(5)) _connection = DriverManager.getConnection(url, user, password)
    _connection
  }
}

trait DbEnum(val dbType: String) {
  override def toString: String
}

class RawString(str: String) {
  override def toString: String = str
}
extension (str: String) {
  def raw: RawString = RawString(str)
}

case class Query(sql: String, params: Seq[QueryParam]) {
  private def buildStatement(using db: Connector): PreparedStatement = {
    val stmt = db.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
    
    for ((param, i) <- params.zipWithIndex) param match {
      case int: Int                 => stmt.setInt(i + 1, int)
      case dbl: Double              => stmt.setDouble(i + 1, dbl)
      case str: String              => stmt.setString(i + 1, str)
      case bool: Boolean            => stmt.setBoolean(i + 1, bool)
      case date: LocalDate          => stmt.setDate(i + 1, java.sql.Date.valueOf(date))
      case _enum: DbEnum            => stmt.setString(i + 1, _enum.toString)
      case bytes: scala.Array[Byte] => stmt.setBytes(i + 1, bytes)
    }
    
    stmt
  }

  def query[T](using db: Connector)(fn: ResultSet => T): Seq[T] = {
    Using.resource(buildStatement) { stmt =>
      Iterator.unfold(stmt.executeQuery()) { results =>
        if (results.next()) Some(fn(results), results)
        else None
      }.toSeq
    }
  }

  def execute(using db: Connector)(): Unit = Using.resource(buildStatement)(_.execute())

  def update(using db: Connector)(): Int = Using.resource(buildStatement)(_.executeUpdate())
  def updateGetKey(using db: Connector)(): Int = Using.resource(buildStatement) { stmt =>
    stmt.executeUpdate()
    val results = stmt.getGeneratedKeys
    results.next()
    results.getInt(1)
  }

  def +(o: Query): Query = Query(sql + o.sql, params ++ o.params)
}

extension (sc: StringContext) {
  def sql(params: (QueryParam | RawString | Query)*): Query = {
    Query(
      sc.parts.zipAll(params, "", "".raw).map {
        case (part, param) if param == null => s"${part}NULL"
        case (part, _enum: DbEnum)          => s"$part?::${_enum.dbType}"
        case (part, _: QueryParam)          => s"$part?"
        case (part, param: RawString)        => s"$part$param"
        case (part, q: Query)               => s"$part${q.sql}"
      }.mkString,
      params.flatMap {
        case p: QueryParam if p != null => Some(p)
        case q: Query => q.params
        case _ => None
      }
    )
  }
}

extension (rs: ResultSet) {
  def getIntOption(col: String): Option[Int] = {
    val v = rs.getInt(col)
    if (rs.wasNull) None else Some(v)
  }

  def getDoubleOption(col: String): Option[Double] = {
    val v = rs.getDouble(col)
    if (rs.wasNull) None else Some(v)
  }

  def getStringOption(col: String): Option[String] = {
    val v = rs.getString(col)
    if (rs.wasNull) None else Some(v)
  }
}
