package example

import example.FunModels._
import zio.IO

class FunData {
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

  def findByName(name: String): IO[CharacterNotFound, Character] = {
    IO.fromOption(dataSource.find(c => c.name == name)) orElse IO.succeed(CharacterNotFound(name))
  }
}
