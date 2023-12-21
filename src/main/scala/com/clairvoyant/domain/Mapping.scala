package com.clairvoyant.domain

case class Mapping(
                    birthdate: String,
                    accounts: String,
                    address: String,
                    name: String,
                    username: String,
                    email: String
                  ) extends Entity[Mapping] {
  private val propertyMap = Map(
    0 -> birthdate,
    1 -> accounts,
    2 -> address,
    3 -> name,
    4 -> username,
    5 -> email
  )

  override def productElement(n: Int): Any = propertyMap(n)

  override def productArity: Int = 6

  override def canEqual(that: Any): Boolean = this equals that
}