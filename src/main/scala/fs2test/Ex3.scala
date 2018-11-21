package fs2test

import cats.effect.{IO, Timer}
import cats.implicits._
import fs2._

import scala.concurrent.duration._

object Ex3 {

  implicit val timer: Timer[IO] = IO.timer(scala.concurrent.ExecutionContext.global)


  val seconds: Stream[IO, Int] = Stream.awakeEvery[IO](1.second).scan(1)((acc, _) => acc+1)

  /*
   * s.take(10).compile.drain.unsafeRunSync
   * will print seconds as numbers starting with 1
   */
  val s: Stream[IO, Unit] = seconds.to(Sink.showLinesStdOut)


  /*
   * How to throttle a byte stream, for instance when doing streaming IO for a bytes input
   * (a file, a socket, etc.).
   */

  val bytes: Stream[IO, Byte] = ???

  /*
   * Throttling the byte stream by 1 chunk per second.
   */
  val throttled: Stream[IO, Byte] = bytes.chunks.zip(seconds).map(_._1).flatMap(c => Stream.chunk(c))
}
