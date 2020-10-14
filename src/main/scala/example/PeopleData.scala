package example

import caliban.schema.Annotations.{GQLDescription, GQLInterface}
import example.PeopleData.Relationship.{CHILD, FRIEND, PARENT}

object PeopleData {

  @GQLDescription("usually a human...")
  case class Person(
    @GQLDescription("a unique Id for a family member")
    id: Int,
    @GQLDescription("the name of the person")
    name: Name,
    @GQLDescription("the age of a person, if provided")
    age: Option[Int] = None,
    relationship: Option[Relationship] = None,
    friends: List[Int] = List.empty,
    family: List[Int] = List.empty
  )

  @GQLInterface
  sealed trait Relationship

  object Relationship {
    case object PARENT extends Relationship
    case object CHILD extends Relationship
    case object FRIEND extends Relationship
  }

  case class Name(first: String, last: String)

  @GQLDescription("arguments for querying a person")
  case class PersonArgs(name: String)

  @GQLDescription("define a Person ID")
  case class PersonId(id: Int)

  case class FilterArgs(name: Option[String], relationship: Option[Relationship])

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
