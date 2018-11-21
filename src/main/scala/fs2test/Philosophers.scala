/*
 * `mpilquist` gist with small variations.
 */

package fs2test

import cats.effect.concurrent.Semaphore
import cats.effect.{ExitCode, IO, IOApp}

import cats.syntax.applicative._
import cats.syntax.apply._
import cats.instances.string._

import fs2._

import scala.concurrent.duration._
import scala.util.Random

object PhilosophersModel {
  sealed trait Philosopher
  case object Plato extends Philosopher
  case object Socrates extends Philosopher
  case object Aristotle extends Philosopher
  case object Descartes extends Philosopher
  case object Kant extends Philosopher

  case class Fork(id: Int)

  sealed trait Event
  case class Acquired(p: Philosopher, f: Fork) extends Event
  case class Ate(p: Philosopher) extends Event
  case class Released(p: Philosopher, f: Fork) extends Event
}

object Philosophers extends IOApp {

  import PhilosophersModel._

  def run(args: List[String]): IO[ExitCode] = {

    /*
     * The Dijkstra rule for assigning the order to resources acquisition.
     * (Note the last case.)
     */
    def forksFor(p: Philosopher): (Fork, Fork) = p match {
      case Socrates => (Fork(0), Fork(1))
      case Plato => (Fork(1), Fork(2))
      case Aristotle => (Fork(2), Fork(3))
      case Descartes => (Fork(3), Fork(4))
      case Kant => (Fork(0), Fork(4))
    }

    val forkSemaphores: IO[List[Semaphore[IO]]] = Semaphore[IO](1).replicateA(5)

    def acquire(semaphores: List[Semaphore[IO]], p: Philosopher, f: Fork): IO[Event] =
      semaphores(f.id).acquire *> IO.pure(Acquired(p, f))

    def release(semaphores: List[Semaphore[IO]], p: Philosopher, f: Fork): IO[Event] =
      semaphores(f.id).release *> IO.pure(Released(p, f))

    val randomSleep: Stream[IO, Nothing] = Stream.sleep_((Random.nextInt % 1000).millis)

    def live(semaphores: List[Semaphore[IO]], p: Philosopher): Stream[IO, Event] = {
      val (first, second) = forksFor(p)
      val once = Stream.eval(acquire(semaphores, p, first)) ++
        Stream.eval(acquire(semaphores, p, second)) ++
        randomSleep ++
        Stream.emit(Ate(p)) ++
        Stream.eval(release(semaphores, p, first)) ++
        Stream.eval(release(semaphores, p, second)) ++
        randomSleep
      once.repeat
    }

    val events: Stream[IO, Event] = {

      for {
        semaphores <- Stream.eval(forkSemaphores)
        philosopher <- Stream.emits(List(Socrates, Plato, Aristotle, Descartes, Kant))
      } yield live(semaphores, philosopher)

    }.parJoinUnbounded

    val showEvents = events.map(_.toString).to(Sink.showLinesStdOut[IO, String])

    showEvents.compile.drain.map(_ => ExitCode.Success)
  }
}
