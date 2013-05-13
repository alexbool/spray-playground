package com.alexb

import scala.concurrent.Future

package object swift {
  type Action[R] = AuthenticationResult => Future[R]
}
