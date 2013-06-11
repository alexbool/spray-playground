package com.alexb.protobuf

import scala.reflect.api.Universe

class MessageMetadata[U <: Universe](val u: U) {
  import u._
  import u.definitions._
  import ReflectionUtils._

  type Getter = MethodSymbol

  sealed trait Message {
    def fields: Seq[Field]
    def messageName: String

    /**
     * @return Scala Type of this message
     */
    def thisType: Type
  }

  case class RootMessage private[MessageMetadata] (messageName: String, fields: Seq[Field], thisType: Type) extends Message

  sealed trait Field {
    def number: Int
    def getter: Getter
    def fieldName: String = getter.name.decoded
    def actualType = {
      val tpe = getter.returnType
      if (tpe <:< typeOf[Option[_]] || tpe <:< typeOf[Iterable[_]]) firstTypeArgument(tpe)
      else tpe
    }
  }

  sealed trait Scalar extends Field {
    def optional: Boolean
  }

  sealed trait MessageField extends Field with Message {
    def messageName = actualType.typeSymbol.name.decoded
    def thisType = getter.returnType
  }

  case class Primitive private[protobuf] (number: Int, getter: Getter, optional: Boolean = false) extends Scalar
  case class EmbeddedMessage private[protobuf] (number: Int, getter: Getter, fields: Seq[Field], optional: Boolean = false) extends Scalar with MessageField

  sealed trait Repeated extends Field
  case class RepeatedPrimitive private[protobuf] (number: Int, getter: Getter) extends Repeated
  case class RepeatedMessage private[protobuf] (number: Int, getter: Getter, fields: Seq[Field]) extends Repeated with MessageField

  //object RootMessage {
    def apply[T](implicit tt: WeakTypeTag[T]): RootMessage = apply(tt.tpe)
    def apply(tpe: Type): RootMessage = RootMessage(tpe.typeSymbol.name.decoded, fieldsFor(tpe), tpe)

    private def fieldsFor(tpe: Type): Seq[Field] = {
      val numbers = Stream from 1
      scalaFields(tpe).zip(numbers).map { typeAndNum => fieldFor(typeAndNum._2, typeAndNum._1) }
    }

    private def scalaFields(tpe: Type): Seq[Getter] =
      tpe.members
        .sorted
        .filter(m => m.isPublic && m.isMethod && m.asMethod.isGetter)
        .map(_.asMethod)
        .to[Seq]

    private def fieldFor(number: Int, getter: Getter): Field = {
      val tpe = getter.returnType
      if      (isPrimitive(tpe))            Primitive(number, getter, optional = false)
      else if (tpe <:< typeOf[Option[_]])   if (isPrimitive(firstTypeArgument(tpe))) Primitive(number, getter, optional = true)
                                            else EmbeddedMessage(number, getter, fieldsFor(firstTypeArgument(tpe)), optional = true)
      else if (tpe <:< typeOf[Iterable[_]]) if (isPrimitive(firstTypeArgument(tpe))) RepeatedPrimitive(number, getter)
                                            else RepeatedMessage(number, getter, fieldsFor(firstTypeArgument(tpe)))
      else                                  EmbeddedMessage(number, getter, fieldsFor(tpe), optional = false)
    }
  //}

  object ReflectionUtils {
    def isPrimitive(tpe: Type): Boolean = tpe <:< AnyValTpe || tpe <:< typeOf[String]

    def typeArguments(tpe: Type) = tpe match {
      case TypeRef(_, _, args) => args
    }

    def firstTypeArgument(tpe: Type): Type = typeArguments(tpe).head
  }
}

object MessageMetadata {
  def apply[U <: Universe](u: U) = new MessageMetadata[u.type](u)
  val runtime = MessageMetadata[scala.reflect.runtime.universe.type](scala.reflect.runtime.universe)
}