package com.clairvoyant.etl

import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions.{col, udf}
import java.sql.Timestamp
import java.time.LocalDateTime

import com.clairvoyant.domain.Entity

trait EtlType[T <: Entity[T]] {
  lazy val name: String = this.getClass.getSimpleName.stripSuffix("$")

  @transient protected lazy val log: Logger = Logger.getLogger("ETLLoads")
  private[etl] var is_transformed: Boolean = false

  def load(spark: SparkSession): Dataset[T]

  def dataset(spark: SparkSession): Dataset[T] = {
    load(spark)
  }

}

trait OutputEtlType[T <: Entity[T]] extends EtlType[T] {
  lazy val current_timestamp: Timestamp = new Timestamp(System.currentTimeMillis())
  lazy val ts1900 = Timestamp valueOf (LocalDateTime of(1900, 1, 1, 0, 0))
  lazy val tsY3K = Timestamp valueOf (LocalDateTime of(2999, 1, 1, 0, 0))

  def transform(spark: SparkSession): Dataset[T]

  def dataset(spark: SparkSession, runTransform: Boolean): Dataset[T] =
    if (is_transformed) {
      log.info(s"Returning previously transformed or loaded dataset for $name.")
      dataset(spark)
    } else if (runTransform) {
      log.info(s"Running transform for dataset $name")
      val ds: Dataset[T] = transform(spark)
      is_transformed = true
      ds

    } else {
      log.info(s"Loading dataset for $name without running transform")
      val ds: Dataset[T] = dataset(spark)
      is_transformed = true
      ds

    }

  def addPrimaryId(spark: SparkSession, dataframe: DataFrame, primaryKeys: String, hashColumnName: String): DataFrame = {
    val addMaxBigInt = udf((number: Long) => BigInt(Long.MaxValue).*(3).+(number))
    val addMaxBigIntUDF = spark.udf.register("addMaxBigInt", addMaxBigInt)

    dataframe
      .withColumn(hashColumnName, addMaxBigIntUDF(primaryKeys.split(",")
        .map(_.trim)
        .toList
        .distinct
        .map(col): _*))
  }

  def save(spark: SparkSession, runTransform: Boolean): Boolean

}