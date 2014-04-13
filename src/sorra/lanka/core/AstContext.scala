package sorra.lanka.core

import org.eclipse.jdt.core.dom.CompilationUnit
import java.io.File

case class AstContext(val cu: CompilationUnit, val source: String, val file: File) {
}

//object AstContext {
//  def apply(cu: CompilationUnit, source: String, file: File) = new AstContext(cu, source, file)
//  def unapply(context: AstContext) = Some(context.cu, context.source, context.file)
//}