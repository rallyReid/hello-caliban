package example

import caliban.GraphQL.graphQL
import caliban.schema.Annotations.GQLDescription
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers.{maxDepth, printSlowQueries, timeout}
import caliban.{GraphQL, RootResolver}
import example.FunData.{Character, CharacterArgs}
import example.FunService._
import zio.URIO
import zio.clock.Clock
import zio.console.Console
import zio.duration._

object FunApi extends GenericSchema[ExampleService] {

  def getCharacters: URIO[ExampleService, List[Character]] = URIO.accessM(_.get.getCharacters)

  def getCharacter(
    name: String
  ): URIO[ExampleService, Option[Character]] = URIO.accessM(_.get.getCharacter(name))

  def deleteCharacter(name: String): URIO[ExampleService, Boolean] =
    URIO.accessM(_.get.deleteCharacter(name))

  // we want to describe all of the queries we will allow.
  case class Queries(
    @GQLDescription("Must Return all characters")
    characters: URIO[ExampleService, List[Character]],
    @GQLDescription("Return a character by name")
    character: CharacterArgs => URIO[ExampleService, Option[Character]]
  )

  // now we will tell our queries how to resolve.
  val resolver: Queries = Queries(
    getCharacters,
    args => getCharacter(args.name)
  )
  implicit val characterSchema: FunApi.Typeclass[Character] = gen[Character]
  implicit val characterArgsSchema: FunApi.Typeclass[CharacterArgs] = gen[CharacterArgs]

  // Finally we describe our api.
  val funApi: GraphQL[Console with Clock with ExampleService] =
    graphQL(
      RootResolver(resolver)
    ) @@ maxDepth(50) @@
    timeout(3.seconds) @@
    printSlowQueries(1500.millis) @@
    apolloTracing

}
