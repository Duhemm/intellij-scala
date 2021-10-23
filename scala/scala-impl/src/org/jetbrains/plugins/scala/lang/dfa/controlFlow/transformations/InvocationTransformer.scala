package org.jetbrains.plugins.scala.lang.dfa.controlFlow.transformations

import com.intellij.psi.CommonClassNames
import org.jetbrains.plugins.scala.lang.dfa.analysis.framework.ScalaStatementAnchor
import org.jetbrains.plugins.scala.lang.dfa.analysis.invocations.ScalaInvocationInstruction
import org.jetbrains.plugins.scala.lang.dfa.analysis.invocations.specialSupport.CollectionAccessAssertions.addCollectionAccessAssertions
import org.jetbrains.plugins.scala.lang.dfa.analysis.invocations.specialSupport.SyntheticMethodsSpecialSupport.tryTransformSyntheticFunctionSpecially
import org.jetbrains.plugins.scala.lang.dfa.controlFlow.{ScalaDfaControlFlowBuilder, TransformationFailedException}
import org.jetbrains.plugins.scala.lang.dfa.invocationInfo.{InvocationInfo, InvokedElement}
import org.jetbrains.plugins.scala.lang.dfa.utils.ScalaDfaTypeConstants.SyntheticOperators.NumericBinary
import org.jetbrains.plugins.scala.lang.psi.api.expr._
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.synthetic.ScSyntheticFunction

class InvocationTransformer(val wrappedInvocation: ScExpression)
  extends ExpressionTransformer(wrappedInvocation) {

  override def toString: String = s"InvocationTransformer: $wrappedInvocation"

  override def transform(builder: ScalaDfaControlFlowBuilder): Unit = {
    val invocationsInfo = wrappedInvocation match {
      case methodCall: ScMethodCall => InvocationInfo.fromMethodCall(methodCall)
      case methodInvocation: MethodInvocation => List(InvocationInfo.fromMethodInvocation(methodInvocation))
      case referenceExpression: ScReferenceExpression => List(InvocationInfo.fromReferenceExpression(referenceExpression))
      case constructorInvocation: ScNewTemplateDefinition => List(InvocationInfo.fromConstructorInvocation(constructorInvocation))
      case _ => throw TransformationFailedException(wrappedInvocation, "Expression of this type cannot be transformed as an invocation.")
    }

    if (isUnsupportedInvocationExpression(wrappedInvocation)) {
      builder.pushUnknownCall(wrappedInvocation, 0)
    } else if (!tryTransformIntoSpecialRepresentation(invocationsInfo, builder)) {
      invocationsInfo.tail.foreach(invocation => {
        transformMethodInvocation(invocation, builder)
        builder.popReturnValue()
      })

      transformMethodInvocation(invocationsInfo.head, builder)
    }
  }

  private def isUnsupportedInvocationExpression(invocation: ScExpression): Boolean = invocation match {
    case infix: ScInfixExpr => isUnsupportedInfixSyntheticAssignment(infix.operation.getText)
    case _ => false
  }

  private def isUnsupportedInfixSyntheticAssignment(operation: String): Boolean = {
    // There were significant problems with recognizing and transforming properly combinations of synthetic methods
    // with var assignments, for example x += 3. Supporting it most likely needs modifications to relevant PSI elements.
    val isBinaryModifyingAssignment = operation.length == 2 && operation(1) == '='
    val isUnsupportedSyntheticOperator = NumericBinary.keys.toList.contains(operation(0)) ||
      operation(0) == '&' || operation(0) == '|'
    isBinaryModifyingAssignment && isUnsupportedSyntheticOperator
  }

  private def addAdditionalAssertions(invocationInfo: InvocationInfo, builder: ScalaDfaControlFlowBuilder): Unit = {
    addCollectionAccessAssertions(wrappedInvocation, invocationInfo, builder)
  }

  private def transformMethodInvocation(invocationInfo: InvocationInfo, builder: ScalaDfaControlFlowBuilder): Unit = {
    addAdditionalAssertions(invocationInfo, builder)

    val args = invocationInfo.argListsInEvaluationOrder.flatten
    args.foreach(_.content.transform(builder))

    val transfer = builder.maybeTransferValue(CommonClassNames.JAVA_LANG_THROWABLE)
    builder.addInstruction(new ScalaInvocationInstruction(invocationInfo,
      ScalaStatementAnchor(wrappedInvocation), transfer))
  }

  private def tryTransformIntoSpecialRepresentation(invocationsInfo: Seq[InvocationInfo], builder: ScalaDfaControlFlowBuilder): Boolean = {
    if (invocationsInfo.size > 1) return false
    val invocationInfo = invocationsInfo.head

    invocationInfo.invokedElement match {
      case Some(InvokedElement(psiElement)) => psiElement match {
        case function: ScSyntheticFunction =>
          tryTransformSyntheticFunctionSpecially(function, invocationInfo, wrappedInvocation, builder)
        case _ => false
      }
      case _ => false
    }
  }
}
