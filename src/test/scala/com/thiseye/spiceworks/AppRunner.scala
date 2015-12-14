package com.thiseye.spiceworks

object AppRunner extends App {

  val csvPath = "/home/nirav/Downloads/Spiceworks Challenge - Identifiers Example.csv"

  new InputPersister(csvPath).process

}
