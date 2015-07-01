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
@plugin(TestPluginComponent1) class TestPlugin {
  val name: String = "phantom-consistency-analysis"
  describe("Assign consistency levels to operations based on data-centric policies")
  val beforeFinder = utilities.PHASE_PATMAT
}

@checker("check-annotation-targets") class TestPluginComponent1 {
  import ch.usi.inf.testplugin.Commons._
  after(List(PHASE_TYPER))

  def check(unit: CompilationUnit): Unit = {
    val tableAnnotatedTrees = unit.body.filter(
        x => getAnnotationInfo(x, tableAnnoName) != None)
        
   /* 
    * Split to classes having Table annotation and other nodes
    * having it. Only ClassDefs can have this annotation
    */     
    val classDefOthers: (List[Tree], List[Tree]) = 
      tableAnnotatedTrees.foldLeft( ( List[Tree]() , List[Tree]() ) ) ( (c,r) =>
        if(r.isInstanceOf[ClassDef]) (r :: c._1, c._2)  else (c._1, r :: c._2) )
    
    // report errors for nodes having the wrong annotation.
    classDefOthers._2.foreach( tree => unit.error(tree.pos, "Cannot have " + tableAnnoName+ " annotation!"))
        
//    annotatedClasses.foreach(x => println(x + "Has an annotation"))
  }
  
}


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
//// class TestPluginComponent2(plgn: TestPlugin) extends TransformerPluginComponent(plgn) {
//
//@treeTransformer("test2") class TestPluginComponent2 {
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




