package example
import caliban.wrappers.Wrappers.{maxDepth, printSlowQueries, timeout}
import caliban.wrappers.ApolloTracing.apolloTracing
import zio.duration._

object Hello extends App {
  // DATA
    val Reid = Character("Reid", 31)
    val Lauren = Character("Lauren", 28)
    val Brooklyn = Character("Brooklyn", 2)
    val Levi = Character("Levi", 1)
    private val dataSource: List[Character] = List(
      Lauren,
      Reid,
      Brooklyn,
      Levi
    )

  case class Character(name: String, age: Int)

  def getCharacters: List[Character] = dataSource
  def getCharacter(name: String): Option[Character] = dataSource.find(_.name == name)

  // Our API
  // schema
  case class CharacterName(name: String)
  case class Queries(characters: List[Character],
                     character: CharacterName => Option[Character])
  // resolver
  val queries = Queries(
    getCharacters, args => getCharacter(args.name)
    )

  import caliban.GraphQL.graphQL
  import caliban.RootResolver

  val api =
    graphQL(RootResolver(queries))@@
      maxDepth(50) @@
      timeout(3.second) @@
      printSlowQueries(500.millis) @@
      apolloTracing

  println(Console.MAGENTA + Console.BLACK_B + api.render)


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
    result <- interpreter.execute(query)
    _      <- zio.console.putStrLn(result.data.toString)
  } yield ()


}
