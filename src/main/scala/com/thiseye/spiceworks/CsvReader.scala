package com.thiseye.spiceworks

import java.io.FileReader

import au.com.bytecode.opencsv.CSVReader

class CsvReader(fileName : String) extends Traversable[Array[String]] {

  override def foreach[U](f: (Array[String]) => U): Unit = {
    val reader = new CSVReader(new FileReader(fileName))
    try {
      var next = true
      while (next) {
        val values = reader.readNext()
        if (values != null) {
          f(values)
        } else {
          next = false
        }
      }
    } finally {
      reader.close()
    }
  }
}
