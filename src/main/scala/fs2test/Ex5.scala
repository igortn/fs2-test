package fs2test

/*
 * From the talk of Fabio Labella: https://www.youtube.com/watch?v=YSN__0VEsaw .
 * Process different streams concurrently.
 */

import cats.effect._
import fs2._

object Ex5 {

  implicit val cio: Concurrent[IO] = ???

  def healthCheck: Stream[IO, Message] = ???
  def kafkaMessages: Stream[IO, Message] = ???
  def fileConverter: Stream[IO, Unit] = ???

  def all: Stream[IO, Message] = Stream(
    healthCheck,
    fileConverter.drain,
    kafkaMessages
  ).covary[IO].parJoinUnbounded

}
