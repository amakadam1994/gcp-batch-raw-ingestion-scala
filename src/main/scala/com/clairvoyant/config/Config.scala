package com.clairvoyant

import com.typesafe.config.{Config, ConfigFactory}

object Config {
  lazy val config: Config = ConfigFactory.load

  val environment: String = $("environment")
  lazy val projectId: String = $("project_id")
  lazy val temporaryGcsBucket: String = $("temporaryGcsBucket")
  lazy val doneFolderName: String = $("doneFolderName")
  lazy val distSchema: String = $("distSchema")

  val WriteFormat: String = "bigquery"
  val WriteOptions: Map[String, String] = Map(("a", "1"))
  val CsvReadFormat: String = "csv"
  val JsonReadFormat: String = "json"
  val ReadFormat: String = "bigquery"
  val ReadOptions: Map[String, String] = Map(("a", "1"))

  lazy val doneFilePath = s"gs://${temporaryGcsBucket}/${doneFolderName}_done_files"
  lazy val listDoneFilePath = s"${doneFilePath}/list"
  lazy val tableDoneFilePath = s"${doneFilePath}/table"

  val mappingPath: String = dimPathOf("mapping")
  val srcMappingPath: String = sourcePathOf("mapping")

  val ProcessSuccess = true
  val ProcessFailure = false

  def loadTable(etlType: String, etlName: String): String =
    $("all_datasets.%s.%s.schema".format(etlType, etlName)) + '.' + $("all_datasets.%s.%s.name".format(etlType, etlName))

  def sourcePathOf(etlName: String): String = $("all_datasets.gcs.%s.path".format(etlName))

  def dimPathOf(etlName: String): String = $("all_datasets.bigquery.%s.path".format(etlName))

  def dimPrefixNameOf(etlName: String): String = $("all_datasets.gcs.%s.prefix".format(etlName))

  def $: String => String = key => config.getString(key)

}
