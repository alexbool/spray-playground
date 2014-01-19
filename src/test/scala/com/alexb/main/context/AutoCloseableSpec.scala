package com.alexb.main.context

import org.scalatest.{Matchers, WordSpec}
import java.io.Closeable

class AutoCloseableSpec extends WordSpec with Matchers {
  object TestInnerObject extends AutoCloseable with CloseableMembers { }

  "AutoCloseable trait" should {
    "find and close Closeable fields in class instances" in {
      val objectUnderTest = new TestCloseableClass
      objectUnderTest.close()
      objectUnderTest.closeableVal.isClosed should be (true)
      objectUnderTest.closeableVar.isClosed should be (true)
      objectUnderTest.closeableGetter.isClosed should be (true)
    }
    "find and close Closeable fields in outer objects" in {
      TestOuterObject.close()
      TestOuterObject.closeableVal.isClosed should be (true)
      TestOuterObject.closeableVar.isClosed should be (true)
      TestOuterObject.closeableGetter.isClosed should be (true)
    }
    "find and close Closeable fields in inner objects" in {
      TestInnerObject.close()
      TestInnerObject.closeableVal.isClosed should be (true)
      TestInnerObject.closeableVar.isClosed should be (true)
      TestInnerObject.closeableGetter.isClosed should be (true)
    }
  }
}

object TestOuterObject extends AutoCloseable with CloseableMembers {
}

class TestCloseableClass extends AutoCloseable with CloseableMembers {
}

class CloseableMock extends Closeable {
  var isClosed = false
  def close() {
    isClosed = true
  }
}

trait CloseableMembers {
  val closeableVal = new CloseableMock
  var closeableVar = new CloseableMock
  private val hiddenCloseableVal = new CloseableMock
  def closeableGetter = hiddenCloseableVal
}
