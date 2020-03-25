package example
import caliban.{CalibanError, ResponseValue}
import caliban.wrappers.Wrappers.{maxDepth, printSlowQueries, timeout}
import caliban.wrappers.ApolloTracing.apolloTracing
import zio.clock.Clock
import zio.{URIO, ZIO}
import zio.console._
import zio.duration._

object Hello extends zio.App {


  case class Character(name: String, age: Int)

  private val CharactersDb = List(Character("reid", 32))

  def getCharacters: List[Character] = CharactersDb
  def getCharacter(name: String): Option[Character] = CharactersDb.find(c => c.name == name)

  // Our API
  // schema
  case class CharacterName(name: String)
  case class Queries(characters: List[Character],
                     character: CharacterName => Option[Character])
  // resolver
  val queries = Queries(getCharacters, args => getCharacter(args.name))

  import caliban.GraphQL.graphQL
  import caliban.RootResolver

  val api = graphQL(RootResolver(queries))@@
      maxDepth(50) @@
      timeout(3.second) @@
      printSlowQueries(500.millis) @@
      apolloTracing

//  println(api.render)


  /*
  In order to process requests, you need to turn your API into an interpreter,
  which can be done easily by calling .interpreter. An interpreter is a light
  wrapper around the API definition that allows plugging in some middleware and
  possibly modifying the environment and error types (see Middleware for more info).
   */

  case class GraphQLResponse[+E](data: ResponseValue, errors: List[E])

  val query =
    """
      |{
      |   characters {
      |     name
      |   }
      |}
      |""".stripMargin

  lazy val myAppLogic: ZIO[Console with Clock, CalibanError.ValidationError, Unit] = for {
    _           <- putStrLn(api.render)
    interpreter <- api.interpreter
    result      <- interpreter.execute(query)
    _           <- putStrLn(result.data.toString)
  } yield ()

  def run(args: List[String]): URIO[Console with Clock, Int] = {
    myAppLogic.fold(_ => 1, _ => 0)
  }
}
