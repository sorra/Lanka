package javaparser

import org.eclipse.jdt.core.{dom => jdt}
import scala.collection.JavaConversions._
import net.liftweb.json._
import scala.collection.mutable.ArrayBuffer
import sorra.lanka.core.AstContext

object MetaConversion {
  def apply(context: AstContext): JObject = conv(context.cu, context)
  
  def conv(node: jdt.ASTNode, ctx: AstContext): JObject = {
    val fields = ArrayBuffer[JField]()
    for (prop <- node.structuralPropertiesForType) {
      val propDesc = prop.asInstanceOf[jdt.StructuralPropertyDescriptor]
      val name = propDesc.getId
      val value = node.getStructuralProperty(propDesc)
      if (value != null) {
        fields += (propDesc match {
          case childProp: jdt.ChildPropertyDescriptor =>
            JField(name, conv(value.asInstanceOf[jdt.ASTNode], ctx))
          case childListProp: jdt.ChildListPropertyDescriptor =>
            val list = value.asInstanceOf[java.util.List[jdt.ASTNode]]
            JField(name, JArray(list.map(conv(_, ctx)).toList))
          case simpleProp: jdt.SimplePropertyDescriptor =>
            JField(name, JString(value.toString))
        })
      }
    }
    
    val tail: List[JField] =
      if (node eq ctx.cu) {
        val comments =
          ctx.cu.getCommentList.toList.asInstanceOf[List[jdt.Comment]]
            .filter {_.getParent eq null}
            .map {comment=>
              JObject(JField("source", JString(
                  ctx.source.substring(comment.getStartPosition, comment.getStartPosition + comment.getLength)))
                :: commonFields(comment, ctx))}
        List(JField("comments", JArray(comments)))
      } else {Nil}
    
    JObject(commonFields(node, ctx):::fields.toList:::tail)
  }
  
  def commonFields(node: jdt.ASTNode, ctx: AstContext) = {
    List(JField("nodeClass", JString(node.getClass.getSimpleName)),
        JField("startPosition", JInt(node.getStartPosition)),
        JField("length", JInt(node.getLength)),
        JField("startLine", JInt(ctx.cu.getLineNumber(node.getStartPosition))))
  }
  
}