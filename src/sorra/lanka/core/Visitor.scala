package sorra.lanka.core

import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.ASTNode

class Visitor(yourNodeVisit: PartialFunction[ASTNode, Boolean],
              yourPreVisit: Option[Function[ASTNode, Unit]],
              yourPostVisit: Option[Function[ASTNode, Unit]]) extends ASTVisitor {
  
  private val nodeVisit: PartialFunction[ASTNode, Boolean] = yourNodeVisit.orElse({case _ => true})
  
  def start(scope: ASTNode) = {
    scope.accept(this)
    this
  }
  
  override def preVisit2(node: ASTNode): Boolean = {
    preVisit(node)
    yourPreVisit.foreach(_.apply(node))
    val continueOrStop = nodeVisit(node)
    yourPostVisit.foreach(_.apply(node))
    continueOrStop
  }
}

object Visitor {
  /**
   * yourNodeVisit matches some node types, returns true:CONTINUE or false:STOP.
   * Other node types are skipped.
   */
  def stoppable(yourNodeVisit: PartialFunction[ASTNode, Boolean]) = {
    new Visitor(yourNodeVisit, None, None)
  }
  
  def stoppable(yourNodeVisit: PartialFunction[ASTNode, Boolean],
                yourPreVisit: Function[ASTNode, Unit],
                yourPostVisit: Function[ASTNode, Unit]) = {
    new Visitor(yourNodeVisit, Some(yourPreVisit), Some(yourPostVisit))
  }
  
  /**
   * yourNodeVisit matches some node types, visiting all nodes.
   * Other node types are skipped.
   */
  def forall(yourNodeVisit: PartialFunction[ASTNode, Unit]) = {
    new Visitor( yourNodeVisit.andThen{_=>true} , None, None)
  }
  
  def forall(yourNodeVisit: PartialFunction[ASTNode, Unit],
             yourPreVisit: Function[ASTNode, Unit],
             yourPostVisit: Function[ASTNode, Unit]) = {
    new Visitor( yourNodeVisit.andThen{_=>true} , Some(yourPreVisit), Some(yourPostVisit))
  }
}