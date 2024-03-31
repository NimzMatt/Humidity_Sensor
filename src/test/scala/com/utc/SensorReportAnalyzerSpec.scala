package com.utc

import com.calc.SensorReportAnalyzer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

import java.io.File

class SensorReportAnalyzerSpec extends AnyFunSuite {

  // Helper function to create a temporary directory
  def createTempDir(): File = {
    val tempDir = new File(System.getProperty("java.io.tmpdir"), "testDir")
    tempDir.mkdir()
    tempDir
  }

  test("Test directory availability") {
    val directoryPath = "dailyreport/"
    val result = SensorReportAnalyzer.main(Array.empty)
    result shouldEqual "No Arguments passed. Expected <directory-path>"
  }

  test("Test CSV files availability") {
    val directoryPath = "/path/directory/without_csv_files"
    assert(new File(directoryPath).exists(), s"Directory '$directoryPath' should exist.")
    assert(new File(directoryPath).listFiles(_.getName.endsWith(".csv")).isEmpty, "No CSV files should be present in the directory.")
  }

  test("Test argument passed") {
    assertThrows[ArrayIndexOutOfBoundsException] {
      SensorReportAnalyzer.main(Array.empty[String])
    }
  }


  test("Parsing CSV file"){
    val testFile = new File("test.csv")
    val writer = new java.io.PrintWriter(testFile)
    try {
      writer.write("sensor-id,humidity\n")
      writer.write("s1,10\n")
      writer.write("s2,NaN\n")
      writer.write("s3,20\n")
    } finally writer.close()

    val measurements = SensorReportAnalyzer.parseCSV(testFile)
    measurements should have size 3
    measurements(0) shouldBe SensorReportAnalyzer.Measurement("s1", Some(10))
    measurements(1) shouldBe SensorReportAnalyzer.Measurement("s2", None)
    measurements(2) shouldBe SensorReportAnalyzer.Measurement("s3", Some(20))

    testFile.delete()
  }

  test("Test statistics calculated "){
    val tempDir = createTempDir()
    val file1 = new File(tempDir, "test1.csv")
    val content1 =
      """Sensor-id,humidity
        |s1,10
        |s2,88
        |s1,NaN""".stripMargin
    val file2 = new File(tempDir, "test2.csv")
    val content2 =
      """Sensor-id,humidity
        |s2,80
        |s3,NaN
        |s2,78
        |s1,98""".stripMargin
    val writer1 = new java.io.PrintWriter(file1)
    writer1.write(content1)
    writer1.close()
    val writer2 = new java.io.PrintWriter(file2)
    writer2.write(content2)
    writer2.close()

    val result = SensorReportAnalyzer.calculateDailyReportStats(tempDir.getAbsolutePath).toString.split("\n").map(_.trim)
    result(1) shouldEqual "Num of processed files: 2"
    result(2) shouldEqual "Num of processed measurements: 7"
    result(3) shouldEqual "Num of failed measurements: 2"
    result(8) shouldEqual "sensor_id,min,avg,max"
    result(9) shouldEqual "s2,78.0,82.0,88.0"
    result(10) shouldEqual "s1,10.0,54.0,98.0"
    result(11) shouldEqual "s3,NaN,NaN,NaN"
  }
}
