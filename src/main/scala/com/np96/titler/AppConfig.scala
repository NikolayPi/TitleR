package com.np96.titler

import pureconfig._
import pureconfig.generic.auto._

final case class AppConfig(
    host: String,
    port: Int
)

object AppConfig {
  def loadConfig(): AppConfig = {
    ConfigSource.default.load[AppConfig].getOrElse(throw new IllegalStateException("Could not read app config"))
  }
}
