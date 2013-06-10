package com.alexb.protobuf.macros

import scala.reflect.macros.Context
import com.alexb.protobuf._
import com.google.protobuf.CodedOutputStream

object Macros {

  def serializer[T: c.WeakTypeTag](c: Context): c.Expr[Serializer[T]] = {
    import c.universe._

    val tt = implicitly[c.WeakTypeTag[T]]
    val mm = MessageMetadata[c.universe.type](c.universe)
    val helper = new Helper[c.type](c)

    val rm: mm.RootMessage = mm.apply(tt.tpe)

    require(rm.fields.size > 0, "Message object must contain at leat one field")

    val out = c.Expr[CodedOutputStream](Ident(newTermName("output"))) // XXX Not that safe
    val obj = c.Expr[T](Ident(newTermName("obj")))                    // XXX And this too

    def toExpr[V](v: V): c.Expr[V] = c.Expr[V](Literal(Constant(v)))
    def value(obj: c.Expr[Any], f: mm.Field): c.Expr[Any] = c.Expr(Select(obj.tree, f.getter))

    def serializeField(obj: c.Expr[Any], f: mm.Field): c.Expr[Unit] = f match {
      case p: mm.Primitive => {
        val exprF: c.Expr[Any] => c.Expr[Unit] = e => helper.writePrimitive(p.actualType)(out, toExpr(p.number), e)
        if (p.optional) helper.optional(value(obj, f).asInstanceOf[c.Expr[Option[Any]]], exprF)
        else exprF(value(obj, f))
      }
      case rp: mm.RepeatedPrimitive => {
        val exprF: c.Expr[Any] => c.Expr[Unit] = e => helper.writePrimitive(rp.actualType)(out, toExpr(rp.number), e)
        helper.repeated(value(obj, f).asInstanceOf[c.Expr[Seq[Any]]], exprF)
      }
      /*case em: EmbeddedMessage => {

      }*/
      case _ => throw new NotImplementedError("This is not implemented. Sorry")
    }

    def serializeEmbeddedMessage(m: mm.Message, number: c.Expr[Int], obj: c.Expr[Any]): c.Expr[Unit] = {
      // 1. Compute size
      // 2. Write tag and size
      // 3. Write fields

      ???
    }

    def messageSize(m: mm.Message, obj: c.Expr[Any]): c.Expr[Int] = {
      // 1. Get sizes of all the fields
      // 2. Sum them
      m.fields
        .map(f => f match {
          case f: mm.Primitive =>
          case f: mm.RepeatedPrimitive =>
          case f: mm.EmbeddedMessage =>
          case f: mm.RepeatedMessage =>
        })
    }

    val fieldSerializations: Seq[c.Expr[Unit]] = rm.fields.map(serializeField(obj, _))
    val fs = c.Expr(fieldSerializations.map(_.tree).reduce(Block(_, _)))
    println(fs)

    val resultingSerializer = reify {
      new MacroSerializerBase[T] {
        protected def serialize(obj: T, output: CodedOutputStream) {
          println("Hello, World!")
          fs.splice
        }
      }
    }
    resultingSerializer
  }
}
