package cps.stream

import org.junit.{Test,Ignore}
import org.junit.Assert._

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global


import cps.*
import cps.stream.*
import cps.monads.{*, given}

import cps.util.FutureCompleter

class TestAsyncIteratorOps {

    
    @Test  def testSimpleMap() = {
        val stream = AsyncList.iterate[Future,Int](1 to 10)
        val iterator = stream.iterator
        val mappedIterator = iterator.map(x => x.toString)
        val f = async[Future] {
          var  i = 1
          while{
            await(mappedIterator.next) match
              case Some(x) => 
                  assert(i.toString == x)
                  i = i + 1
                  true
              case None =>   
                  assert(i == 11)
                  false
          } do ()
        } 
        FutureCompleter(f)
    }

    @Test  def testSimpleMapAsync() = {
      val stream = AsyncList.iterate[Future,Int](1 to 10)
      val iterator = stream.iterator
      val f = async[Future] {
        val mappedIterator = iterator.map(x => await(Future successful x.toString))
        var  i = 1
        while{
          await(mappedIterator.next) match
            case Some(x) => 
                assert(i.toString == x)
                i = i + 1
                true
            case None =>   
                assert(i == 11)
                false
        } do ()
      } 
      FutureCompleter(f)
    }

    @Test def testSimpleFilter() = {
      val stream = AsyncList.iterate[Future,Int](1 to 10)
      val iterator = stream.iterator
      val filtered = iterator.filter(_ % 2 == 0)
      val f = async[Future] {
        var i = 0
        while{
          val optValue = await(filtered.next)
          optValue.foreach{ x =>
            assert(x % 2 == 0)
            i = i + 1
          }
          optValue.isDefined
        } do ()
        assert(i == 5)
      }
      FutureCompleter(f)
    }

    @Test def testSimpleFilterAsync() = {
      val stream = AsyncList.iterate[Future,Int](1 to 10)
      val iterator = stream.iterator
      val f = async[Future] {
        val filtered = iterator.filter(_ % 2 == await(Future successful 0) )
        var i = 0
        while{
          val optValue = await(filtered.next)
          optValue.foreach{ x =>
            assert(x % 2 == 0)
            i = i + 1
          }
          optValue.isDefined
        } do ()
        assert(i == 5)
      }
      FutureCompleter(f)
    }

    @Test def testFindPos() = {
      val stream = AsyncList.iterate[Future,Int](1 to 10)
      val iterator = stream.iterator
      val f = async[Future] {
        val x = await(iterator.find(_ > 8))
        assert(x == Some(9))
      }
      FutureCompleter(f)
    }

    @Test def testFindNeg() = {
      val stream = AsyncList.iterate[Future,Int](1 to 10)
      val iterator = stream.iterator
      val f = async[Future] {
        val x = await(iterator.find(_ > 800))
        assert(x == None)
      }
      FutureCompleter(f)
    }

    @Test def testFindAsycPos() = {
      val stream = AsyncList.iterate[Future,Int](1 to 10)
      val iterator = stream.iterator
      //implicit val printCode = cps.macros.flags.PrintCode
      //implicit val debugLevel = cps.macros.flags.DebugLevel(20)
      val f = async[Future] {
        val x = await(iterator.find(_ > await(Future successful 8)))
        assert(x == Some(9))
      }
      FutureCompleter(f)
    }

    @Test def testFold(): Unit = {
      val stream = AsyncList.iterate[Future,Int](1 to 3)
      val iterator = stream.iterator
      val f = async[Future]{
           val fr = iterator.fold(0)(
                    (x:Int,y:Int) => x + y + 1
           )
           val r = await(fr)
           assert(r == 9)
      }
      FutureCompleter(f)
    }


    @Test def testFoldAsync(): Unit = {
      val stream = AsyncList.iterate[Future,Int](1 to 3)
      val iterator = stream.iterator
      val f = async[Future]{
           val fr = iterator.fold(0)(
                    (x:Int,y:Int) => x + y + await(Future.successful(1)) 
           )
           val r = await(fr)
           assert(r == 9)
      }
      FutureCompleter(f)
    }

    @Test def testScan(): Unit = {
      val stream = AsyncList.iterate[Future,Int](1 to 5)
      val iterator = stream.iterator
      val ft = async[Future]{
           val fr = iterator.scan(0)( (x,y) => x + y )
           val r0 = await(fr.next)
           assert(r0.get == 0)
           val r1 = await(fr.next)
           assert(r1.get == 1)
           val r2 = await(fr.next)
           assert(r2.get == 3)
           val r3 = await(fr.next)
           assert(r3.get == 6)
           val r4 = await(fr.next)
           assert(r4.get == 10)
           val r5 = await(fr.next)
           assert(r5.get == 15)
           val r6 = await(fr.next)
           assert(r6 == None)
      }
      FutureCompleter(ft)
    }

    
    @Test def testScanAsync(): Unit = {
      val stream = AsyncList.iterate[Future,Int](1 to 5)
      val iterator = stream.iterator
      val ft = async[Future]{
           val fr = iterator.scan(0)( (x,y) => x + await(Future.successful(y)) )
           val r0 = await(fr.next)
           assert(r0.get == 0)
           val r1 = await(fr.next)
           assert(r1.get == 1)
           val r2 = await(fr.next)
           assert(r2.get == 3)
           val r3 = await(fr.next)
           assert(r3.get == 6)
           val r4 = await(fr.next)
           assert(r4.get == 10)
           val r5 = await(fr.next)
           assert(r5.get == 15)
           val r6 = await(fr.next)
           assert(r6 == None)
      }
      FutureCompleter(ft)
    }

}