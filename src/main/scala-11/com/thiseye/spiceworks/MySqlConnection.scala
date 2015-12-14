package com.thiseye.spiceworks

import java.sql.DriverManager

class MySqlConnection {

  val url = "jdbc:mysql://localhost:3306/mysql"
  val driver = "com.mysql.jdbc.Driver"
  val username = "root"
  val password = "root"

  def connect = {
    Class.forName(driver)
    DriverManager.getConnection(url, username, password)
  }
}