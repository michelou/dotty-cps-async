package cps.plugin.forest.application

import dotty.tools.dotc.*
import core.*
import core.Contexts.*
import core.Decorators.*
import core.Names.*
import core.Symbols.*
import core.Types.*
import ast.tpd.*

import cps.plugin.*
import cps.plugin.forest.*

enum ApplyArgCallMode {
  case SYNC, ASYNC, ASYNC_SHIFT
}

sealed trait ApplyArg {
    def name: TermName
    def tpe:  Type

    def isAsync: Boolean
    def isLambda: Boolean

    def optFlatMapsBeforCall: Seq[(CpsTree,ValDef)]
    def exprInCall(callMode: ApplyArgCallMode, optRuntimeAwait:Option[Tree]): Tree

    def dependencyFromLeft: Boolean
}

object ApplyArg {

  def apply(expr: Tree, index: Int, paramName: TermName, paramType: Type, isByName: Boolean, owner: Symbol, tctx: TransformationContext)(using Context): ApplyArg = {
    expr match
      case SeqLiteral(elems, elementtp) =>
        RepeatApplyArg(paramName, paramType, elems.map(p => RootTransform(p,owner,tctx) ))
      case _ =>
        val cpsExpr = RootTransform(expr, owner, tctx)
        if (isByName) then
          ByNameApplyArg(paramName, paramType, cpsExpr)
        else
          paramType match
            case AnnotatedType(tp, an) if an.symbol == defn.InlineParamAnnot =>
                InlineApplyArg(paramName,tp,cpsExpr)
            case AnnotatedType(tp, an) if an.symbol == defn.ErasedParamAnnot =>
                ErasedApplyArg(paramName,tp,cpsExpr)
            case _ =>
                // TODO: create val
                PlainApplyArg(paramName,paramType,cpsExpr)    
  }

}

sealed trait ExprApplyArg extends ApplyArg {

  def expr: CpsTree

  override def isAsync = expr.isAsync

  override def isLambda = expr.asyncKind match
                            case AsyncLambda(internal) => true
                            case _ => false

}

/**
 * Note, that sym is created only if expr is async
 * (i.e. unpure is empty)
 * Also AsyncLambda-s are 
 **/
case class PlainApplyArg(  
  override val name: TermName,
  override val tpe: Type,
  override val expr: CpsTree,  
  val optIdentValDef: Option[ValDef],
) extends ExprApplyArg  {

  override def flatMapsBeforeCall: Seq[(CpsTree,Symbol)] = {
    optIdentSym.map(sym => (expr,sym)).toSeq
  }

  /**
   *  Output the expression inside call
   *    this can 
   *
   *  //Are we change symbol when do changeOwner to tree ?
   *  If yes, we should be extremally careful with different refs to
   *  optIdentSym.  Maybe better do expr function from sym ?
   **/
   override def exprInCall(shifted: Boolean, optRuntimeAwait:Option[Tree]): Tree =
    import AsyncKind.*
    expr.asyncKind match
      case Sync => expr.unpure match
        case Some(tree) => tree
        case None => throw CpsTransformException("Impossibke: syn expression without unpure",expr.origin.span)
      case Async(_) => Ref(optIdentSym.get)
      case AsyncLambda(internal) =>
        if (shifted) then
          expr.transformed
        else
          optRuntimeAwait match
            case Some(runtimeAwait) =>
              val withRuntimeAwait = expr.applyRuntimeAwait(RuntiemAwaitMode)
            case None => 
              throw CpsTransformException(s"Can't transform function (both shioft and runtime-awaif for ${fType} are not found)", expr.origin.span)


}

case class RepeatApplyArg(
  override val name: TermName,
  override val tpe: Type,
  elements: Seq[ApplyArg]
) extends ApplyArg {

  override def isAsync = exprs.exists(_.isAsync)

  override def flatMapsBeforeCall = 
    elements.foldLeft(IndexedSeq.empty[(CpsTree,Symbol)]){ (s,e) =>
       s ++ e.flatMapsBeforeCall
    }

  override def exprInCall =
    val trees = elements.foldLeft(IndexedSeq.empty[Tree]){ (s,e) =>
      s.appended(e.exprInCall)
    }
    SeqLiteral(trees.toList)
}

case class ByNameApplyArg(
  override val name: TermName,
  override val tpe: Type,
  override val expr: CpsTree
) extends ExprApplyArg  {

  override def isLambda = true

  override def exprInCall(shifted: Boolean, optRuntimeAwait:Option[Tree]): Tree = ???

}



// potentially not used if we run after macros,
// but we can return 
case class InlineApplyArg(
  override val name: TermName,
  override val tpe: Type,
  override val expr: CpsTree
) extends ExprApplyArg

case class ErasedApplyArg(
  override val name: TermName,
  override val tpe: Type,
  override val expr: CpsTree
) extends ExprApplyArg
