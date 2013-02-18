package com.alexb.main.context

import scala.reflect.runtime.universe._
import java.io.Closeable

trait AutoCloseable extends Closeable {
  def close() {
    val rm = runtimeMirror(this.getClass.getClassLoader)
    val instanceMirror = rm.reflect(this)
    val members = instanceMirror.symbol.typeSignature.members
    members
    .filter { m =>
      m.isMethod &&
      (m.asMethod.isVal || m.asMethod.isVar || m.asMethod.isGetter) &&
      m.typeSignature.baseClasses.contains(typeOf[Closeable].typeSymbol)
    }
    .foreach { m =>
      instanceMirror.reflectMethod(m.asMethod).apply().asInstanceOf[Closeable].close()
    }
  }
}
