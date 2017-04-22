package com.knowgate.bulkmailer.test

import com.knowgate.bulkmailer.hipergate.datamodel.HgModelManager

class CreateDataModel extends TestCase {

  def create = {
    val man = new HgModelManager();
    man.connect("org.postgresql.Driver", "jdbc:postgresql://127.0.0.1:5432/bulkmailer", "", "postgres", "postgres")
    man.dropAll
    // man.createDefaultDatabase
    man.disconnect
    
    // val fs = new FileSystem()
    // fs.writefilestr("C:\\Temp\\Debug\\modelmanager.txt", man.report(), "ISO8859_1")
    // println()
  }
}