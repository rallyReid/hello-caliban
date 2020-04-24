package example

import example.FunData.Character
import zio._

/**
  * taken from [Caliban](https://github.com/ghostdogpr/caliban/blob/master/examples/src/main/scala/caliban/ExampleService.scala)
  */
object FunService {

  type ExampleService = Has[Service]

  trait Service {
    def getCharacters: UIO[List[Character]]
    def getCharacter(name: String): UIO[Option[Character]]
    def deleteCharacter(name: String): UIO[Boolean]
  }

  def make(initialCharacters: List[Character]): ZLayer[Any, Nothing, ExampleService] =
    ZLayer.fromEffect {
      for {
        characters  <- Ref.make(initialCharacters)
        subscribers <- Ref.make(List.empty[Queue[String]])
      } yield new Service {
        def getCharacters: UIO[List[Character]] = {
          characters.get
        }
        def getCharacter(
          name: String
        ): UIO[Option[Character]] = {
          characters.get.map(_.find(c => c.name == name))
        }

        def deleteCharacter(name: String): UIO[Boolean] = {
          characters
            .modify(list =>
              if (list.exists(_.name == name)) (true, list.filterNot(_.name == name))
              else (false, list)
            )
            .tap(deleted =>
              UIO.when(deleted)(
                subscribers.get.flatMap(
                  // add item to all subscribers
                  UIO.foreach(_)(queue =>
                    queue
                      .offer(name)
                      .onInterrupt(
                        subscribers.update(_.filterNot(_ == queue))
                      ) // if queue was shutdown, remove from subscribers
                  )
                )
              )
            )
        }
      }
    }
}
