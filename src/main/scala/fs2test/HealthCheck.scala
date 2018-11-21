/*
 *
 * Declarative control flow with FS2.
 *
 * - Create simple IO actions (words)
 * - Compose them into a Stream (sentence)
 *
 */

package fs2test

import cats.effect.{IO, Timer}
import fs2._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class HealthCheckMessage(check: Boolean) extends Message

object HealthCheck {

  val ec: ExecutionContext = ???

  implicit val timer: Timer[IO] = IO.timer(ec)

  def ping: IO[Boolean] = ???

  def healthCheck: Stream[IO, Message] = {
    val retryCheck =
      Stream.retry(ping, 1.second, d => d + 1.second, maxAttempts = 5)

    val check = retryCheck
      .map(HealthCheckMessage)
      .handleErrorWith(_ => ???)

    (check ++ Stream.sleep_(1.hour)).repeat
  }
}