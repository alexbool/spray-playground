package com.alexb.protobuf

import scala.reflect.runtime.universe._
import scala.reflect.runtime.universe.definitions._
import Field._
import ReflectionUtils._

sealed trait Message {
  def fields: Seq[Field]
  def messageName: String

  /**
   * @return Scala Type of this message
   */
  def thisType: Type
}

case class RootMessage private (messageName: String, fields: Seq[Field], thisType: Type) extends Message

sealed trait Field {
  def getter: Getter
  def fieldName: String = getter.name.decoded
}

trait Scalar extends Field {
  def optional: Boolean
}

trait MessageField extends Field with Message {
  def messageName = {
    val tpe = thisType
    val actualType = if (tpe <:< typeOf[Option[_]] || tpe <:< typeOf[Iterable[_]]) firstTypeArgument(tpe)
                     else tpe
    actualType.typeSymbol.name.decoded
  }
  def thisType = getter.returnType
}

case class Primitive private[protobuf] (getter: Getter, optional: Boolean = false) extends Scalar
case class EmbeddedMessage private[protobuf] (getter: Getter, fields: Seq[Field], optional: Boolean = false) extends Scalar with MessageField

abstract class Repeated extends Field
case class RepeatedPrimitive private[protobuf] (getter: Getter) extends Repeated
case class RepeatedMessage private[protobuf] (getter: Getter, fields: Seq[Field]) extends Repeated with MessageField

object RootMessage {
  def apply[T](implicit tt: TypeTag[T]): RootMessage = RootMessage(tt.tpe.typeSymbol.name.decoded, fieldsFor(tt.tpe), tt.tpe)

  private def fieldsFor(tpe: Type): Seq[Field] = scalaFields(tpe) map { fieldFor _ }

  private def scalaFields(tpe: Type): Seq[Getter] =
    tpe.members
      .sorted
      .filter(m => m.isPublic && m.isMethod && m.asMethod.isGetter)
      .map(_.asMethod)
      .to[Seq]

  private def fieldFor(getter: Getter): Field = {
    val tpe = getter.returnType
    if      (isPrimitive(tpe))            Primitive(getter, optional = false)
    else if (tpe <:< typeOf[Option[_]])   if (isPrimitive(firstTypeArgument(tpe))) Primitive(getter, optional = true)
                                          else EmbeddedMessage(getter, fieldsFor(firstTypeArgument(tpe)), optional = true)
    else if (tpe <:< typeOf[Iterable[_]]) if (isPrimitive(firstTypeArgument(tpe))) RepeatedPrimitive(getter)
                                          else RepeatedMessage(getter, fieldsFor(firstTypeArgument(tpe)))
    else                                  EmbeddedMessage(getter, fieldsFor(tpe), optional = false)
  }
}

object Field {
  type Getter = MethodSymbol
}

object ReflectionUtils {
  def isPrimitive(tpe: Type): Boolean = tpe <:< AnyValTpe || tpe <:< typeOf[String]

  def typeArguments(tpe: Type) = tpe match {
    case TypeRef(_, _, args) => args
  }

  def firstTypeArgument(tpe: Type): Type = typeArguments(tpe).head
}