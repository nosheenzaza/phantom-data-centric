package com.websudos.phantom.ntests

import org.scalatest.{ Matchers, FlatSpec }
import com.websudos.util.testing._

import scala.concurrent.{ blocking, Future }
import scala.util.{ Success, Failure }
import com.websudos.phantom.dsl._
import com.datastax.driver.core.{ Cluster, Session }
import java.util.UUID
import com.websudos.phantom.sample.ShoppingCartItem
import com.websudos.phantom.sample.ShoppingCarts

import com.websudos.phantom.sample.Recipe
import com.websudos.phantom.sample.Recipes
import com.websudos.phantom.sample.RecipesDatabaseService


/**
 * @author nosheen
 */
class ShoppingCartTest extends FlatSpec with Matchers {
  val cartItem1 = ShoppingCartItem(
    "nosheen", "movies",
    UUID.randomUUID(),
    "The Great Gatsby",
    "A movie about Mr Gatsby",
    12.0F)

  val cartItem2 = ShoppingCartItem(
    "nosheen", "movies",
    UUID.randomUUID(),
    "Bad Teacher",
    "Funny and funny",
    13.0F)

  ShoppingCarts.insertItemBlocking(cartItem1)
  ShoppingCarts.insertItemBlocking(cartItem2)

  ShoppingCarts.getEntireTable onComplete {
    case Success(a: Seq[ShoppingCartItem]) => { a.foreach(println);}
    case Failure(t)                        => throw t
  }


}
