package example

import caliban.GraphQL.graphQL
import caliban.schema.Annotations.GQLDescription
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers.{maxDepth, printSlowQueries, timeout}
import caliban.{GraphQL, RootResolver}
import example.FunData.{Character, CharacterArgs}
import example.FunService.{getCharacter, getCharacters, ExampleService}
import zio.URIO
import zio.clock.Clock
import zio.console.Console
import zio.duration._

object FunApi extends GenericSchema[ExampleService] {

  case class Queries(
    @GQLDescription("Return all characters")
    characters: URIO[ExampleService, List[Character]],
    @GQLDescription("Return a character by name")
    character: CharacterArgs => URIO[ExampleService, Option[Character]]
  )

  val queries: Queries = Queries(
    getCharacters,
    args => getCharacter(args.name)
  )
  implicit val characterSchema: FunApi.Typeclass[Character] = gen[Character]
  implicit val characterArgsSchema: FunApi.Typeclass[CharacterArgs] = gen[CharacterArgs]

  val funApi: GraphQL[Console with Clock with ExampleService] =
    graphQL(
      RootResolver(queries)
    ) @@ maxDepth(50) @@
    timeout(3.seconds) @@
    printSlowQueries(1500.millis) @@
    apolloTracing

}
