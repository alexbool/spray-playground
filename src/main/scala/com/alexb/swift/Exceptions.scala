package com.alexb.swift

class SwiftException(val cause: Throwable) extends RuntimeException(cause)
class BadCredentialsException extends RuntimeException("Provided credentials are not valid")
