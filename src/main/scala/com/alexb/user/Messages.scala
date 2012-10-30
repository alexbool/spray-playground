package com.alexb.user

case class CheckResult(success: Boolean)
case class RegisterUserCommand(username: String, password: String)
