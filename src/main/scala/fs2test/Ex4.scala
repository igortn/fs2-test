/*
 * Load users from DB in a streaming way while suspending effects in IO.
 */
package fs2test

import cats.effect.IO
import fs2._

import scala.concurrent.Future

case class User(id: Long, name: String)

object Ex4 {

  val userIds: Stream[Pure, Long] = ???

  /**
    * Loads a user from DB. Not pure.
    */
  def loadUser(id: Long): Future[User] = ???

  /**
    * Pure. All effects (DB interactions) are suspended in IO.
    */
  def getUsers(ids: Stream[Pure, Long]): IO[List[User]] =
    ids.evalMap[IO, User] { id =>
      IO.fromFuture {
        IO(loadUser(id))
      }
    }.compile.toList   // IntJ does not resolve this without type hints for `evalMap`,
                       // while scalac compiles without them.
}
