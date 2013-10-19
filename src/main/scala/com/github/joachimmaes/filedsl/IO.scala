package com.github.joachimmaes.filedsl

/**
 * @author Joachim Maes                                           
 */
object IO {
  /**
   * Fun with streams.  Definitely not an efficient implementation to copy all bytes from
   * one place to another. 
   */
  def copy(in: java.io.InputStream, out: java.io.OutputStream): Unit = {
    val fixedByteChunks: Stream[(Array[Byte], Int)] = Stream.continually {
      val CHUNK_SIZE = 2^10
      val bs = new Array[Byte](CHUNK_SIZE)
      val n = in.read(bs)
      (bs, n)
    }
    val untilDone: Stream[(Array[Byte], Int)] = fixedByteChunks.takeWhile {
      case (_, n) => n != -1
    }
    val bytes: Stream[Array[Byte]] = untilDone.map {
      case (bs, n) => bs.slice(0, n)
    }
    bytes.foreach { bs => out.write(bs) }  // shorter: bytes.foreach(out.write)
  }
}