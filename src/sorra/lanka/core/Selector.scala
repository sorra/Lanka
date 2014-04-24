package sorra.lanka.core

import org.eclipse.jdt.core.dom._
import scala.reflect.ClassTag
import scala.collection.mutable.ArrayBuffer

class Selector[T <: ASTNode : ClassTag](nodeVisit: T => Boolean) extends ASTVisitor {

      private val clazz = implicitly[ClassTag[T]].runtimeClass
      private var started = false
      private val hitNodes = ArrayBuffer[T]()
      
      def start(scope: ASTNode) = {
        started = true
        scope.accept(this)
        this
      }

      def results: Seq[T] = {
        if (started) hitNodes
        else throw new IllegalStateException
      }
      
      protected def add(node: T) {
        hitNodes += node
      }
      
      override def preVisit2(node: ASTNode): Boolean = {
        preVisit(node)
        if (clazz.isInstance(node)) nodeVisit(node.asInstanceOf[T])
        else true
      }
}

object Selector {
  /**
   * nodeVisit gets the node, returns true:CONTINUE or false:STOP
   */
  //TODO
  def apply[T <: ASTNode: ClassTag](nodeVisit: T => Boolean): Selector[T] = {
    new Selector[T](nodeVisit)
  }
}