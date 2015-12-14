package com.thiseye.spiceworks

import java.sql.ResultSet

class InputPersister(csvFile : String) {

  val reader = new CsvReader(csvFile)
  val connection = (new MySqlConnection).connect

  private def generateNewUuid: BigInt = {
    val uuidStmt = connection.createStatement
    val rs = uuidStmt.executeQuery("SELECT UUID_SHORT()")
    rs.next()
    rs.getLong(1)
  }

  private def writeValue(values: Array[String]) = {
    // add index (and add 1 to make it 1-based) and filter out empty columns
    val valueTuples = values.zipWithIndex.filter(!_._1.isEmpty).map(t => (t._1, t._2 + 1))
    val currentEntriesInDb = getExistingEntries(valueTuples)

    var minPersonId: Option[BigInt] = None
    var existingLocalIds: List[(String, Int, Long)] = List.empty

    while (currentEntriesInDb.next) {
      val personId = currentEntriesInDb.getLong("personId")
      val entry: (String, Int, Long) = (currentEntriesInDb.getString("localId"), currentEntriesInDb.getInt("localIdType"), personId)

      // save off the found entries in the DB
      existingLocalIds = entry :: existingLocalIds

      // if we need to reassociate a person id, prefer whatever's lowest
      if (minPersonId.isEmpty || currentEntriesInDb.getLong(1) < minPersonId.get) {
        minPersonId = Option(personId)
      }
    }

    // use lowest person id found, or assign new one
    val newUuid = minPersonId.getOrElse(generateNewUuid)

    writeUpdates(valueTuples, existingLocalIds, newUuid)
    writeInserts(valueTuples, existingLocalIds, newUuid)
  }

  private def writeInserts(valueTuples: Array[(String, Int)], existingLocalIds: List[(String, Int, Long)], newUuid: BigInt) = {

    // any not already existing in the db should be written
    val tuplesToInsert = valueTuples.filterNot(existingLocalIds.map(t => (t._1, t._2)).contains(_))

    if (tuplesToInsert.nonEmpty) {
      //Ex. "( Value1, Value2, Value3 ), ( Value1, Value2, Value3 )"
      val valuesClause = tuplesToInsert.map(t => s"('${t._1}', '${t._2}', '${newUuid}')").mkString(", ")

      val insertStmt = connection.createStatement
      insertStmt.executeUpdate(
        s"INSERT INTO `${MySqlConnection.schema}`.`Person` (`localId`, `localIdType`, `personId`) " +
          s"VALUES ${valuesClause};")
    }
  }

  private def writeUpdates(valueTuples: Array[(String, Int)], existingLocalIds: List[(String, Int, Long)], newUuid: BigInt): AnyVal = {

    // any already existing in the db with a different person id need updating
    val tuplesToUpdate = valueTuples.filter(existingLocalIds.filter(_._3 != newUuid).map(t => (t._1, t._2)).contains(_))

    if (tuplesToUpdate.nonEmpty) {

      // Ex: "(localId = 'xyz' and localIdType = 1) OR (localId = 'abc' and localIdType = 2)"
      val whereClause = tuplesToUpdate.map(tuple => s"(localId = '${tuple._1}' AND localIdType = ${tuple._2})").mkString(" OR ")

      val updateStmt = connection.createStatement
      updateStmt.executeUpdate(
        s"UPDATE `${MySqlConnection.schema}`.`Person` SET personId = ${newUuid} WHERE ${whereClause};")
    }
  }

  private def getExistingEntries(valueTuples: Array[(String, Int)]): ResultSet = {
    val queryStmt = connection.createStatement

    // Ex: "(localId = 'xyz' and localIdType = 1) OR (localId = 'abc' and localIdType = 2)"
    val whereClause = valueTuples.map(tuple => s"(localId = '${tuple._1}' AND localIdType = ${tuple._2})").mkString(" OR ")

    queryStmt.executeQuery(s"SELECT personId, localId, localIdType FROM `${MySqlConnection.schema}`.`Person` WHERE $whereClause")
  }

  def process = {
    for (values <- reader) {
      writeValue(values)
    }

    connection.close()
  }
}
