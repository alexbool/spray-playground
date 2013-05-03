package com.alexb.main.context

import org.scalatest.matchers.MustMatchers
import org.scalatest.WordSpec
import java.io.Closeable

class AutoCloseableSpec extends WordSpec with MustMatchers {
  object TestInnerObject extends AutoCloseable with CloseableMembers {
  }

  "AutoCloseable trait" must {
    "find and close Closeable fields in class instances" in {
      val objectUnderTest = new TestCloseableClass
      objectUnderTest.close()
      objectUnderTest.closeableVal.isClosed must be (true)
      objectUnderTest.closeableVar.isClosed must be (true)
      objectUnderTest.closeableGetter.isClosed must be (true)
    }
    "find and close Closeable fields in outer objects" in {
      TestOuterObject.close()
      TestOuterObject.closeableVal.isClosed must be (true)
      TestOuterObject.closeableVar.isClosed must be (true)
      TestOuterObject.closeableGetter.isClosed must be (true)
    }
    "find and close Closeable fields in inner objects" in {
      TestInnerObject.close()
      TestInnerObject.closeableVal.isClosed must be (true)
      TestInnerObject.closeableVar.isClosed must be (true)
      TestInnerObject.closeableGetter.isClosed must be (true)
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
