package example

object FunModels {
  case class CharacterNotFound(name: String) extends Throwable

  case class Character(name: String, age: Int)

}