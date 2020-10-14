package example

import caliban.GraphQL.graphQL
import caliban.schema.Annotations.GQLDescription
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers._
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

  // Schema
  // we want to describe all of the queries we will allow.
  case class Query(
    @GQLDescription("Some people in Reid's life...")
    people: URIO[PeopleService, List[Person]],
    @GQLDescription("Return a person by name")
    personsName: NameArgs => URIO[PeopleService, Option[Person]],
    @GQLDescription("Return a person by id")
    personById: IdArg => URIO[PeopleService, Option[Person]],
    @GQLDescription("List people in the same family")
    family: NameArgs => URIO[PeopleService, Option[List[Person]]],
    @GQLDescription("Filters by name or relationship")
    filteredPeople: FilterArgs => URIO[PeopleService, List[Person]]
  )

  // Optional Schemas...
  case class Mutation(removePerson: NameArgs => URIO[PeopleService, Boolean])

  case class Subscription(personDeleted: ZStream[PeopleService, Nothing, String])

  // Case classes for naming arguments
  case class NameArgs(name: String)

  case class IdArg(id: Int)

  case class FilterArgs(name: Option[String], relationship: Option[Relationship])

  // now we will tell our queries how to resolve.
  val queryResolver: Query = Query(
    reidsPeople,
    nameArgs => getPersonByName(nameArgs.name),
    idArg => getPersonById(idArg.id),
    nameArgs => family(nameArgs.name),
    filterArgs => filterPeople(filterArgs.name, filterArgs.relationship)
  )

  val mutationResolver: Mutation = Mutation(nameArgs => removePerson(nameArgs.name))

  val subscriptionsResolver: Subscription = Subscription(deletedEvents)

  /**
    * [[gen]] derives a generic typeclass instance for the type `T`
    */
  implicit val personSchema: PersonApi.Typeclass[Person] = gen[Person]
  implicit val personArgsSchema: PersonApi.Typeclass[NameArgs] = gen[NameArgs]

  // Finally we describe our api.
  val personApi: GraphQL[Console with Clock with PeopleService] =
    graphQL(RootResolver(queryResolver, mutationResolver, subscriptionsResolver)) @@ // Wrappers
    maxFields(200) @@ // query analyzer that limit query fields
    maxDepth(30) @@ // query analyzer that limit query depth
    timeout(3 seconds) @@ // wrapper that fails slow queries
    printSlowQueries(500 millis) @@ // wrapper that logs slow queries
    printErrors @@ // wrapper that logs errors
    apolloTracing // wrapper for https://github.com/apollographql/apollo-tracing


  // domain logic for resolvers...
  def reidsPeople: URIO[PeopleService, List[Person]] = URIO.accessM(_.get.allPeople)

  def getPersonByName(
    name: String
  ): URIO[PeopleService, Option[Person]] = URIO.accessM(_.get.getPersonByName(name))

  def family(lastName: String): URIO[PeopleService, Option[List[Person]]] =
    URIO.accessM(_.get.familyByLastName(lastName))

  def filterPeople(
    name: Option[String] = None,
    relationship: Option[Relationship] = None
  ): URIO[PeopleService, List[Person]] =
    URIO.accessM(_.get.filterPeople(name, relationship))

  def getPersonById(id: Int): URIO[PeopleService, Option[Person]] =
    URIO.accessM(_.get.getPersonById(id))

  def getPeopleByIds(ids: List[Int]): URIO[PeopleService, List[Person]] = URIO.accessM(
    _.get.findByIds(ids)
  )

  def removePerson(name: String): URIO[PeopleService, Boolean] =
    URIO.accessM(_.get.removePerson(name))

  def deletedEvents: ZStream[PeopleService, Nothing, String] =
    ZStream.accessStream(_.get.deletedEvents)

}
