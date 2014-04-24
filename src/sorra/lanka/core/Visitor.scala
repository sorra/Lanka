package sorra.lanka.core

import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.ASTNode

class Visitor(yourNodeVisit: PartialFunction[ASTNode, Boolean]) extends ASTVisitor {
  private val nodeVisit: PartialFunction[ASTNode, Boolean] = yourNodeVisit.orElse({case _ => true})
  
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
  def apply(yourNodeVisit: PartialFunction[ASTNode, Boolean]) = {
    new Visitor(yourNodeVisit)
  }
}