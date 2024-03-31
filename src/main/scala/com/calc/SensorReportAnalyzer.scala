package com.calc

import scala.io.Source
import java.io.File

object SensorReportAnalyzer {

  case class Measurement(sensorId: String, humidity: Option[Int])

  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      println("No/Wrong Arguments passed. Expected <directory-path>")
      sys.exit(1)
    }
    val directoryPath = args(0)
    calculateDailyReportStats(directoryPath)
  }

  def calculateDailyReportStats(directoryPath: String): Unit = {
    val directory = new File(directoryPath)
    if (directory.exists && directory.isDirectory) {
      val files = directory.listFiles(_.getName.endsWith(".csv"))
      if (files.nonEmpty) {
        var filesProcessed = 0
        var measurementsProcessed = 0
        var failedMeasurements = 0
        var measurementsBySensor = Map.empty[String, List[Int]]

        files.foreach { file =>
          filesProcessed += 1
          val measurements = parseCSV(file)

          measurementsProcessed += measurements.size
          failedMeasurements += measurements.count(_.humidity.isEmpty)

          measurements.foreach { measurement =>
            val currentHumidityList = measurementsBySensor.getOrElse(measurement.sensorId, Nil)
            val updatedHumidityList = measurement.humidity match {
              case Some(humidityValue) => humidityValue :: currentHumidityList
              case None => currentHumidityList
            }
            measurementsBySensor += (measurement.sensorId -> updatedHumidityList)
          }
        }

        val statsBySensor = measurementsBySensor.mapValues { humidityList =>
          val validData = humidityList.filter(_.isValidInt)
          val min = if (validData.nonEmpty) validData.min.toDouble else Double.NaN
          val avg = if (validData.nonEmpty) validData.sum.toDouble / validData.length else Double.NaN
          val max = if (validData.nonEmpty) validData.max.toDouble else Double.NaN
          (min, avg, max)
        }

        val sortedStats = statsBySensor.toList.sortBy { case (_, (_, avg, _)) => -avg }

        println(s"Num of processed files: $filesProcessed")
        println(s"Num of processed measurements: $measurementsProcessed")
        println(s"Num of failed measurements: $failedMeasurements")
        println("\nSensors with highest avg humidity:")
        println("sensor_id,min,avg,max")
        sortedStats.foreach { case (sensorId, (min, avg, max)) =>
          println(s"$sensorId,$min,$avg,$max")
        }
      } else {
        println("No CSV files found in the directory.")
      }
    } else {
      println("Invalid directory path.")
    }
  }

  def parseCSV(file: File): List[Measurement] = {
    val lines = Source.fromFile(file).getLines().toList.drop(1) // Skip header line
    lines.map { line =>
      val Array(sensorId, humidity) = line.split(",")
      Measurement(sensorId, if (humidity == "NaN") None else Some(humidity.toInt))
    }
  }

}
