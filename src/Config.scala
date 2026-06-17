package net.ivoah.tv

import com.typesafe.config.ConfigFactory

object Config {
  private val config = ConfigFactory.load()

  object auth {
    val username: String = config.getString("auth.username")
    val password: String = config.getString("auth.password")
  }

  object database {
    val user: String = config.getString("database.user")
    val password: String = config.getString("database.password")
    val connectionString: String = config.getString("database.connectionString")
  }
}
