import fs2._

def mytake[F[_], A](n: Int): Pipe[F, A, A] =
  in => {
    ???
  }

List(1,2,3,4).take(2)