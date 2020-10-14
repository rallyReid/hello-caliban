package example

import caliban.schema.Annotations.{GQLDeprecated, GQLDescription}
import example.PeopleData.Relationship.{CHILD, FRIEND, PARENT}

object PeopleData {
  @GQLDescription("usually a human...")
  case class Person(
    id: Int,
    @GQLDescription("a name for being social.")
    name: Name,
    age: Option[Int] = None,
    relationship: Option[Relationship] = None,
    friends: List[Int] = List.empty,
    family: List[Int] = List.empty
  )

  case class Name(first: String, last: String)

  sealed trait Relationship

  object Relationship {
    case object PARENT extends Relationship
    case object CHILD extends Relationship
    case object FRIEND extends Relationship
  }

  private val Reid = Person(
    id = 1,
    name = Name("Reid", "Mewborne"),
    age = Some(32),
    relationship = Some(PARENT),
    friends = List(5, 6),
    family = List(2, 3, 4)
  )

  private val Lauren = Person(
    id = 2,
    name = Name("Lauren", "Mewborne"),
    age = None,
    relationship = Some(PARENT),
    family = List(1, 3, 4)
  )

  private val Brooklyn = Person(
    id = 3,
    name = Name("Brooklyn", "Mewborne"),
    age = Some(3),
    relationship = Some(CHILD),
    family = List(1, 2, 4)
  )

  private val Levi = Person(
    id = 4,
    name = Name("Levi", "Mewborne"),
    age = Some(1),
    relationship = Some(CHILD),
    family = List(1, 2, 3)
  )

  private val Matt = Person(
    id = 5,
    name = Name("Matt", "W"),
    age = None,
    relationship = Some(FRIEND),
    friends = List(1, 2)
  )

  private val Bennett = Person(
    id = 6,
    name = Name("Richard", "Bennett"),
    age = None,
    relationship = Some(FRIEND),
    friends = List(1, 6)
  )

  val personDb = List(Reid, Lauren, Brooklyn, Levi, Matt, Bennett)
}
