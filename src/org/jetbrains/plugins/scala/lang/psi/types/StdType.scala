package org.jetbrains.plugins.scala
package lang
package psi
package types

import impl.toplevel.synthetic.{SyntheticClasses, ScSyntheticClass}
import com.intellij.openapi.project.Project
import util.CommonClassesSearcher
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiElement, PsiManager}


abstract case class StdType(name: String, tSuper: Option[StdType]) extends ValueType {
  /**
   * Return wrapped to option appropriate synthetic class.
   * In dumb mode returns None (or before it ends to register classes).
   * @param project in which project to find this class
   * @return If possible class to represent this type.
   */
  def asClass(project: Project): Option[ScSyntheticClass] = {
    if (SyntheticClasses.get(project).isClassesRegistered)
      Some(SyntheticClasses.get(project).byName(name).get)
    else None
  }

  override def equivInner(r: ScType, subst: ScUndefinedSubstitutor, falseUndef: Boolean): (Boolean, ScUndefinedSubstitutor) = {
    (this, r) match {
      case (l: StdType, _: StdType) => (l == r, subst)
      case (AnyRef, _) => {
        ScType.extractClass(r) match {
          case Some(clazz) if clazz.getQualifiedName == "java.lang.Object" => (true, subst)
          case _ => (false, subst)
        }
      }
      case (_, _) => {
        ScType.extractClass(r) match {
          case Some(clazz) if clazz.getQualifiedName == "scala." + name => (true, subst)
          case _ => (false, subst)
        }
      }
    }
  }
}

object StdType {
  val QualNameToType = Map(
    "scala.Any" -> Any,
    "scala.AnyRef" -> AnyRef,
    "scala.AnyVal" -> AnyVal,
    "scala.Unit" -> Unit,
    "scala.Boolean" -> Boolean,
    "scala.Byte" -> Byte,
    "scala.Short" -> Short,
    "scala.Char" -> Char,
    "scala.Int" -> Int,
    "scala.Long" -> Long,
    "scala.Double" -> Double,
    "scala.Null" -> Null,
    "scala.Nothing" -> Nothing,
    "scala.Singleton" -> Singleton
  )
}

trait ValueType extends ScType {
  def isValue = true

  def inferValueType: ValueType = this
}

case object Any extends StdType("Any", None)

case object Null extends StdType("Null", Some(AnyRef))

case object AnyRef extends StdType("AnyRef", Some(Any))

case object Nothing extends StdType("Nothing", Some(Any))

case object Singleton extends StdType("Singleton", Some(AnyRef))

case object AnyVal extends StdType("AnyVal", Some(Any)) {
  override def getValType: Option[StdType] = Some(this)
}

abstract case class ValType(override val name: String) extends StdType(name, Some(AnyVal)) {
  def apply(element: PsiElement): ScType = {
    apply(element.getManager, element.getResolveScope)
  }

  def apply(manager: PsiManager, scope: GlobalSearchScope): ScType = {
    val classes = CommonClassesSearcher.getCachedClass(manager, scope, "scala." + name)
    if (classes.length > 0) ScDesignatorType(classes(0))
    else this
  }

  override def getValType: Option[StdType] = Some(this)
}

object Unit extends ValType("Unit")

object Boolean extends ValType("Boolean")

object Char extends ValType("Char")

object Int extends ValType("Int")

object Long extends ValType("Long")

object Float extends ValType("Float")

object Double extends ValType("Double")

object Byte extends ValType("Byte")

object Short extends ValType("Short")
