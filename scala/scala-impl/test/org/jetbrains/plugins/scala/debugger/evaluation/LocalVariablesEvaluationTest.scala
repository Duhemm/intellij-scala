package org.jetbrains.plugins.scala
package debugger
package evaluation

import org.junit.experimental.categories.Category

@Category(Array(classOf[DebuggerTests]))
class LocalVariablesEvaluationTest_2_11 extends LocalVariablesEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_11
}

@Category(Array(classOf[DebuggerTests]))
class LocalVariablesEvaluationTest_2_12 extends LocalVariablesEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_12
}

@Category(Array(classOf[DebuggerTests]))
class LocalVariablesEvaluationTest_2_13 extends LocalVariablesEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_13
}

@Category(Array(classOf[DebuggerTests]))
class LocalVariablesEvaluationTest_3_0 extends LocalVariablesEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_0

  override def testLocalObjectOuter(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      failing(evalStartsWith("x", "LocalObjectOuter$x"))
    }
  }

  override def testLocalMethodLocalObject(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      failing(evalStartsWith("x", "LocalMethodLocalObject$x"))
    }
  }
}

@Category(Array(classOf[DebuggerTests]))
class LocalVariablesEvaluationTest_3_1 extends LocalVariablesEvaluationTest_3_0 {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_1
}

abstract class LocalVariablesEvaluationTestBase extends ExpressionEvaluationTestBase {
  addSourceFile("InMethod.scala",
    s"""object InMethod {
       |  class A { override def toString: String = "instance of A" }
       |  case class B() { override def toString: String = "instance of B" }
       |  trait C { override def toString: String = "instance of C" }
       |  object D { override def toString: String = "object D" }
       |  class V(private val n: Int) extends AnyVal { override def toString: String = s"V($$n)" }
       |
       |  def main(args: Array[String]): Unit = {
       |    val boolean = true
       |    val byte = 1.toByte
       |    val char = 'a'
       |    val double = 2.0
       |    val float = 3.0f
       |    val integer = 4
       |    val long = 5L
       |    val short = 6.toShort
       |    val string = "abc"
       |    val aInstance = new A()
       |    val bInstance = B()
       |    val cInstance = new C {}
       |    val d = D
       |    val value = new V(5)
       |    println() $breakpoint
       |  }
       |}
     """.stripMargin.trim
  )

  def testInMethod(): Unit = expressionEvaluationTest() { implicit ctx =>
    "boolean" evaluatesTo true
    "byte" evaluatesTo 1.toByte
    "char" evaluatesTo 'a'
    "double" evaluatesTo 2.0
    "float" evaluatesTo 3.0f
    "integer" evaluatesTo 4
    "long" evaluatesTo 5L
    "short" evaluatesTo 6.toShort
    "string" evaluatesTo "abc"
    "aInstance" evaluatesTo "instance of A"
    "bInstance" evaluatesTo "instance of B"
    "cInstance" evaluatesTo "instance of C"
    "d" evaluatesTo "object D"
    "value" evaluatesTo "V(5)"
  }

  addSourceFile("InLambda.scala",
    s"""object InLambda {
       |  class A { override def toString: String = "instance of A" }
       |  case class B() { override def toString: String = "instance of B" }
       |  trait C { override def toString: String = "instance of C" }
       |  object D { override def toString: String = "object D" }
       |  class V(private val n: Int) extends AnyVal { override def toString: String = s"V($$n)" }
       |
       |  def main(args: Array[String]): Unit = {
       |    Array(1).foreach { _ =>
       |      val boolean = true
       |      val byte = 1.toByte
       |      val char = 'a'
       |      val double = 2.0
       |      val float = 3.0f
       |      val integer = 4
       |      val long = 5L
       |      val short = 6.toShort
       |      val string = "abc"
       |      val aInstance = new A()
       |      val bInstance = B()
       |      val cInstance = new C {}
       |      val d = D
       |      val value = new V(5)
       |      println() $breakpoint
       |    }
       |  }
       |}
     """.stripMargin.trim
  )

  def testInLambda(): Unit = expressionEvaluationTest() { implicit ctx =>
    "boolean" evaluatesTo true
    "byte" evaluatesTo 1.toByte
    "char" evaluatesTo 'a'
    "double" evaluatesTo 2.0
    "float" evaluatesTo 3.0f
    "integer" evaluatesTo 4
    "long" evaluatesTo 5L
    "short" evaluatesTo 6.toShort
    "string" evaluatesTo "abc"
    "aInstance" evaluatesTo "instance of A"
    "bInstance" evaluatesTo "instance of B"
    "cInstance" evaluatesTo "instance of C"
    "d" evaluatesTo "object D"
    "value" evaluatesTo "V(5)"
  }

