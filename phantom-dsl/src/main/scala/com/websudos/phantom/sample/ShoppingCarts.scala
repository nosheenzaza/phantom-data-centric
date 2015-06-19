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
 * All modules needed for the shopping cart service. Main table, mapping tables and app-level consistency.
 * All reads from a shopping cart are weakly consistent, unless done for checkout. 
 * Let's see how I can encode in the clearest way.  
 * This is a example of a wide row schema. Each item in a cart is a new column.
 */
trait ShoppingCartConnector extends SimpleConnector {
  override implicit val keySpace = KeySpace("recipes")
  /*
   * Unless you uncomment the lower part, the client will attempt to connect to localhost
   */
//  val hosts = Seq("192.168.56.3", "192.168.56.4", "192.168.56.5")
//  override implicit lazy val session: Session = 
//    ContactPoints(hosts).keySpace(keySpace.name).session
}

case class ShoppingCartItem(
 user_name: String,
 cart_name: String,
 item_id: UUID, // or int?
 item_name: String,
 description: String,
 price: Float
 )


/**
 * An example of a wide row column family. items in a shopping cart are columns
 * and they key of each item is (user_name,cart_name)
 * 
 * TODO really understand why it is better to isolate by row.
 */
sealed class ShoppingCarts extends CassandraTable[ShoppingCarts, ShoppingCartItem]{
  // Setting partition and clustering keys like this is supposed to 
  // instruct CQL to store in a wide-row manner
  object user_name extends StringColumn(this) with PartitionKey[String]
  object cart_name extends StringColumn(this) with PartitionKey[String]
  object item_id extends UUIDColumn(this)  with PrimaryKey[UUID]
  object item_name extends StringColumn(this)
  object description extends StringColumn(this)
  object price extends FloatColumn(this)

  //TODO test this class to see if we retrieve the dynamic column correctly
  def fromRow(row: Row): ShoppingCartItem = {
    ShoppingCartItem(
      user_name(row),
      cart_name(row),
      item_id(row),
      item_name(row),
      description(row),
      price(row))
  }
}


object ShoppingCarts extends ShoppingCarts with ShoppingCartConnector {
  override lazy val tableName = "shopping_carts"
  val creation = ShoppingCarts.create.ifNotExists.execute()
  Await.ready(creation, 10.seconds)
   
  
  /**
   * Supposedly inserting with the same name and id will not duplicate, 
   * because they are the key and this is how we create the wide
   * row, let's see if this is the case. 
   */
  def insertItemBlocking(item: ShoppingCartItem) {
    Await.ready(insert
    .value(_.user_name, item.user_name)
    .value(_.cart_name, item.cart_name)
    .value(_.item_id, item.item_id)
    .value(_.item_name, item.item_name)
    .value(_.description, item.description)
    .value(_.price, item.price).execute(), 3.seconds)
  }

  def getEntireTable: ScalaFuture[Seq[ShoppingCartItem]] = {
    select.fetchEnumerator() run Iteratee.collect()
  }  
  
  def deleteAllBlocking() {
    val truncateFuture = truncate().execute()
    Await.ready(truncateFuture, 10.seconds)
  }
    
}











