package com.clairvoyant.config

import com.typesafe.config.{Config, ConfigFactory}

object Config_bkp {
  lazy val config: Config = ConfigFactory.load

  val environment: String = $("environment")

/*
  val WriteFormat: String = $(s"$environment.write_format")
  val WriteOptions: Map[String, String] =config.getObject(s"$environment.write_options").unwrapped().asScala.mapValues(_.toString).toMap

  val ReadFormat: String = $(s"$environment.read_format")
  val ReadOptions: Map[String, String] =config.getObject(s"$environment.read_options").unwrapped().asScala.mapValues(_.toString).toMap
*/

  val WriteFormat: String = "bigquery"
  val WriteOptions: Map[String, String] =Map(("a","1"))
  val CsvReadFormat: String = "csv"
  val JsonReadFormat: String = "json"
  val ReadFormat: String = "bigquery"

  val ReadOptions: Map[String, String] =Map(("a","1"))

  val mappingPath: String = "playground-375318.transactional_data.mapping"
//  val mappingPath: String = dimPathOf("mapping")
  //val srcMappingPath: String = sourcePathOf("mapping")
  val srcMappingPath: String = "gs://bronze-poc-group/mongodb/landing/sample_analytics/customers-20231208-00000-of-00001.json"

  lazy val distSchema: String = ""
//  lazy val distSchema: String = $("schemas.distSchema")


  lazy val temporaryGcsBucket: String = $("temporaryGcsBucket")
  lazy val destinationRoot: String = ""
  //lazy val destinationRoot: String = $("destination_root")
  lazy val doneFolderName: String = $("doneFolderName")
  lazy val doneFilePath = s"gs://${temporaryGcsBucket}/${doneFolderName}_done_files"
  lazy val listDoneFilePath = s"${doneFilePath}/list"
  lazy val tableDoneFilePath = s"${doneFilePath}/table"


  private val sourceDSPathExpr= "all_datasets.sources.%s.path"
  private val outputDSPathExpr= "all_datasets.outputs.%s.path"
  private val sourceDSPrefixNameExpr= "all_datasets.sources.%s.prefix"

  private val sourceTableExpr= "all_datasets.sources.%s.name"
  private val sourceSchemaExpr= "all_datasets.sources.%s.schema"

  val Source = "sources"
  val Output = "outputs"

  val ProcessSuccess = true
  val ProcessFailure = false




  def loadTable(etlType: String, etlName: String): String =
    $("all_datasets.%s.%s.schema".format(etlType, etlName)) + '.' + $("all_datasets.%s.%s.name".format(etlType, etlName))

  def sourcePathOf(etlName: String): String = $(sourceDSPathExpr.format(etlName))

  def dimPathOf(etlName: String): String = $(outputDSPathExpr.format(etlName))

  def dimPrefixNameOf(etlName: String): String = $(sourceDSPrefixNameExpr.format(etlName))

  def $: String => String = key => config.getString(key)

}
