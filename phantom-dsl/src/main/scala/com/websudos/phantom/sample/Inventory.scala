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

/**
 * Inventory model based on description at 
 * http://www.datastax.com/dev/blog/scalable-inventory-example
 * @author Nosheen Zaza
 */

//TODO use a single conector for the entire database
trait InventoryConnector extends SimpleConnector {
  override implicit val keySpace = KeySpace("bookstore")
  /*
   * Unless you uncomment the lower part, the client will attempt to connect to localhost
   */
//  val hosts = Seq("192.168.56.3", "192.168.56.4", "192.168.56.5")
//  override implicit lazy val session: Session = 
//    ContactPoints(hosts).keySpace(keySpace.name).session
}

case class Item(
  id: String,
  store: String,
  item_type: String,
  item_name: String,
  count: Int)

case class LogItem(
  id: String,
  store: String,
  item_type: String,
  item_name: String,
  count: Int,
  time :UUID)


class Inventory extends CassandraTable[Inventory, Item] {
  object id extends StringColumn(this) with PartitionKey[String]
  object store extends StringColumn(this) with PartitionKey[String]
  object item_type extends StringColumn(this) with PartitionKey[String]
  object item_name extends StringColumn(this)
  object count extends IntColumn(this)
  
  def fromRow(row: Row): Item =  {
    Item (
    id(row),
    store(row),
    item_type(row),
    item_name(row),
    count(row))
  }

}


class InventoryLog extends CassandraTable[InventoryLog, LogItem] {
  object id extends StringColumn(this) with PartitionKey[String]
  object store extends StringColumn(this) with PartitionKey[String]
  object item_type extends StringColumn(this)
  object item_name extends StringColumn(this)
  object count extends IntColumn(this)
  object time extends TimeUUIDColumn(this)
  
  def fromRow(row: Row): LogItem =  {
    LogItem (
    id(row),
    store(row),
    item_type(row),
    item_name(row),
    count(row),
    time(row))
  }

}