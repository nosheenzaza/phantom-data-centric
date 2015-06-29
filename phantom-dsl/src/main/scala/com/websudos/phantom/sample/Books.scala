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
 
trait BooksConnector extends SimpleConnector {
  override implicit val keySpace = KeySpace("Books")
  /*
   * Unless you uncomment the lower part, the client will attempt to connect to localhost
   */
//  val hosts = Seq("192.168.56.3", "192.168.56.4", "192.168.56.5")
//  override implicit lazy val session: Session = 
//    ContactPoints(hosts).keySpace(keySpace.name).session
}

case class Book(
 isbn: String,
 title: String,
 author: String,
 description: String
)

sealed class Books extends CassandraTable[Books, Book] {
  object isbn extends  StringColumn(this) with PartitionKey[String] {
  override lazy val name = "isbn"
  }
  object title extends StringColumn(this)
  object author extends StringColumn(this)
  object description extends StringColumn(this)

  def fromRow(row: Row): Book = {
    Book(
      isbn(row),        
      title(row),
      author(row),
      description(row)
    )
  }
}

object Books extends Books with BooksConnector {
  override lazy val tableName = "main_Books"

  def insertBook(Book: Book): ScalaFuture[ResultSet] = {
    insert.value(_.isbn, Book.isbn)
      .value(_.title, Book.title)
      .value(_.author, Book.author)
      .value(_.description, Book.description)
      .future()
  }
  
  def getBookById(id: String): ScalaFuture[Option[Book]] = {
    select.where(_.isbn eqs id).one()
  }
  
  def getEntireTable: ScalaFuture[Seq[Book]] = {
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

/**
 * This is a mapping table for Books. It allows querying by title. 
 */
sealed class BooksByTitle extends CassandraTable[BooksByTitle, (String, String )] {

  object title extends StringColumn(this) with PartitionKey[String]
  object isbn extends StringColumn(this)

  def fromRow(row: Row): (String, String) = {
    //TODO remove this tuple2 crap
    Tuple2(title(row), isbn(row))
  }
}

object BooksByTitle extends BooksByTitle with BooksConnector {
  override lazy val tableName = "by_title_Books"


  def insertBook(Book: (String, String)): ScalaFuture[ResultSet] = {
    insert.value(_.title, Book._1).value(_.isbn, Book._2).future()
  }

  def getBookByTitle(title: String): ScalaFuture[Option[(String, String)]] = {
    select.where(_.title eqs title).one()
  }
}

object BooksDatabaseService extends BooksConnector {
    // I could have also used method insertSchema but I think the timeout is very short.
    val creation = Future.join ( 
        Books.create.ifNotExists.execute(), 
        BooksByTitle.create.ifNotExists.execute()
        )
    Await.ready(creation, 10.seconds)
  
  /**
   * TODO this seems like a bad practice to me. As far as I understand,
   * such updates should happen in batches. 
   */
  def insertBook(Book: Book): ScalaFuture[ResultSet] = {
    for {
      insert <- Books.insertBook(Book)
      byTitle <- BooksByTitle.insertBook(Tuple2(Book.title, Book.isbn))
    } yield insert
  }
}



 