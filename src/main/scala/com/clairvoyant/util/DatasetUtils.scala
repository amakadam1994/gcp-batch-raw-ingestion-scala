package com.clairvoyant.util

import com.clairvoyant.Config
import com.clairvoyant.Config.$
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import scala.reflect.runtime.universe._
import org.apache.spark.sql.{Dataset, SparkSession}
import org.slf4j.LoggerFactory
import org.apache.spark.sql._

object DatasetUtils {
  private[util] lazy val isLocalEnv = $("environment") == "local"
  private val logger = LoggerFactory.getLogger("ETLLoads")

  def save[T](dataset: Dataset[T], path: String, format: String = Config.WriteFormat, options: Map[String, String] = Config.WriteOptions): Unit = {
    if (format == "bigquery")
      (if (isLocalEnv) dataset.repartition(1) else dataset)
        .toDF.write
        .format("com.google.cloud.spark.bigquery.BigQueryRelationProvider")
        .mode("overwrite")
        .option("table", path)
        .option("temporaryGcsBucket", s"${Config.temporaryGcsBucket}")
        .save()
    else
      logger.info(s"There is no implementation to write data in ${format} format")
  }

  def load[T: TypeTag](spark: SparkSession, path: String, format: String, encoder: Option[Encoder[T]] = None): Dataset[T] = {
    val enc = encoder match {
      case Some(e) => e
      case None => ExpressionEncoder[T]()
    }

    if (format == "csv")
      spark
        .read
        .format("com.databricks.spark.csv")
        .option("header", "true")
        .load(path).as[T](enc)
    else if (format == "json")
      spark
        .read
        .format("org.apache.spark.sql.execution.datasources.json.JsonFileFormat")
        .load(path).as[T](enc)
    else
      spark.read.format(format).load(path).as[T](enc)
  }

}
