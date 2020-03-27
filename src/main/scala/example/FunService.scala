package example

import zio.{Has, Queue, Ref, UIO, URIO, ZLayer}
import FunData.{CharacterArgs, Character}


/**
 * taken from [Caliban](https://github.com/ghostdogpr/caliban/blob/master/examples/src/main/scala/caliban/ExampleService.scala)
 */
object FunService {

  type ExampleService = Has[Service]

  trait Service {
    def getCharacters(name: Option[String]): UIO[List[Character]]
    def getCharacter(name: String): UIO[Option[Character]]
    def deleteCharacter(name: String): UIO[Boolean]
  }

  def getCharacters(
    name: Option[String]
  ): URIO[ExampleService, List[Character]] = URIO.accessM(_.get.getCharacters(name))

  def getCharacter(
    name: String
  ): URIO[ExampleService, Option[Character]] = URIO.accessM(_.get.getCharacter(name))

  def deleteCharacter(name: String): URIO[ExampleService, Boolean] =
    URIO.accessM(_.get.deleteCharacter(name))

  def make(initial: List[Character]): ZLayer[Any, Nothing, ExampleService] = ZLayer.fromEffect {
    for {
      characters  <- Ref.make(initial)
      subscribers <- Ref.make(List.empty[Queue[String]])
    } yield new Service {
      def getCharacters(
        name: Option[String]
      ): UIO[List[Character]] = {
        characters.get.map(_.filter(c => name.forall(c.name == _)))
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
