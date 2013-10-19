object worksheet {

  import com.github.joachimmaes.filedsl.DynamicFileDSL._

  val d = directory("/tmp/acme") {
    file("README", contents = "hello world")
		directory("csv") {
		  file("data-001.csv", contents = "...")
		  file("data-002.csv", contents = "...")
		}
  }                                               //> d  : com.github.joachimmaes.filedsl.DynamicFileDSL.DirectoryDescription = Di
                                                  //| rectoryDescription(/tmp/acme,List(DirectoryDescription(csv,List(),List(FileD
                                                  //| escription(data-002.csv,<function1>), FileDescription(data-001.csv,<function
                                                  //| 1>)))),List(FileDescription(README,<function1>)))
              
  d.list()                                        //> /tmp/acme
                                                  //| /tmp/acme/csv
                                                  //| /tmp/acme/csv/data-002.csv
                                                  //| /tmp/acme/csv/data-001.csv
                                                  //| /tmp/acme/README
}