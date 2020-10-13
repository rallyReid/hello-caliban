package example

import caliban.GraphQL.graphQL
import caliban.schema.Annotations.GQLDescription
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers.{maxDepth, printSlowQueries, timeout}
import caliban.{GraphQL, RootResolver}
import example.PeopleData.{CharacterArgs, FilterArgs, Person, Relationship}
import example.PeopleService._
import zio.URIO
import zio.clock.Clock
import zio.console.Console
import zio.duration._
import zio.stream.ZStream

object PersonApi extends GenericSchema[PeopleService] {

  def people: URIO[PeopleService, List[Person]] = URIO.accessM(_.get.allPeople)

  def getPersonByName(
    name: String
  ): URIO[PeopleService, Option[Person]] = URIO.accessM(_.get.getPersonByName(name))

  def family(lastName: String): URIO[PeopleService, Option[List[Person]]] = URIO.accessM(_.get.familyByLastName(lastName))

  def filterPeople(name: Option[String] = None, relationship: Option[Relationship] = None): URIO[PeopleService, List[Person]] =
    URIO.accessM(_.get.filterPeople(name, relationship))

  def removePerson(name: String): URIO[PeopleService, Boolean] =
    URIO.accessM(_.get.removePerson(name))

//  def addPerson(p: Person): URIO[PeopleService, Boolean] = {
//    URIO.accessM(_.get.addPerson(p))
//  }

  def deletedEvents: ZStream[PeopleService, Nothing, String] =
    ZStream.accessStream(_.get.deletedEvents)

  // we want to describe all of the queries we will allow.
  case class Queries(
    @GQLDescription("Must Return all characters")
    people: URIO[PeopleService, List[Person]],
    @GQLDescription("Return a character by name")
    person: CharacterArgs => URIO[PeopleService, Option[Person]],
    @GQLDescription("list People in the same family")
    family: CharacterArgs => URIO[PeopleService, Option[List[Person]]],
    filteredPeople: FilterArgs => URIO[PeopleService, List[Person]]
  )

  case class Mutations(
    removePerson: CharacterArgs => URIO[PeopleService, Boolean]
//    addPerson: Person => URIO[PeopleService, Boolean]
  )

  case class Subscriptions(characterDeleted: ZStream[PeopleService, Nothing, String])

  // now we will tell our queries how to resolve.
  val resolver: Queries = Queries(
    people,
    args => getPersonByName(args.name),
    args => family(args.name),
    filterArgs => filterPeople(filterArgs.name, filterArgs.relationship)
  )
  val mutationResolver:Mutations = Mutations(
    args => removePerson(args.name)
//    p => addPerson(p)
  )

  val subscriptionsResolver = Subscriptions(deletedEvents)
  implicit val characterSchema: PersonApi.Typeclass[Person] = gen[Person]
  implicit val characterArgsSchema: PersonApi.Typeclass[CharacterArgs] = gen[CharacterArgs]

  // Finally we describe our api.
  val funApi: GraphQL[Console with Clock with PeopleService] =
    graphQL(
      RootResolver(resolver, mutationResolver, subscriptionsResolver)
    ) @@ maxDepth(50) @@
    timeout(3.seconds) @@
    printSlowQueries(1500.millis) @@
    apolloTracing

}
