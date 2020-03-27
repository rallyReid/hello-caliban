package example

import caliban.{GraphQL, RootResolver}
import caliban.GraphQL.graphQL
import caliban.schema.Annotations.GQLDeprecated
import caliban.schema.GenericSchema
import caliban.wrappers.Wrappers.{maxDepth, printSlowQueries, timeout}
import example.FunService.{getCharacter, ExampleService}
import zio.clock.Clock
import zio.console.Console
import zio.duration._
import caliban.wrappers.ApolloTracing.apolloTracing
import FunData.{Character, CharacterArgs}
import zio.URIO

object FunApi extends GenericSchema[ExampleService] {

  case class Queries(
    character: CharacterArgs => URIO[ExampleService, Option[Character]]
  )

  val queries = Queries(args => getCharacter(args.name))
  implicit val characterSchema = gen[Character]
  implicit val characterArgsSchema  = gen[CharacterArgs]

  val funApi: GraphQL[Console with Clock with ExampleService] =
    graphQL(
      RootResolver(queries)
    ) @@ maxDepth(50) @@
    timeout(3.seconds) @@
    printSlowQueries(500.millis) @@
    apolloTracing

}
