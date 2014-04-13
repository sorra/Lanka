package sorra.lanka.core

import scala.collection._
import scala.collection.JavaConversions._
import org.eclipse.jdt.core.dom._
import java.io.File
import java.nio.file.Paths
import java.nio.file.Files
import scala.actors.threadpool.Executors
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import scala.actors.threadpool.TimeUnit
import org.eclipse.jface.text.Document

class Walker(roots: Seq[String], filter: File => Boolean, astFunction: AstContext => Boolean) {
  val files: Queue[File] = new ConcurrentLinkedQueue

  implicit def makeRunnable(runnable: () => Unit) = {
    new Runnable {
      override def run() { runnable() }
    }
  }

  def start() {
    for (each <- roots.map(Paths.get(_).toFile)) {
      println("root: " + each)
      through(each)
    }
    
    val threads = {
      val processors = Runtime.getRuntime.availableProcessors
      if (processors > 4) 4 else processors
    }
    
    val executorService = Executors.newFixedThreadPool(threads)
    for (i <- 1 to threads) yield {
      executorService.execute(() => {
        var stop = false
        while (!stop) {
          val file = files.poll
          if (file == null) {
            stop = true
          }
          else {
            processFile(file)
          }
        }
      })
    }
    executorService.shutdown()
    executorService.awaitTermination(10, TimeUnit.MINUTES)
  }

  private def processFile(file: java.io.File): Any = {
    try {
      val astContext = parse(file)
      if (astFunction(astContext)) {
        println("Write file: " + file)
        Files.write(file.toPath, rewrite(astContext).getBytes)
      }
    } catch {
      case e: Throwable => System.out.synchronized {println("Error at file: " + file); throw e}
    }
  }

  private def through(in: File) {
    if (in.isDirectory) {
      in.listFiles.foreach(through _)
    } else if (filter(in)) {
      files add in
    }
  }

  private def parse(file: File) = {
    val source = new String(Files.readAllBytes(file.toPath), "UTF-8")
    val parser = ASTParser.newParser(AST.JLS4)
    parser setKind ASTParser.K_COMPILATION_UNIT
    parser setCompilerOptions Walker.compilerOptions
    parser setSource source.toCharArray
    val cu = parser.createAST(null).asInstanceOf[CompilationUnit]
    cu.recordModifications()
    
    AstContext(cu, source, file)
  }
  
  private def rewrite(context: AstContext) = {
    val document = new Document(context.source)
    val edits = context.cu.rewrite(document, Walker.formatterOptions)
    edits.apply(document)
    document.get
  }
}

object Walker {
  val compilerOptions = {
    import org.eclipse.jdt.core.JavaCore
    val compilerOptions: mutable.Map[String, String] = JavaCore.getOptions().
      asInstanceOf[java.util.Map[String, String]]
    import JavaCore._
    compilerOptions +=
      COMPILER_COMPLIANCE -> VERSION_1_7 +=
      COMPILER_CODEGEN_TARGET_PLATFORM -> VERSION_1_7 +=
      COMPILER_SOURCE -> VERSION_1_7
  }

  val formatterOptions = {
    import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants._
    val formatterOptions: mutable.Map[String, String] = getEclipseDefaultSettings().
      asInstanceOf[java.util.Map[String, String]]
    import org.eclipse.jdt.core.JavaCore
    import JavaCore.{VERSION_1_7, SPACE}
    formatterOptions +=
      JavaCore.COMPILER_COMPLIANCE -> VERSION_1_7 +=
      JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM -> VERSION_1_7 +=
      JavaCore.COMPILER_SOURCE -> VERSION_1_7 +=
      FORMATTER_TAB_CHAR -> SPACE +=
      FORMATTER_TAB_SIZE -> "2" +=
      FORMATTER_LINE_SPLIT -> "100" +=
      FORMATTER_JOIN_WRAPPED_LINES -> FALSE +=
      FORMATTER_JOIN_LINES_IN_COMMENTS -> FALSE
  }

  def launch(roots: Seq[String], filter: File => Boolean, astFunction: AstContext => Boolean) {
    new Walker(roots, filter, astFunction).start()
  }
}