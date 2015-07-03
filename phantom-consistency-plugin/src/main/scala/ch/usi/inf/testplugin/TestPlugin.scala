/*
 * Copyright (c) <2013>, Amanj Sherwany <http://www.amanj.me>
 * All rights reserved.
 * */

package ch.usi.inf.testplugin
import ch.usi.inf.l3.piuma.neve.NeveDSL._
import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent
import scala.tools.nsc.transform.Transform
import scala.tools.nsc.transform.TypingTransformers
import scala.tools.nsc.ast.TreeDSL
import scala.annotation.StaticAnnotation
import scala.collection.mutable.Map
import scala.reflect.internal.Flags._ 


object Commons{
  val tableAnnoName = "com.datastax.driver.mapping.annotations.Table" 
  
}
@plugin(TestPluginComponent2) class TestPlugin {
  val name: String = "phantom-consistency-analysis"
  describe("Assign consistency levels to operations based on data-centric policies")
  val beforeFinder = utilities.PHASE_PATMAT
}

/*
 * TODO it seems like scala does not know what is the annotation target.
 * Thus it was not possible for me to check that the annotations
 * appear on the correct trees! 
 * 
 * I might try with scala annotations later but this is not a priority for now.
 */
@checker("check-annotation-targets") class TestPluginComponent1 {
  import ch.usi.inf.testplugin.Commons._
  
  after(List(PHASE_TYPER))

  def check(unit: CompilationUnit): Unit = {
    
    /*
     * The problem here is that almost all contained elements 
     * appear to have this annotation as well. 
     */
    val tableAnnotatedTrees = unit.body.filter(
        x => getAnnotationInfo(x.symbol, tableAnnoName) != None)

    val classDefOthers=
      tableAnnotatedTrees.groupBy(x => x.isInstanceOf[ClassDef])

    // report errors for nodes having the wrong annotation.
    classDefOthers.getOrElse(false, Nil).foreach(
      tree => unit.error(
        tree.pos, "The element cannot have " + tableAnnoName + " annotation!"))
        
   val annotatedClasses = classDefOthers.getOrElse(true, Nil)
   
//   val apply = unit.body.filter(x => x match { case Apply(_,_) => true ; case _ => false })
//   apply.foreach(println)
   
  }
  
}

@checker("check-annotation-targets") class TestPluginComponent2 {
  import ch.usi.inf.testplugin.Commons._
  
  // FIXME The phase runs after typer only because the previous checker is 
  // not included for now.
  after(List(PHASE_TYPER))

  def check(unit: CompilationUnit): Unit = {

    // Get class term names in compilation unit.
    val tableAnnotatedClasses = unit.body.filter(
      x => {
        getAnnotationInfo(x.symbol, tableAnnoName) != None &&
          x.isInstanceOf[ClassDef]
      })

    val classTermNames = tableAnnotatedClasses.map( x => x.symbol.name.toTermName)

    // Get insert statements TODO add all other modificatin statements other than DDL. 
    val apply = unit.body.filter( 
        tree => classTermNames.exists( 
            x => tree match {
              case Apply(Select(Ident(`x`), TermName("insert")), _) => true
              case _ => false }))
    println("Raw Apply")
    apply.foreach(x => println(x + "RAW: " + showRaw(x)))

//    object traverser extends Traverser {
//      override def traverse(tree: Tree): Option[tree] = {
//        tree match {
//          case cl @ TermName("consistencyLevel_$eq") => Some(cl)
//          case _ => super.traverse(tree)
//        }
//        None
//      }
//    }

  }
  
}

//@treeTransformer("c") class TestPluginComponent2 {
//  // override val runsRightAfter = Some(plgn.utilities.PHASE_INLINER)
//  // override val runsAfter = List[String](plgn.utilities.PHASE_INLINER)
//
//  after(List(PHASE_INLINER))
//  // after(List(plgn.utilities.PHASE_INLINER))
//
//
//  def transform(tree: Tree): Tree = {
//    tree match {
//      case x: ValDef =>
//        println("HERE HERE " + x.symbol.attachments)
//        super.transform(x)
//      case x => super.transform(x)
//    }
//  }
//}



//@treeTransformer("test") class TestPluginComponent {
//  rightAfter(List(PHASE_PATMAT))
//
//
//  def transform(tree: Tree): Tree = {
//    tree match {
//      //      case a @ ValDef(_, _, _, _) => null
//      //      case a @ q"val" if true => null
//      case x: ClassDef =>
//
//        val newName = newTypeName("YESS")
//        val vtree = mkVar(x.symbol, TermName("xmass "), EmptyTree)
//        val strgtr = mkSetterAndGetter(vtree).get
//        val ntmplt = treeCopy.Template(x.impl, x.impl.parents, x.impl.self, vtree :: strgtr._1 :: strgtr._2 :: x.impl.body)
//        val nclazz = treeCopy.ClassDef(x, x.mods, x.name, x.tparams, ntmplt)
//
//        super.transform(typer.typed(nclazz))
//      case x: ValDef if (x.name == newTermName("b")) =>
//        val newName = newTermName("hello")
//        if (canRename(tree, newName)) {
//          println("hello1")
//          super.transform(rename(tree.asInstanceOf[ValDef], newName))
//        } else {
//          super.transform(tree)
//        }
//      case x: ValDef if (x.name == newTermName("c")) =>
//        val newName = newTermName("local")
//        if (canRename(tree, newName)) {
//          println("hello1")
//          super.transform(rename(tree.asInstanceOf[ValDef], newName))
//        } else {
//          super.transform(tree)
//        }
//
//      case x: ValDef if (x.name == newTermName("d")) =>
//        val newName = newTermName("param")
//        if (canRename(tree, newName)) {
//          println("hello1")
//          super.transform(rename(tree.asInstanceOf[ValDef], newName))
//        } else {
//          super.transform(tree)
//        }
//      case x => super.transform(x)
//    }
//  }
//}
//




