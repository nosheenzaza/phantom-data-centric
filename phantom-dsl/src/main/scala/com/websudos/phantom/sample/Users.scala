package com.websudos.phantom.sample

import java.util.UUID
import com.websudos.phantom.iteratee.Iteratee
import scala.concurrent.{ Future => ScalaFuture }
import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.dsl._
import com.twitter.conversions.time._
import com.twitter.util.{Await, Future}
import com.websudos.phantom.connectors.KeySpace
import com.websudos.phantom.connectors.SimpleConnector
import com.websudos.phantom.connectors.ContactPoints
import scala.concurrent.{Future => ScalaFuture}


/*
 * The purpose of this file is creating a "denormalized" schema. I want to 
 * show later another version of this table that shows how the fields 
 * are actually mapped, and the compiler will take care of 
 * generating library calls that will achieve the mapping.   
 */
 
trait UsersConnector extends SimpleConnector {
  override implicit val keySpace = KeySpace("bookstore")
  /*
   * Unless you uncomment the lower part, the client will attempt to connect to localhost
   */
//  val hosts = Seq("192.168.56.3", "192.168.56.4", "192.168.56.5")
//  override implicit lazy val session: Session = 
//    ContactPoints(hosts).keySpace(keySpace.name).session
}

case class User(
 username: String,
 firstname: String, 
 lastname: String,
 shopping_carts: Set[String]
)

sealed class Users extends CassandraTable[Users, User] {
  object id extends  UUIDColumn(this) with PartitionKey[UUID] {
  override lazy val name = "id"
  }
  object username extends StringColumn(this)
  object firstname extends StringColumn(this)
  object lastname extends StringColumn(this)
  object shopping_carts extends SetColumn[Users, User, String](this)

  def fromRow(row: Row): User = {
    User(
      username(row),
      firstname(row),
      lastname(row),
      shopping_carts(row)
    )
  }
}

object Users extends Users with UsersConnector {
  override lazy val tableName = "main_Users"

  def insertUser(User: User): ScalaFuture[ResultSet] = {
    insert.value(_.username, User.username)
      .value(_.firstname, User.firstname)
      .value(_.lastname, User.lastname)
      .value(_.shopping_carts, User.shopping_carts)
      .future()
  }
  def getUserById(id: UUID): ScalaFuture[Option[User]] = {
    select.where(_.id eqs id).one()
  }
  
  def getEntireTable: ScalaFuture[Seq[User]] = {
    select.fetchEnumerator() run Iteratee.collect()
  }
  
  /**
   * TODO this does not seem to be working within tests. Figure out why
   */
  def deleteAllBlocking() {
    val truncateFuture = truncate().execute()
    Await.ready(truncateFuture, 10.seconds)
  }
}

object UsersDatabaseService extends UsersConnector {
    // I could have also used method insertSchema but I think the timeout is very short.
    val creation =  Users.create.ifNotExists.execute()
    Await.ready(creation, 10.seconds)
  
  /**
   * TODO this seems like a bad practice to me. As far as I understand,
   * such updates should happen in batches. 
   */
  def insertUser(User: User): ScalaFuture[ResultSet] = {
    for {
      insert <- Users.insertUser(User)
    } yield insert
  }
}



 