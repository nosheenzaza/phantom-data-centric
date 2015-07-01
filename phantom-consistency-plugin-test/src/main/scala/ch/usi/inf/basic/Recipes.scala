package ch.usi.inf.basic

import java.util.UUID
import com.websudos.phantom.iteratee.Iteratee
import scala.concurrent.{ Future => ScalaFuture }
import com.datastax.driver.core.{ ResultSet, Row }
import com.datastax.driver.mapping.annotations.Table
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
 
trait RecipesConnector extends SimpleConnector {
  override implicit val keySpace = KeySpace("recipes")
  /*
   * Unless you uncomment the lower part, the client will attempt to connect to localhost
   */
//  val hosts = Seq("192.168.56.3", "192.168.56.4", "192.168.56.5")
//  override implicit lazy val session: Session = 
//    ContactPoints(hosts).keySpace(keySpace.name).session
}

case class Recipe(
 id: UUID,
 name: String,
 title: String,
 author: String,
 description: String,
 ingredients: Set[String]
)

@Table(name = "main_recipes", writeConsistency = "ONE")
sealed class Recipes extends CassandraTable[Recipes, Recipe] {
  object id extends  UUIDColumn(this) with PartitionKey[UUID] {
  override lazy val name = "id"
  }
  object name extends StringColumn(this)
  object title extends StringColumn(this)
  object author extends StringColumn(this)
  object description extends StringColumn(this)
  object ingredients extends SetColumn[Recipes, Recipe, String](this)

  def fromRow(row: Row): Recipe = {
    Recipe(
      id(row),
      name(row),
      title(row),
      author(row),
      description(row),
      ingredients(row)
    )
  }
}

object Recipes extends Recipes with RecipesConnector {
  override lazy val tableName = "main_recipes"

  def insertRecipe(recipe: Recipe): ScalaFuture[ResultSet] = {
    insert.consistencyLevel_=(ConsistencyLevel.ONE)
      .value(_.id, recipe.id)
      .value(_.title, recipe.title)
      .value(_.author, recipe.author)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.name, recipe.name)
      .ttl(150.minutes.inSeconds)
      .future()
  }
  def getRecipeById(id: UUID): ScalaFuture[Option[Recipe]] = {
    select.where(_.id eqs id).one()
  }
  
  def getRecipeNameAuthorById(id: UUID): ScalaFuture[Option[(String, String)]] = {
    select(_.name, _.author).where(_.id eqs id).one()
  }
  
  def getEntireTable: ScalaFuture[Seq[Recipe]] = {
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
 * This is a mapping table for recipes. It allows querying by title. 
 */
sealed class RecipesByTitle extends CassandraTable[RecipesByTitle, (String, UUID)] {

  object title extends StringColumn(this) with PartitionKey[String]
  object id extends UUIDColumn(this)

  def fromRow(row: Row): (String, UUID) = {
    //TODO remove this tuple2 crap
    Tuple2(title(row), id(row))
  }
}

object RecipesByTitle extends RecipesByTitle with RecipesConnector {
  override lazy val tableName = "by_title_recipes"


  def insertRecipe(recipe: (String, UUID)): ScalaFuture[ResultSet] = {
    insert.value(_.title, recipe._1).value(_.id, recipe._2).consistencyLevel_=(ConsistencyLevel.ONE).future()
  }

  def getRecipeByTitle(title: String): ScalaFuture[Option[(String, UUID)]] = {
    select.where(_.title eqs title).one()
  }
}

object RecipesDatabaseService extends RecipesConnector {
    // I could have also used method insertSchema but I think the timeout is very short.
    val creation = Future.join ( 
        Recipes.create.ifNotExists.execute(), 
        RecipesByTitle.create.ifNotExists.execute()
        )
    Await.ready(creation, 10.seconds)
  
  /**
   * TODO this seems like a bad practice to me. As far as I understand,
   * such updates should happen in batches. 
   */
  def insertRecipe(recipe: Recipe): ScalaFuture[ResultSet] = {
    for {
      insert <- Recipes.insertRecipe(recipe)
      byTitle <- RecipesByTitle.insertRecipe(Tuple2(recipe.title, recipe.id))
    } yield insert
  }
}



 