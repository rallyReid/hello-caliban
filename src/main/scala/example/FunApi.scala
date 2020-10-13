package example

import caliban.GraphQL.graphQL
import caliban.schema.Annotations.GQLDescription
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers.{maxDepth, printSlowQueries, timeout}
import caliban.{GraphQL, RootResolver}
import example.FunData.{CharacterArgs, FilterArgs, Person, Relationship}
import example.FunService._
import zio.URIO
import zio.clock.Clock
import zio.console.Console
import zio.duration._

object FunApi extends GenericSchema[ExampleService] {

  def people: URIO[ExampleService, List[Person]] = URIO.accessM(_.get.allPeople)

  def getPersonByName(
    name: String
  ): URIO[ExampleService, Option[Person]] = URIO.accessM(_.get.getPersonByName(name))

  def family(lastName: String): URIO[ExampleService, Option[List[Person]]] = URIO.accessM(_.get.familyByLastName(lastName))

  def filterPeople(name: Option[String] = None, relationship: Option[Relationship] = None): URIO[ExampleService, List[Person]] =
    URIO.accessM(_.get.filterPeople(name, relationship))

  def removePerson(name: String): URIO[ExampleService, Boolean] =
    URIO.accessM(_.get.removePerson(name))

  // we want to describe all of the queries we will allow.
  case class Queries(
    @GQLDescription("Must Return all characters")
    people: URIO[ExampleService, List[Person]],
    @GQLDescription("Return a character by name")
    person: CharacterArgs => URIO[ExampleService, Option[Person]],
    @GQLDescription("list People in the same family")
    family: CharacterArgs => URIO[ExampleService, Option[List[Person]]],
    filteredPeople: FilterArgs => URIO[ExampleService, List[Person]]
  )

  case class Mutations(removePerson: CharacterArgs => URIO[ExampleService, Boolean])

  // now we will tell our queries how to resolve.
  val resolver: Queries = Queries(
    people,
    args => getPersonByName(args.name),
    args => family(args.name),
    filterArgs => filterPeople(filterArgs.name, filterArgs.relationship)
  )
  val mutationResolver:Mutations = Mutations(args => removePerson(args.name))
  implicit val characterSchema: FunApi.Typeclass[Person] = gen[Person]
  implicit val characterArgsSchema: FunApi.Typeclass[CharacterArgs] = gen[CharacterArgs]

  // Finally we describe our api.
  val funApi: GraphQL[Console with Clock with ExampleService] =
    graphQL(
      RootResolver(resolver, mutationResolver)
    ) @@ maxDepth(50) @@
    timeout(3.seconds) @@
    printSlowQueries(1500.millis) @@
    apolloTracing

}
