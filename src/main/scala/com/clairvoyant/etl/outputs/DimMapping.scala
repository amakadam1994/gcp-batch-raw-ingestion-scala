package com.clairvoyant.etl.outputs

import com.clairvoyant.domain.Mapping
import com.clairvoyant.etl.OutputEtlType
import com.clairvoyant.etl.sources.SrcMapping
import com.clairvoyant.util.DatasetUtils
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.{Dataset, SparkSession}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object DimMapping extends OutputEtlType[Mapping] {

  private val logger = LoggerFactory.getLogger(getClass)

  override def load(spark: SparkSession): Dataset[Mapping] = {
    DatasetUtils.load[Mapping](spark, Config.mappingPath, Config.ReadFormat)
  }

  override def save(spark: SparkSession, runTransform: Boolean): Boolean = {
    Try(DatasetUtils.save(dataset(spark, runTransform), Config.mappingPath)) match {
      case Success(_) => ProcessSuccess
      case Failure(e) => logger.error("Exception happened when trying to save the data", e)
        ProcessFailure
    }
  }

  override def transform(spark: SparkSession): Dataset[Mapping] = {
    lazy val mappingdf = SrcMapping.dataset(spark).toDF()
    mappingdf.show()
    mappingdf.as[Mapping](ExpressionEncoder[Mapping]())
  }

}
