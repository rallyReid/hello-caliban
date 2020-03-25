package example

import example.FunModels.{CharacterNotFound, Character}
import zio.{IO, UIO}

trait FunService {
  def getCharacter(name: String): IO[CharacterNotFound, Character]                          // GET request
//  def randomCharacterPicture: UIO[String]                                        // GET request
//  def addCharacter(pug: Character): UIO[Unit]                                          // POST request
//  def editCharacterPicture(name: String, pictureUrl: String): IO[CharacterNotFound, Unit] // PUT request
}

class FunServiceV1(data: FunData) extends FunService {
  def getCharacter(name: String): IO[CharacterNotFound, Character] = data.findByName(name)

//  def randomCharacterPicture: UIO[String] = ???
//
//  def addCharacter(pug: Character): UIO[Unit] = ???
//
//  def editCharacterPicture(name: String, pictureUrl: String): IO[CharacterNotFound, Unit] = ???
}
