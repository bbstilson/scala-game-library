package sgl.util

/**
 * Super trait for anything that can be loaded.
 * 
 * In Scala 3, this can be replaced with a union type.
 * 
 * Scala 2:
 * val assets: Map[String, Loader[Asset]]
 * 
 * Scala 3:
 * val assets: Map[String, Loader[Music | Sound | BitMap]]
 */
trait Asset
