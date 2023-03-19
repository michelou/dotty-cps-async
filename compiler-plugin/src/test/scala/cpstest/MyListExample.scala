package cpstest

import cps._

sealed trait MyList[+A] {

  def map[B](f: A=>B ): MyList[B]

  def map_async[B,F[_]](m: CpsMonad[F])(f: A=>F[B] ): F[List[B]] = ???

}


case object MyNil extends MyList[Nothing] {

  def map[B](f: Nothing=>B ): MyList[B] = MyNil

//  def map_async[B,F[_]](m: CpsMonad[F])(f: Nothing => B): m.pure(MyNil)

}

case class MyCons[A](head:A, tail:MyList[A]) extends MyList[A]{

  def map[B](f: A=>B ): MyList[B] = {
    MyCons(f(head), tail.map(f))
  }

  //def map_async[B](m: CpsMonad[F])(f: A => F[B]): F[List[B]] = async[F]{
  //  MyCons(await(f(head)), await(tail.map_asyn(f)) )
  //}

}

import scala.concurrent.*
import cps._
import cps.monads.{*,given}

/*
Monad {

  def pure
  def map

}
*/

object MyListExample {

  object network {
    def fetch(x:String): CpsMonadContext[Future] ?=> String =
      ???
  }

  //def myFunction(l:MyList[String]): Future[List[String]] = async[Future] {
  //   val otherList: List[Int] = l.map(url => await(network.fetch(url)))
  //   println(otherList)
  //}

  def myFunction(l: MyList[String]): CpsMonadContext[Future] ?=> MyList[String] =
  {
    val otherList: MyList[String] = l.map(url => network.fetch(url) )
    println(otherList)
    otherList
  }




}

//  EffectLike[Effects] =  [T] =>> EffectLikeM[Effects,T]
//
