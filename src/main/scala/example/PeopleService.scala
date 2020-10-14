package example

import example.PeopleData.{Person, Relationship}
import zio._
import zio.stream.ZStream

/**
  * taken from [Caliban](https://github.com/ghostdogpr/caliban/blob/master/examples/src/main/scala/caliban/ExampleService.scala)
  */
object PeopleService {

  type PeopleService = Has[Service]

  trait Service {
    def allPeople: UIO[List[Person]]
    def getPersonByName(name: String): UIO[Option[Person]]
    def getPersonById(id: Int): UIO[Option[Person]]
    def findByIds(id: List[Int]): UIO[List[Person]]
    def removePerson(name: String): UIO[Boolean]
    def familyByLastName(lastName: String): UIO[Option[List[Person]]]

    def filterPeople(
      name: Option[String],
      relationShip: Option[Relationship] = None
    ): UIO[List[Person]]
    def deletedEvents: ZStream[Any, Nothing, String]
  }

  def make(initialPeople: List[Person]): ZLayer[Any, Nothing, PeopleService] =
    ZLayer.fromEffect {
      for {
        peopleRepo  <- Ref.make(initialPeople)
        subscribers <- Ref.make(List.empty[Queue[String]])
      } yield new Service {
        def allPeople: UIO[List[Person]] = {
          peopleRepo.get
        }
        def getPersonByName(
          name: String
        ): UIO[Option[Person]] = {
          peopleRepo.get.map(_.find(c => c.name.first == name))
        }

        def getPersonById(id: Int): UIO[Option[Person]] = {
          peopleRepo.get.map(_.find(_.id == id))
        }

        def findByIds(ids: List[Int]): UIO[List[Person]] = {
          peopleRepo.get.map(_.filter(person => ids.contains(person.id)))
        }

        def filterPeople(
          name: Option[String],
          relationship: Option[Relationship] = None
        ): UIO[List[Person]] = {
          (name, relationship) match {
            case (None, None) => allPeople
            case (Some(n), None) => {
              peopleRepo.get
                .map(
                  _.filter(person => person.name.first.contains(n) || person.name.last.contains(n))
                )
            }
            case (None, Some(r)) =>
              peopleRepo.get
                .map(
                  _.filter(person => person.relationship.nonEmpty && person.relationship.get == r)
                )
            case (Some(n), Some(r)) =>
              peopleRepo.get
                .map(
                  _.filter(person => person.name.first.contains(n) || person.name.last.contains(n))
                )
                .map(
                  _.filter(person => person.relationship.nonEmpty && person.relationship.get == r)
                )
          }
        }

        def familyByLastName(lastName: String): UIO[Option[List[Person]]] = {
          peopleRepo.get
            .map(_.filter(_.name.last == lastName))
            .map {
              case filteredList if filteredList.isEmpty => None
              case xs                                   => Some(xs)
            }
        }

        def removePerson(name: String): UIO[Boolean] = {
          peopleRepo
            .modify(list =>
              if (list.exists(_.name.first == name)) (true, list.filterNot(_.name.first == name))
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

        def deletedEvents: ZStream[Any, Nothing, String] = ZStream.unwrap {
          for {
            queue <- Queue.unbounded[String]
            _     <- subscribers.update(queue :: _)
          } yield ZStream.fromQueue(queue).ensuring(queue.shutdown)
        }

      }
    }
}
