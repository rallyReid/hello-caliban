package example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import caliban.interop.circe.AkkaHttpCirceAdapter
import example.FunData.Character
import example.FunService._
import zio.{Runtime, ZEnv, ZLayer}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Hello extends App with AkkaHttpCirceAdapter {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val runtime: Runtime[ZEnv] = Runtime.default

  private val CharactersDb = List(Character("reid", 32), Character("lauren", 28))

  val service: ZLayer[Any, Nothing, ExampleService] = make(CharactersDb)

  val interpreter = runtime.unsafeRun(
    FunService
      .make(CharactersDb)
      .memoize
      .use(layer => FunApi.funApi.interpreter.map(_.provideCustomLayer(layer)))
  )

  val route =
    path("api" / "graphql") {
      adapter.makeHttpService(interpreter)
    } ~ path("graphiql") {
      getFromResource("graphiql.html")
    }

  val query: String =
    """
      |{
      |   character(name: "reid") {
      |     name
      |   }
      |}
      |""".stripMargin

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8088)
  println(s"Server online at http://localhost:8088/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
