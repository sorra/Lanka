package sorra.lanka.core

import org.eclipse.jdt.core.dom._
import scala.reflect.ClassTag

abstract class Selector[T <: ASTNode : ClassTag] extends ASTVisitor {

      private val clazz = implicitly[ClassTag[T]].runtimeClass
      
      def start(scope: ASTNode) = {
        scope.accept(this)
        this
      }
      
      def visiting(node: T) : Boolean
      
      override def preVisit2(node: ASTNode): Boolean = {
        preVisit(node)
        if (clazz.isInstance(node)) visiting(node.asInstanceOf[T])
        else true
      }
}