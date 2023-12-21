package com.clairvoyant.etl.sources

import com.clairvoyant.domain.Mapping
import com.clairvoyant.etl.EtlType
import com.clairvoyant.util.DatasetUtils
import org.apache.spark.sql.{Dataset, SparkSession}
import org.slf4j.LoggerFactory

object SrcMapping extends EtlType[Mapping] {

  private val logger = LoggerFactory.getLogger(getClass)

  override def load(spark: SparkSession): Dataset[Mapping] = {
    DatasetUtils.load[Mapping](spark, Config.srcMappingPath, Config.JsonReadFormat)
  }

}
