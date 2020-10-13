package example

import caliban.schema.Annotations.GQLDescription
import example.PeopleData.Relationship.{CHILD, PARENT}

object PeopleData {

  case class Person(
    @GQLDescription("a unique Id for a family member")
    id: Int,
    @GQLDescription("the name of the character")
    name: Name,
    @GQLDescription("the age of a character, if provided")
    age: Option[Int] = None,
    relationShip: Option[Relationship] = None
  )

 sealed trait Relationship

  object Relationship {
    case object PARENT extends Relationship
    case object CHILD extends Relationship
    case object FRIEND extends Relationship
  }

  case class Name(first: String, last: String)

  case class CharacterArgs(name: String)

  case class FilterArgs(name: Option[String], relationship: Option[Relationship])

  case class Family(people: List[Person])

  private val Reid = Person(1, Name("reid", "mewborne"), Some(32), Some(PARENT))
  private val Lauren = Person(2, Name("lauren", "mewborne"))
  private val Brooklyn = Person(3, Name("Brooklyn", "mewborne"), Some(2), Some(CHILD))
  private val Levi = Person(4, Name("Levi", "mewborne"), Some(1))
  private val Matt = Person(38, Name("Matt", "Wagner"), Some(38))
  private val Bennett = Person(36, Name("Richard", "Bennett"), Some(34))

  val personDb = List(Reid, Lauren, Brooklyn, Levi, Matt, Bennett)
}
