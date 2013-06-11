package com.alexb.protobuf.macros

import scala.reflect.macros.Context
import com.alexb.protobuf._
import com.google.protobuf.CodedOutputStream

object Macros {

  def serializer[T: c.WeakTypeTag](c: Context): c.Expr[Serializer[T]] = {
    import c.universe._

    val tt = implicitly[c.WeakTypeTag[T]]
    val helper = new Helper[c.type](c)
    val rm: helper.mm.RootMessage = helper.mm.apply(tt.tpe)

    require(rm.fields.size > 0, s"Message ${rm.messageName} has no fields. Messages must contain at least one field")

    val out = c.Expr[CodedOutputStream](Ident(newTermName("output"))) // XXX Not that safe
    val obj = c.Expr[T](Ident(newTermName("obj")))                    // XXX And this too

    val fieldSerializations: Seq[c.Expr[Unit]] = rm.fields.map(helper.serializeField(obj, _, out))
    val fs = c.Expr(Block(fieldSerializations.map(_.tree).to[List], Literal(Constant(()))))
    println(fs)

    val resultingSerializer = reify {
      new MacroSerializerBase[T] {
        protected def serialize(obj: T, output: CodedOutputStream) {
          fs.splice
        }
      }
    }
    resultingSerializer
  }
}
