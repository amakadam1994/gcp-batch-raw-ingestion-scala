package com.clairvoyant.util

import org.apache.hadoop.conf.Configuration
import org.apache.spark.sql.{SparkSession, Row}
import org.apache.spark.sql.types.{StructType, StructField, StringType}
import org.slf4j.LoggerFactory

object GcsUtils {
  private val logger = LoggerFactory.getLogger("ETLLoads")
  val config = new Configuration()

  def createEmptyFile(path: String, file: String, spark: SparkSession): Boolean = {
    var created = false
    val gcsFilePath = path + "/" + file
    val values = Seq(".done")
    val schema = StructType(Seq(StructField("column_name", StringType, nullable = true)))
    val rows = values.map(value => Row(value))
    val singleColumnDF = spark.createDataFrame(spark.sparkContext.parallelize(rows), schema).coalesce(1)

    if (!created)
      singleColumnDF.write.option("header", "true").csv(gcsFilePath)
    logger.info(s".done file created")
    created = true

    created
  }

  def checkEmptyFile(path: String, file: String, spark: SparkSession): Boolean = {
    var created = false
    val gcsFilePath = path + "/" + file
    try {
      val df = spark.read
        .format("com.databricks.spark.csv")
        .option("header", "true").load(gcsFilePath)
      if (df.count() > 0)
        created = true
    } catch {
      case ex: Exception => logger.info(s"${gcsFilePath} does not exists. Running etl for this table")
    }

    created
  }

  def tableListFromText(gcsFilePath: String, spark: SparkSession): List[String] = {
    val textFileDF = spark.read.text(gcsFilePath)
    val contentList: List[String] = textFileDF.collect().map(row => row.getString(0)).toList

    contentList
  }

}
