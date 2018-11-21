package fs2test

import cats.effect.IO
import fs2._

object Ex1 {

	val currTime: IO[Long] = IO { System.currentTimeMillis }
	val s: Stream[IO, Long] = Stream.eval(currTime)

	//s.repeat.take(5).compile.toList.unsafeRunSync

	val s1: Stream[IO, Long] = Stream.repeatEval(currTime)

	val s2: Stream[IO, (Int, Long)] = Stream.range(0, 8).zip(Stream.repeatEval(currTime))

  val s3: Stream[IO, Int] = Stream(1,2,3,4).covary[IO]

  val printRange: Stream[IO, Unit] = Stream.range(1,10).evalMap(n => IO(println(n)))  // IntJ fucks up here
}
