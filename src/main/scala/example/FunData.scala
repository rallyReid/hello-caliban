package example

import caliban.schema.Annotations.GQLDescription

object FunData {

  case class Character(
    @GQLDescription("a unique Id for a character")
    id: Int,
    @GQLDescription("the name of the character")
    name: String,
    @GQLDescription("the age of a character, if provided")
    age: Option[Int] = None
  )

  case class CharacterArgs(name: String)

  private val Reid = Character(1, "reid", Some(32))
  private val Lauren = Character(2, "lauren")
  private val Brooklyn = Character(3, "Brooklyn", Some(2))
  private val Levi = Character(4, "Levi", Some(1))

  val CharactersDb = List(Reid, Lauren, Brooklyn, Levi)
}
