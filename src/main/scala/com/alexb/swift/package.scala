package com.alexb

import scala.concurrent.Future

package object swift {
  type Action[R] = AuthenticationResult => Future[R]
  private[swift] case class AuthenticationResult(token: String, storageUrl: String)
}
