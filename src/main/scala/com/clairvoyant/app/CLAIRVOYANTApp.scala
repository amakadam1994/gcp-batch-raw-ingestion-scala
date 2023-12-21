package com.clairvoyant.app

import com.clairvoyant.Config
import com.clairvoyant.SparkSessionFactory
import com.clairvoyant.util.GcsUtils
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

//case  class ETLAppParams(transformList: File = new File("."), reloadList: Seq[File]=Seq())
case class ETLAppParams(transformList: String = "")

object CLAIRVOYANTApp {

  val parser = new scopt.OptionParser[ETLAppParams]("com.clairvoyant.app.CLAIRVOYANTApp") {
    head("ETL_App", "1.0")
    opt[String]("tableList").required().action((x, c) => c.copy(transformList = x))
    //  opt[String]('t',"tableList").required()
    //    .action((x,c) => c.copy(transformList = x))
    //    .validate(f => if (f.exists) success else failure("Cannot find one or more list files"))
    //    .text("reloadList")
  }
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    System.out.println(args(0))
    parser.parse(args, ETLAppParams()) match {
      case Some(etlAppParams) =>
        println(s"Input Arg: ${etlAppParams.transformList}")
        runApp(etlAppParams)
      case None => System.err.println("Parsing of arguments failed")
    }
  }

  def runApp(etlAppParams: ETLAppParams): Unit = {
    implicit val spark = SparkSessionFactory.sparkSession

    //    val transformList = Source.fromFile(etlAppParams.transformList).getLines.filterNot(_.trim.startsWith("#")).toList
    //    val reloadList = etlAppParams.reloadList.flatMap(Source.fromFile(_).getLines.filterNot(_.trim.startsWith("#")))

    val transformList: List[String] = GcsUtils.tableListFromText(s"${etlAppParams.transformList}", spark)
    val reloadList: List[String] = List()

    logger.info(s"transformList= $transformList")
    logger.info(s"reloadList= $reloadList")

    if ((transformList intersect reloadList).nonEmpty) throw new RuntimeException("Cannot have common ETLs")
    logger.info("Starting ETL....")
    processETLTables(transformList, reloadList)
    Thread.sleep(20000)
  }

  def processETLTables(transformList: Seq[String], reloadList: Seq[String])(implicit spark: SparkSession): Unit = {
    if (reloadList.nonEmpty) logger.info("Reloading from previously created outputs")
    reloadList.map(etlType).foreach(t => {
      logger.info(s"Reloading ${t.name} dataset from its output path")
      t dataset(spark, runTransform = false)
    })

    if (transformList.nonEmpty) logger.info("Running transformations for ETLs")
    transformList.map(etlType).foreach(t => {
      lazy val done_file = s"${t.name}.done"

      //      if(!FileUtils.exists(s"${Config.listDoneFilePath}/$done_file")){
      if (!GcsUtils.checkEmptyFile(Config.listDoneFilePath, done_file, spark)) {
        logger.info(s"Running transformation for EtlType ${t.name}")
        if (!(t save(spark, runTransform = true))) {
          logger.error(s"ETL transformation failed for EtlType ${t.name}")
          throw new RuntimeException(s"Etl Failed for ${t.name}")
        }
        else {
          //          logger.info(s"ETL transformation success for EtlType ${t.name}, so creating done file")
          //          FileUtils.createEmptyFile(Config.listDoneFilePath,done_file,spark)
          //          logger.info(s"Deleting done file for EtlType ${t.name}")
          //          FileUtils.delete(Config.tableDoneFilePath)

          logger.info(s"ETL transformation success for EtlType ${t.name}, so creating done file")
          GcsUtils.createEmptyFile(Config.listDoneFilePath, done_file, spark)
        }
      } else {
        logger.info(s"Skipping ETL transformation for EtlType ${t.name} as its done file ${Config.listDoneFilePath}/$done_file exists")
      }
    })

  }

}
