package cps.forest

import scala.quoted._
import scala.quoted.matching._

import cps._


class TypedTransform[F[_]:Type,T:Type](cpsCtx: TransformationContext[F,T]):

  import cpsCtx._

  // case Apply(fun,args) 
  def run(using qctx: QuoteContext)(t: qctx.tasty.Term, tp: qctx.tasty.TypeTree): CpsExpr[F,T] =
     import qctx.tasty.{_, given _}
     val r = Async.nestTransform(t.seal.asInstanceOf[Expr[T]], cpsCtx, "T")
     if (!r.isAsync) 
       CpsExpr.sync(asyncMonad, patternCode)
     else
       // TODO:  create Typed with F[$tp] as type ?
       r
  

