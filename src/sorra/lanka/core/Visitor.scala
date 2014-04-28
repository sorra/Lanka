package sorra.lanka.core

import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.ASTNode

class Visitor(nodeVisit: PartialFunction[ASTNode, Boolean]) extends ASTVisitor {
  
  def start(scope: ASTNode) = {
    scope.accept(this)
    this
  }
  
  override def preVisit2(node: ASTNode): Boolean = {
    preVisit(node)
    nodeVisit(node)
  }
}

object Visitor {
  /**
   * yourNodeVisit matches some node types, returns true:CONTINUE or false:STOP.
   * Other node types are skipped.
   */
  def stoppable(yourNodeVisit: PartialFunction[ASTNode, Boolean]) = {
    new Visitor(yourNodeVisit.orElse{case _ => true})
  }
  
  /**
   * yourNodeVisit matches some node types, goes through the AST.
   * Other node types are skipped.
   */
  def through(yourNodeVisit: PartialFunction[ASTNode, Unit]) = {
    val nodeVisit: PartialFunction[ASTNode, Unit] = yourNodeVisit.orElse{case _ =>}
    new Visitor( nodeVisit.andThen{_=>true} )
  }
}