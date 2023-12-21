package com.clairvoyant

import com.clairvoyant.etl.OutputEtlType
import com.clairvoyant.etl.outputs._
import org.slf4j.LoggerFactory

package object app {

  private val logger = LoggerFactory.getLogger(getClass)
  val etlType: PartialFunction[String, OutputEtlType[_]] = {
    case "mapping" => DimMapping

    case nonExisting: String => {
      logger.error("ETL failed..!!", new RuntimeException(s"No ETL implementation available for $nonExisting"))
      throw new RuntimeException(s"No ETL implementation available for $nonExisting")
    }
  }

  case class LoadEntityParams(input: String = null, output: String = null)

  case class CPLoadJobConf(user: String, dateTime: String)

}
