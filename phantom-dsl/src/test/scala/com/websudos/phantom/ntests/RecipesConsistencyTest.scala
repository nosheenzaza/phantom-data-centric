package com.websudos.phantom.ntests

import org.scalatest.{ Matchers, FlatSpec }
import com.websudos.util.testing._

import scala.concurrent.{ blocking, Future }
import scala.util.{ Success, Failure }
import com.websudos.phantom.dsl._
import com.datastax.driver.core.{ Cluster, Session }
import java.util.UUID
import com.websudos.phantom.sample.Recipe
import com.websudos.phantom.sample.Recipes
import com.websudos.phantom.sample.RecipesDatabaseService

/**
 * @author nosheen
 */
class RecipesConsistencyTest extends FlatSpec with Matchers {
  val recipe = Recipe(
    UUID.randomUUID(),
    "Stuffed Chicken",
    "Yummy Chicken",
    "Nosheen Zaza",
    "Stuff it with rice, pine kernels, peas, and season generously",
    Set("rice", "pine kernels", "peas"))

  val recipe2 = Recipe(
    UUID.randomUUID(),
    "Grilled Chicken",
    "Yummy Chicken",
    "Nosheen Zaza",
    "Grill on wooden skewers",
    Set("chicken", "spice", "more spice"))

//  it should "Insert one recipe in table recipes in my 3-node cluster." in {
    RecipesDatabaseService.insertRecipe(recipe2) onComplete {
      case Success(a: ResultSet) => {
        Recipes.getEntireTable onComplete {
          case Success(a: Seq[Recipe]) => { a.foreach(println); Recipes.deleteAllBlocking() }
          case Failure(t)              => throw t

        }
      }
      case Failure(t) => throw t
    }
//  }
}