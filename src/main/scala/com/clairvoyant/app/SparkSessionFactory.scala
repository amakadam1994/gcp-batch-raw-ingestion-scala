package com.clairvoyant

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SparkSessionFactory {
  private val conf = new SparkConf()
    .setIfMissing("spark.app.name", "Data Ingestion")
    .setIfMissing("spark.master", "local[*]")

  def sparkSession: SparkSession = {
    SparkSession.builder().config(conf).getOrCreate()
  }

}
