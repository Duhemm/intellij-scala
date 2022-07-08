package org.jetbrains.plugins.scala
package lang
package psi
package stubs

import com.intellij.psi.stubs.StubElement
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.imports.ScImportSelectors

trait ScImportSelectorsStub extends StubElement[ScImportSelectors] {
  def hasWildcard: Boolean
}