  addSourceFile("LocalClassParam.scala",
    s"""
       |class LocalClassParam(x: Int) {
       |  val h = x
       |  def foo(): Unit = {
       |    val y = () => {
       |      println() $breakpoint
       |      1 + 2 + x
       |    }
       |    y()
       |  }
       |}
       |object LocalClassParam {
       |  def main(args: Array[String]): Unit = {
       |    val a = new LocalClassParam(1)
       |    a.foo()
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalClassParam(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
    }
  }

  addSourceFile("ClassParamInConstructor.scala",
    s"""
       |class ClassParamInConstructor(
       |  unused: Int,
       |  used: Int,
       |  val field: Int
       |) {
       |  println(s"in constructor $$used") $breakpoint
       |}
       |object ClassParamInConstructor {
       |  def main(args: Array[String]): Unit = {
       |    val a = new ClassParamInConstructor(1, 2, 3)
       |  }
       |}
       |""".stripMargin)

  def testClassParamInConstructor(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("unused", "1")
      evalEquals("used", "2")
      evalEquals("field", "3")
    }
  }

  addSourceFile("BackingFieldParamInMethod.scala",
    s"""
       |class BackingFieldParamInMethod(
       |    backingField: Int
       |) {
       |  def foo: Int = backingField $breakpoint
       |}
       |object BackingFieldParamInMethod {
       |  def main(args: Array[String]): Unit = {
       |    val a = new BackingFieldParamInMethod(1)
       |    a.foo
       |  }
       |}
       |""".stripMargin)

  def testBackingFieldParamInMethod(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("backingField", "1")
    }
  }

  addSourceFile("BackingFieldParamInConstructor.scala",
    s"""
       |class BackingFieldParamInConstructor(
       |    backingField: Int
       |) {
       |  println("in constructor") $breakpoint
       |
       |  def foo: Int = backingField
       |}
       |object BackingFieldParamInConstructor {
       |  def main(args: Array[String]): Unit = {
       |    val a = new BackingFieldParamInConstructor(1)
       |    a.foo
       |  }
       |}
       |""".stripMargin)

  def testBackingFieldParamInConstructor(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("backingField", "1")
    }
  }

  addSourceFile("NoBackingFieldParam.scala",
    s"""
       |class NoBackingFieldParam(
       |    noBackingField: Int
       |) {
       |  def foo: Int = 5 $breakpoint
       |}
       |object NoBackingFieldParam {
       |  def main(args: Array[String]): Unit = {
       |    val a = new NoBackingFieldParam(1)
       |    a.foo
       |  }
       |}
       |""".stripMargin)

  def testNoBackingFieldParam(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalFailsWith("noBackingField", "constructor parameter 'noBackingField' is inaccessible outside of the class constructor")
    }
  }

  addSourceFile("LocalFromForStatement.scala",
    s"""
       |object LocalFromForStatement {
       |  def main(args: Array[String]): Unit = {
       |    val x = 1
       |    for (i <- 1 to 1) {
       |      x
       |      println() $breakpoint
       |    }
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalFromForStatement(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
    }
  }

  addSourceFile("LocalFromForStmtFromOut.scala",
    s"""
       |object LocalFromForStmtFromOut {
       |  def main(args: Array[String]): Unit = {
       |    val x = 1
       |    for (i <- 1 to 1) {
       |      x
       |      println() $breakpoint
       |    }
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalFromForStmtFromOut(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
    }
  }

  addSourceFile("Param.scala",
    s"""
       |object Param {
       |  def foo(x: Int): Unit = {
       |    println() $breakpoint
       |  }
       |
       |  def main(args: Array[String]): Unit = {
       |    val x = 0
       |    foo(x + 1)
       |  }
       |}
      """.stripMargin.trim
  )

  def testParam(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
    }
  }

  addSourceFile("LocalParam.scala",
    s"""
       |object LocalParam {
       |  def main(args: Array[String]): Unit = {
       |    def foo(x: Int): Unit = {
       |      println() $breakpoint
       |    }
       |    foo(1)
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalParam(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
    }
  }

  addSourceFile("LocalOuter.scala",
    s"""
       |object LocalOuter {
       |  def main(args: Array[String]): Unit = {
       |    val x = 1
       |    val runnable = new Runnable {
       |      def run(): Unit = {
       |        x
       |        println() $breakpoint
       |      }
       |    }
       |    runnable.run()
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalOuter(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
    }
  }

  addSourceFile("LocalOuterOuter.scala",
    s"""
       |object LocalOuterOuter {
       |  def main(args: Array[String]): Unit = {
       |    val x = 1
       |    var y = "a"
       |    val runnable = new Runnable {
       |      def run(): Unit = {
       |        val runnable = new Runnable {
       |          def run(): Unit = {
       |            x
       |            println() $breakpoint
       |          }
       |        }
       |        runnable.run()
       |      }
       |    }
       |    runnable.run()
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalOuterOuter(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
      evalEquals("y", "a")
    }
  }

  addSourceFile("LocalObjectOuter.scala",
    s"""
       |object LocalObjectOuter {
       |  def main(args: Array[String]): Unit = {
       |    object x {}
       |    val runnable = new Runnable {
       |      def run(): Unit = {
       |        val runnable = new Runnable {
       |          def run(): Unit = {
       |            val outer = x
       |            println(outer) $breakpoint
       |          }
       |        }
       |        runnable.run()
       |      }
       |    }
       |    runnable.run()
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalObjectOuter(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalStartsWith("x", "LocalObjectOuter$x")
    }
  }

  addSourceFile("LocalFromClosureAndClass.scala",
    s"""
       |object LocalFromClosureAndClass {
       |  def main(args: Array[String]): Unit = {
       |    val x = 1
       |    var y = "a"
       |    val runnable = new Runnable {
       |      def run(): Unit = {
       |        val foo = () => {
       |          val runnable = new Runnable {
       |            def run(): Unit = {
       |              x
       |              println() $breakpoint
       |            }
       |          }
       |          runnable.run()
       |        }
       |        foo()
       |      }
       |    }
       |    runnable.run()
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalFromClosureAndClass(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
      evalEquals("y", "a")
    }
  }

  addSourceFile("LocalMethodLocal.scala",
    s"""
       |object LocalMethodLocal {
       |  def main(args: Array[String]): Unit = {
       |    val x: Int = 1
       |    var s = "a"
       |    def foo(y: Int): Unit = {
       |      println() $breakpoint
       |      x
       |    }
       |    foo(2)
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalMethodLocal(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
      evalEquals("s", "a")
    }
  }

  addSourceFile("LocalMethodLocalObject.scala",
    s"""
       |object LocalMethodLocalObject {
       |  def main(args: Array[String]): Unit = {
       |    object x
       |    def foo(y: Int): Unit = {
       |      val outer = x
       |      println(outer) $breakpoint
       |    }
       |    foo(2)
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalMethodLocalObject(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalStartsWith("x", "LocalMethodLocalObject$x")
    }
  }

  addSourceFile("LocalMethodLocalMethodLocal.scala",
    s"""
       |object LocalMethodLocalMethodLocal {
       |  def main(args: Array[String]): Unit = {
       |    val x = 1
       |    var s = "a"
       |    def foo(y: Int): Unit = {
       |      def foo(y: Int): Unit = {
       |        println() $breakpoint
       |         x
       |      }
       |      foo(y)
       |    }
       |    foo(2)
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalMethodLocalMethodLocal(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
      evalEquals("s", "a")
    }
  }

  addSourceFile("LocalMethodLocalMethodLocalClass.scala",
    s"""
       |object LocalMethodLocalMethodLocalClass {
       |  def main(args: Array[String]): Unit = {
       |    val x = 1
       |    var s = "a"
       |    def foo(y: Int): Unit = {
       |      def foo(y: Int): Unit = {
       |        class A {
       |          def foo(): Unit = {
       |           println() $breakpoint
       |            s + x
       |          }
       |        }
       |        new A().foo()
       |      }
       |      foo(y)
       |    }
       |    foo(2)
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalMethodLocalMethodLocalClass(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
      evalEquals("s", "a")
    }
  }

  addSourceFile("LocMethLocMethLocClassLocMeth.scala",
    s"""
       |object LocMethLocMethLocClassLocMeth {
       |  def main(args: Array[String]): Unit = {
       |    val x = 1
       |    def foo(y: Int): Unit = {
       |      def foo(y: Int): Unit = {
       |        class A {
       |          def foo(): Unit = {
       |            class B {
       |              def foo(): Unit = {
       |                def goo(y: Int): Unit = {
       |                  println() $breakpoint
       |                  x
       |                }
       |                goo(x + 1)
       |              }
       |            }
       |            new B().foo()
       |          }
       |        }
       |        new A().foo()
       |      }
       |      foo(y)
       |    }
       |    foo(2)
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocMethLocMethLocClassLocMeth(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
    }
  }

  addSourceFile("LocalObjectInside.scala",
    s"""
       |object LocalObjectInside {
       |  def main(args: Array[String]): Unit = {
       |    val x = 1
       |    object X {
       |      def foo(y: Int): Unit = {
       |        println() $breakpoint
       |         x
       |      }
       |    }
       |    X.foo(2)
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalObjectInside(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
    }
  }

  addSourceFile("LocalObjectInsideClassLevel.scala",
    s"""
       |object LocalObjectInsideClassLevel {
       |  def main(args: Array[String]): Unit = {
       |    class Local {
       |      def foo(): Unit = {
       |        val x = 1
       |        var s = "a"
       |        object X {
       |          def foo(y: Int): Unit = {
       |            println() $breakpoint
       |             x
       |          }
       |        }
       |        X.foo(2)
       |      }
       |    }
       |    new Local().foo()
       |  }
       |}
      """.stripMargin.trim
  )

  def testLocalObjectInsideClassLevel(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("x", "1")
      evalEquals("s", "a")
    }
  }

  addSourceFile("LocalUseNamedArgs.scala",
    s"""
       |object LocalUseNamedArgs {
       |  def main(args: Array[String]): Unit = {
       |    val j = 2
       |
       |    def inner(z: Int = 1) = bar(i = z, j = j)
       |    def inner2() = bar(i = 1, j = j )
       |
       |    println() $breakpoint
       |  }
       |
       |  def bar(i: Int, j: Int): Int = i + j
       |}
      """.stripMargin.trim
  )

  def testLocalUseNamedArgs(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalEquals("inner()", "3")
      evalEquals("inner(2)", "4")
      evalEquals("inner2()", "3")
      evalEquals("inner2", "3")
    }
  }
}
