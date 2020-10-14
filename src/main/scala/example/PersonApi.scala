package example

import caliban.GraphQL.graphQL
import caliban.schema.Annotations.GQLDescription
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers.{maxDepth, maxFields, printErrors, printSlowQueries, timeout}
import caliban.{GraphQL, RootResolver}
import example.PeopleData._
import example.PeopleService._
import zio.URIO
import zio.clock.Clock
import zio.console.Console
import zio.duration._
import zio.stream.ZStream

import scala.language.postfixOps

object PersonApi extends GenericSchema[PeopleService] {

  def people: URIO[PeopleService, List[Person]] = URIO.accessM(_.get.allPeople)

  def getPersonByName(
    name: String
  ): URIO[PeopleService, Option[Person]] = URIO.accessM(_.get.getPersonByName(name))

  def family(lastName: String): URIO[PeopleService, Option[List[Person]]] = URIO.accessM(_.get.familyByLastName(lastName))

  def filterPeople(name: Option[String] = None, relationship: Option[Relationship] = None): URIO[PeopleService, List[Person]] =
    URIO.accessM(_.get.filterPeople(name, relationship))

  def getPersonById(id: Int): URIO[PeopleService, Option[Person]] = URIO.accessM(_.get.getPersonById(id))

  def getPeopleByIds(ids: List[Int]): URIO[PeopleService, List[Person]] = URIO.accessM(
    _.get.findByIds(ids)
  )

  def removePerson(name: String): URIO[PeopleService, Boolean] =
    URIO.accessM(_.get.removePerson(name))

  def deletedEvents: ZStream[PeopleService, Nothing, String] =
    ZStream.accessStream(_.get.deletedEvents)

  // we want to describe all of the queries we will allow.
  case class Queries(
    @GQLDescription("Some people in Reid's life...")
    people: URIO[PeopleService, List[Person]],
    @GQLDescription("Return a person by name")
    person: PersonArgs => URIO[PeopleService, Option[Person]],
    @GQLDescription("Return a person by id")
    personById: PersonId => URIO[PeopleService, Option[Person]],
    @GQLDescription("list people in the same family")
    family: PersonArgs => URIO[PeopleService, Option[List[Person]]],
    filteredPeople: FilterArgs => URIO[PeopleService, List[Person]]
  )

  case class Mutations(
    removePerson: PersonArgs => URIO[PeopleService, Boolean]
  )

  case class Subscriptions(personDeleted: ZStream[PeopleService, Nothing, String])

  // now we will tell our queries how to resolve.
  val resolver: Queries = Queries(
    people,
    args => getPersonByName(args.name),
    args => getPersonById(args.id),
    args => family(args.name),
    filterArgs => filterPeople(filterArgs.name, filterArgs.relationship)
  )
  val mutationResolver:Mutations = Mutations(
    args => removePerson(args.name)
  )

  val subscriptionsResolver: Subscriptions = Subscriptions(deletedEvents)
  implicit val personSchema: PersonApi.Typeclass[Person] = gen[Person]
  implicit val personArgsSchema: PersonApi.Typeclass[PersonArgs] = gen[PersonArgs]

  // Finally we describe our api.
  val funApi: GraphQL[Console with Clock with PeopleService] =
    graphQL(
      RootResolver(resolver, mutationResolver, subscriptionsResolver)
    ) @@
      maxFields(200) @@               // query analyzer that limit query fields
      maxDepth(30) @@                 // query analyzer that limit query depth
      timeout(3 seconds) @@           // wrapper that fails slow queries
      printSlowQueries(500 millis) @@ // wrapper that logs slow queries
      printErrors @@                  // wrapper that logs errors
      apolloTracing                   // wrapper for https://github.com/apollographql/apollo-tracing

}
