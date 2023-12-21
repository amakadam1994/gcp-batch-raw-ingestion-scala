package com.clairvoyant.domain

trait Entity[T <: Entity[T]] extends Product with Serializable {
  this: T =>
}

