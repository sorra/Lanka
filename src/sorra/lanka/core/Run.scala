package sorra.lanka.core

import scala.collection._
import scala.collection.JavaConversions._
import java.nio.file.Files
import java.nio.file.Paths
import org.eclipse.jdt.core.dom._
import org.eclipse.jdt.core.JavaCore
import scala.reflect.ClassTag

object Run extends App {
  //TODO
  def selector[T <: ASTNode: ClassTag](visitor: T => Boolean): Selector[T] = {
    new Selector[T] {
      override def visiting(node: T): Boolean = visitor(node)
    }
  }

  Walker.launch(Seq("."),
    {file => file.getPath().endsWith(".java") },
    {context =>
      val AstContext(cu, source, file) = context
      val sel = {md: MethodDeclaration => 
        md.setName(md.getAST().newSimpleName("hi"))
        true
      }
      selector(sel).start(cu)
      println(cu.toString)
      false
    })
}