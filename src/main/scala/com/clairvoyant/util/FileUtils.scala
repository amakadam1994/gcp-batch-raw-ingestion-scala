package com.clairvoyant.util

import org.slf4j.LoggerFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path

object FileUtils {
  private val logger = LoggerFactory.getLogger("ETLLoads")
  val config = new Configuration()

  val fs = FileSystem.get(config)

  def createEmptyFile(path: String, file: String): Boolean = {
    var created = true
    val filePath = new Path(path + "/" + file)
    createDirectoryIfNotExists(path)

    if (!fs.createNewFile(filePath)) {
      created = false
      throw new Exception(s"Unable to create ${filePath.getName}")
    }
    logger.info(s"$file created")
    created
  }

  def createDirectoryIfNotExists(pathStr: String): Boolean = {
    var created = true
    val path = new Path(pathStr)

    if (!fs.exists(path)) {
      created = fs.mkdirs(path)
    }
    logger.info(s"$path created")
    created
  }

  def delete(path: String): Unit = {
    deleteRecursively(new Path(path))
    logger.info(s"$path deleted")
  }

  def deleteRecursively(file: Path): Unit = {
    if (fs.exists(file)) {
      if (!fs.delete(file, true)) {
        throw new Exception(s"Unable to delete ${file.getName}")
      }
    }
  }

  def exists(path: String): Boolean = {
    fs.exists(new Path(path))
  }
}
