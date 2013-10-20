package com.github.joachimmaes.filedsl

import scala.util.DynamicVariable


/**
 * File DSL that uses dynamic scopes by means of a mutable DynamicVariable.  It lacks type safety.
 *  
 * <pre>
 *   directory("/tmp/acme") {
 *     file("README", contents = "hello world")
 *     directory("csv") {
 *       file("data-001.csv", contents = "...")
 *       file("data-002.csv", contents = "...")
 *     }
 *   }  
 * </pre>
 *  
 * @author Joachim Maes                                           
 */
object DynamicFileDSL {
  case class DirectoryDescription(name: String, subdirs: List[DirectoryDescription], files: List[FileDescription]) {
    def create(): java.io.File = DynamicFileDSL.create(this)
    def list(): Unit = DynamicFileDSL.list(this)
  }

  case class FileDescription(name: String, contents: java.io.OutputStream=>Unit)

  /* directory block */
  private val dirContext: DynamicVariable[Option[DirectoryDescription]] = new DynamicVariable(None)
  
  private def addDirToDir(dir: DirectoryDescription) = {
    dirContext.value = dirContext.value.map { parent => parent.copy(subdirs = dir :: parent.subdirs) }
  }
  
  private def addFileToDir(file: FileDescription) = {
    dirContext.value = dirContext.value.map { parent => parent.copy(files = file :: parent.files) }
    file
  }
  
  def directory(name: String)(contents: =>Unit): DirectoryDescription = {
    val dir = dirContext.withValue(Some(DirectoryDescription(name, Nil, Nil))) {
      contents
      dirContext.value.get
    }
    addDirToDir(dir)
    dir
  }
  
  def tempDir(contents: =>Unit) = directory(new java.io.File(java.lang.System.getProperty("java.io.tmpdir")).getCanonicalPath())(contents)
  
  /* file block */
  private val fileContext: DynamicVariable[Option[FileDescription]] = new DynamicVariable(None)

  private def addContentToFile(contents: java.io.OutputStream=>Unit) = {
    fileContext.value = fileContext.value.map { file => file.copy(contents = { out => file.contents(out); contents(out) }) }
  }
  
  def file(name: String)(contents: =>Unit): FileDescription = {
    val file = fileContext.withValue(Some(FileDescription(name, { out => }))) {
      contents
      fileContext.value.get
    }
    addFileToDir(file)
  }
  
  def line(t: String): Unit = {
    addContentToFile { out => out.write(t.getBytes()); out.write('\n') }
  }
  
  def write(contents: java.io.OutputStream=>Unit): Unit = {
    addContentToFile(contents)
  }
 
  /* file methods */
  def file(name: String, contents: String): FileDescription = {
    addFileToDir(FileDescription(name, { out => out.write(contents.getBytes()) }))
  }
  
  def file(name: String, contents: Array[Byte]): FileDescription = {
    addFileToDir(FileDescription(name, { out => out.write(contents) }))
  }
  
  def file(name: String, contents: java.net.URL): FileDescription = {
    addFileToDir(FileDescription(name, { out => 
      val in = contents.openStream()
      try 
        IO.copy(in, out)
      finally 
        in.close()
    }))
  }
  
  def file(name: String, contents: scala.xml.Elem): FileDescription = {
    addFileToDir(FileDescription(name, { out => out.write(contents.toString().getBytes()) }))
  }
  
  /* operations */
  def list(dir: DirectoryDescription): Unit = {
    def NO_PARENT = { name: String => name }
    def WITH_PARENT(parent: String) = { name: String => parent + "/" + name } 
    
    def listDir(bind: String => String, dir: DirectoryDescription): Unit = {
      val path = bind(dir.name)
      println(path)
      dir.subdirs.foreach { subdir => listDir(WITH_PARENT(path), subdir) }
      dir.files.foreach { file => listFile(WITH_PARENT(path), file) }
    }
    
    def listFile(bind: String => String, file: FileDescription): Unit = {
      println(bind(file.name))
    }
    
    listDir(NO_PARENT, dir)
  }
  
  def create(dir: DirectoryDescription): java.io.File = {
    def NO_PARENT = { name: String => new java.io.File(name) }
    def WITH_PARENT(parent: java.io.File) = { name: String => new java.io.File(parent, name) } 
    
    def createDir(bind: String => java.io.File, dir: DirectoryDescription): java.io.File = {
      val path = bind(dir.name)
      path.mkdirs()
      dir.subdirs.foreach { subdir => createDir(WITH_PARENT(path), subdir) }
      dir.files.foreach { file => createFile(WITH_PARENT(path), file) }
      path
    }
  
    def createFile(bind: String => java.io.File, file: FileDescription): Unit = {
      val jfile = bind(file.name)
      val fos = new java.io.FileOutputStream(jfile)
      try
        file.contents(fos)
      finally
        fos.close()
    }
    
    createDir(NO_PARENT, dir)
  }
}
