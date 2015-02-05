package javaparser

import net.liftweb.json.JsonAST
import net.liftweb.json.Printer
import java.io._
import sorra.lanka.core.Walker

object Main extends App {
  Walker.launch(
      Seq("."),
      _.getPath.endsWith(".java"),
      {context =>
        val jsonAst = MetaConversion(context)
        val writer = new PrintWriter(new File(context.file.getPath + ".json"))
        writer.write(Printer.pretty(JsonAST.render(jsonAst)))
        writer.close()
        println(context.file.getPath)
        false})
}