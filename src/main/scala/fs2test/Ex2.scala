package fs2test

/*
 * Read a file.
 */

import java.nio.file.Paths
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO, Sync}
import fs2._

import scala.concurrent.ExecutionContext


object Ex2 {

  /*
   * FS2 manages io resources (files, sockets, etc.) automatically.
   */

  def readFile[F[_] : Sync : ContextShift](pathToFile: String, chunkSize: Int)(
    implicit ec: ExecutionContext): Stream[F, Byte] = {
    val path = Paths.get(pathToFile)
    io.file.readAll[F](path, ec, chunkSize)
  }

  def run(): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val ctxShift: ContextShift[IO] = IO.contextShift(ec)

    val src: Stream[IO, Byte] = readFile[IO]("/Users/igortn/Temp/a.txt", 8)

    val bytes: IO[Vector[Byte]] = src.compile.toVector
    val res1: Vector[Byte] = bytes.unsafeRunSync

    val pipe: Pipe[IO, Byte, String] = text.utf8Decode[IO]
    val res2: Vector[String] = pipe(src).compile.toVector.unsafeRunSync

    /*
     * Same as res2 but more idiomatic, `through` is essentially a thrush operator with respect to `pipe`.
     * It is convenient for pushing a stream through multiple pipelines.
     */
    val res3: Vector[String] = src.through(pipe).compile.toVector.unsafeRunSync

    /*
     * The strings in the result above represent byte chunks of the size 8 (as we asked).
     * To convert this to the stream elements of which represent lines of text, we used
     * another pipeline combinator which is called `lines`.
     */
    val res4: Vector[String] = src
      .through(text.utf8Decode)
      .through(text.lines)
      .compile.toVector.unsafeRunSync

    val res5: Vector[String] = src
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(_.startsWith("A"))
      .map(_.toUpperCase)
      .compile.toVector.unsafeRunSync
  }

  /*
   * It's better not to run a blocking operation in the default global execution context
   * which is backed by the fork-join pool.
   *
   * implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
   * implicit val ctxShift: ContextShift[IO] = IO.contextShift(ec)
   */

  /**
    * Synchronously reads a file into a stream of bytes, transforms the stream and writes it
    * into another file.
    */
  def readTransformWrite[F[_] : Sync : ContextShift](pathFrom: String, pathTo: String)(
    implicit blockingCtx: ExecutionContext): F[Unit] = {

    val from = Paths.get(pathFrom)
    val to = Paths.get(pathTo)

    val src: Stream[F, Byte] = io.file.readAll[F](from, blockingCtx, 8)
    val writeSink: Sink[F, Byte] = io.file.writeAll[F](to, blockingCtx)

    val program: Stream[F, Unit] = src
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(_.nonEmpty)
      .map(_.toUpperCase)
      .through(text.utf8Encode)
      .through(writeSink)

    program.compile.drain
  }
}
