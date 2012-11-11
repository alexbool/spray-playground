package com.alexb

import akka.dispatch.Future
import spray.http.HttpCredentials

package object oauth {

	type Token = String
	type OAuthTokenValidator[T] = Option[Token] => Future[Option[T]]
}

package oauth {
	
	case class User(username: String, authorities: Seq[String])
}
