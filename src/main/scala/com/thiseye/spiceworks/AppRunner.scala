package com.thiseye.spiceworks

object AppRunner extends App {

  val csvPath = "/home/nirav/Downloads/Spiceworks Challenge - Identifiers Example.csv"

  new InputPersister(csvPath).process

  val connection = (new MySqlConnection).connect
  val queryStmt = connection.createStatement

  val rs = queryStmt.executeQuery(s"SELECT localId, localIdType, personId FROM `${MySqlConnection.schema}`.`Person`")

  while(rs.next) {
    println(s"idType: ${rs.getInt("localIdType")}, localId: ${rs.getString("localId")} => personId: ${rs.getLong("personId")}")
  }

  connection.close()
}
