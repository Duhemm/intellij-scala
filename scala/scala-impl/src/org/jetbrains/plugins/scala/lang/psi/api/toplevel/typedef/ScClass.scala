package org.jetbrains.plugins.scala
package lang
package psi
package api
package toplevel
package typedef

import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunction

/**
* @author Alexander Podkhalyuzin
* Date: 20.02.2008
*/
trait ScClass extends ScTypeDefinition with ScConstructorOwner {

  def typeParamString: String = typeParameters
    .map(ScalaPsiUtil.typeParamString(_)) match {
    case Seq() => ""
    case seq => seq.mkString("[", ", ", "]")
  }

  def tooBigForUnapply: Boolean = constructor.exists(_.parameters.length > 22)

  def getSyntheticImplicitMethod: Option[ScFunction]

  def getClassToken: PsiElement = findFirstChildByType(ScalaTokenTypes.kCLASS)

  def getObjectClassOrTraitToken: PsiElement = getClassToken
}
