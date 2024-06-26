package cps

import org.junit.{Test,Ignore}
import org.junit.Assert.*

import scala.quoted.*
import scala.util.*
import scala.util.*
import scala.concurrent.*
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext.Implicits.global


import cps.monads.given
import cps.plugin.annotation.*
import cps.macros.flags.UseCompilerPlugin
given UseCompilerPlugin.type = UseCompilerPlugin



class TestFutureRangeWithFilterShiftM7min1:


  /**
   * we need special handling for withFilter.
   **/
  @Test def testWithFilterForeachWrapper() = 
     //implicit val printCode = cps.macroFlags.PrintCode
     //implicit val debugLevel = cps.macroFlags.DebugLevel(20)
     val c = async[Future]{
        var s = 0
        for(i <- 1 to 4 if i%2 == 1) {
           s += await(Future successful 3) + i
        }
        s
        assert(s == 10)
     }
     Await.ready(c, 1.second)


