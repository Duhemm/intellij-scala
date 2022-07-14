package org.jetbrains.plugins.scala
package annotator
package quickfix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScTemplateDefinition
import org.jetbrains.plugins.scala.overrideImplement.ScalaOIUtil

/**
 * User: Alexander Podkhalyuzin
 * Date: 22.09.2008
 */

final class ImplementMembersQuickFix(clazz: ScTemplateDefinition) extends IntentionAction {
  override def getText: String = ScalaBundle.message("implement.members.fix")
  override def startInWriteAction: Boolean = false

  override def isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean =
    clazz.isValid && file.isWritable

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit =
    ScalaOIUtil.invokeOverrideImplement(file, isImplement = true)(project, editor)

  override def getFamilyName: String = ScalaBundle.message("implement.members.fix")
}
