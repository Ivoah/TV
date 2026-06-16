package net.ivoah.tv

import java.net.URLEncoder
import java.security.MessageDigest
import scala.util.matching.Regex

object Extensions {
  extension (str: String) {
    def replaceAll(regex: String, replacer: Regex.Match => String): String = {
      regex.r.replaceAllIn(str, replacer)
    }

    def redacted: String =
      str
        .replaceAll(raw"[^>\s](?![^<]*>)(?![^<]*>)", "█")
        .replaceAll(raw"""<img src=".+?"(.*?)>""", """<img src="/static/redacted.jpg"$1>""")
    def urlEncoded: String = URLEncoder.encode(str, "UTF-8")
    def cssEscape: String = str.htmlClass.replaceAll("([!\\\"#$%&'()*+,\\-./:;<=>?@\\[\\\\\\]^`{|}~])", "\\\\$1")
    def htmlClass: String = str.replaceAll(" ", "")

    def rgbHash: String = {
      val md5 = MessageDigest.getInstance("md5").digest(str.getBytes)
      s"rgb(${md5.slice(0, 3).map(_ & 0xff).mkString(", ")})"
    }
  }
}
