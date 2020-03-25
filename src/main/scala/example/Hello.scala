package example
import caliban.wrappers.Wrappers.{maxDepth, printSlowQueries, timeout}
import caliban.wrappers.ApolloTracing.apolloTracing
import example.FunModels.{CharacterNotFound, Character}
import zio.duration._
import zio.IO

object Hello extends App {

  val service = new FunServiceV1(new FunData)

  // Our API
  // schema
  case class Queries(character: CharacterName => IO[CharacterNotFound, Character])
  // resolver
  val queries = Queries(
    args => service.getCharacter(args.name)
    )

  import caliban.GraphQL.graphQL
  import caliban.RootResolver

  val api = graphQL(RootResolver(queries))@@
      maxDepth(50) @@
      timeout(3.second) @@
      printSlowQueries(500.millis) @@
      apolloTracing

  println(api.render)


  /*
  In order to process requests, you need to turn your API into an interpreter,
  which can be done easily by calling .interpreter. An interpreter is a light
  wrapper around the API definition that allows plugging in some middleware and
  possibly modifying the environment and error types (see Middleware for more info).
   */
  val interpreter = api.interpreter

  val query =
    """
      |{
      |   characters {
      |     name
      |   }
      |}
      |""".stripMargin

  for {
    result      <- interpreter.execute(query)
    _           <- zio.console.putStrLn(result.data.toString)
  } yield ()


}
