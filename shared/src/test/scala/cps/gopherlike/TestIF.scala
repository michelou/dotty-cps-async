package cps.gopherlike

import org.junit.{Test,Ignore}
import org.junit.Assert._

import scala.quoted._
import scala.util.Success

import cps._

class TestIF:

  @Test def reproduce(): Unit =
     //implicit val printCode = cps.macroFlags.PrintCode
     //implicit val printTree = cps.macroFlags.PrintTree
     //implicit val debugLevel = cps.macroFlags.DebugLevel(20)
     val writer = IFWriter[ComputationBound,Int]()
     val reader = IFReader[ComputationBound,Int](10)
     val c = async{
        // compiler crash:
        //     https://github.com/lampepfl/dotty/issues/10910
        reader.foreach{
           a => writer.write(a)
        }
        //await(reader.aforeach{
        //   a => writer.write(a)
        //})
        //reader.aforeach{
        //   a => writer.write(a)
        //}
        // happy path
        //reader.aforeach{
        //     a => await(writer.awrite(a))
        //}
        writer.v
     }
     assert(c.run() == Success(10))
