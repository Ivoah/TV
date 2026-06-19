package net.ivoah.tv

import java.net.URLEncoder
import java.security.MessageDigest
import scala.util.matching.Regex

extension (str: String) {
  def urlEncoded: String = URLEncoder.encode(str, "UTF-8")
}

extension[T] (seq: Seq[T]) {
  def mkSeq(start: T, sep: T, end: T): Seq[T] = start +: seq.flatMap(elem => Seq(elem, sep)).dropRight(1) :+ start
  def mkSeq(sep: T): Seq[T] = seq.flatMap(elem => Seq(elem, sep)).dropRight(1)
}
