package com.lightbend.sbt.jpms

import sbt.{AutoPlugin, Compile, Def, ModuleID, Setting, Test, inConfig, settingKey, _}
import sbt.Keys._

object JpmsPlugin extends AutoPlugin{
  private val JpmsNameKey = "jpmsName"
  object autoImport {
    implicit class RichModuleId(private val self: ModuleID) {
      def jpmsName(s: String) = self.withExtraAttributes(self.extraAttributes.updated(JpmsNameKey, s))
    }

    val jpmsModuleName = settingKey[String]("JPMS module name")
    val jpmsLibraryDependencyName = settingKey[Seq[(ModuleID, String)]]("JPMS module name of dependencies")
    val jpmsPatchTest = settingKey[Boolean]("Test sources should be patched into the compile module")
  }

  import autoImport._

  override def trigger = super.trigger

  override lazy val projectSettings: Seq[Setting[_]] = commonSettings

  def commonConfigSettings: Seq[Setting[_]] = Seq(
    javaSource := {
      jpmsModuleName.?.value match {
        case Some(modName) => (javaSource.value / modName)
        case _ => javaSource.value
      }
    },
    javacOptions := overwriteModulePath(dependencyClasspath.value.map(_.data))(javacOptions.value),
    javaOptions := overwriteModulePath(dependencyClasspath.value.map(_.data) ++ (if (configuration.value != Test || !jpmsPatchTest.??(false).value) List(classDirectory.value) else Nil) )(javaOptions.value),
    // apparently this needs to follow `--patch-module`: https://gist.github.com/d696b44b817e6fd0b2e457ad805e62cb
    javaOptions := {
      val main = mainClass.value.getOrElse("")
      jpmsModuleName.?.value match {
        case Some(modName) =>
          val modAndMainClass = modName + "/" + main
          overwriteOption("--module", modAndMainClass, moveToEnd = true)(javaOptions.value)
        case None => javaOptions.value
      }
    }
  )

  private def patchModule(isJavaOptions: Boolean) = Def.task {
    val modNameOpt = jpmsModuleName.?.value
    val javaSourceValue = (javaSource in Test).value
    val classDirectoryValue = (classDirectory in Test).value
    val target = if (isJavaOptions) classDirectoryValue else javaSourceValue
    if (jpmsPatchTest.??(false).value) {
      modNameOpt match {
        case Some(moduleName) =>
          List(
            "--patch-module", moduleName + "=" + target.toString
          )
        case _ => Nil
      }
    } else Nil
  }

  def patchModuleSettings: Seq[Setting[_]] = Seq(
    javacOptions in Test ++= patchModule(isJavaOptions = false).value,
    javaOptions in Test ++= patchModule(isJavaOptions = true).value
  )

  private def testAddModulesAddReads(isJavaOptions: Boolean): Def.Initialize[Task[Seq[String]]] = Def.task  {
    val compileDeps: Seq[Attributed[File]] = (dependencyClasspath in Compile).value
    val compileFileSet = compileDeps.iterator.map(_.data)
    val testDeps = (dependencyClasspath in Test).value
    val testOnlyDeps = testDeps.filterNot(dep => compileFileSet.contains(dep.data))
    val map: Map[(String, String), String] = jpmsLibraryDependencyName.value.iterator.map(it => ((it._1.organization, it._1.name), it._2)).toMap
    val testOnlyJpmsNames: Seq[String] = testOnlyDeps.flatMap(dep => dep.get(moduleID.key).flatMap((m: ModuleID) => map.get((m.organization, m.name))))
    val testModuleName = (jpmsModuleName in Test).?.value
    val compileModuleName = (jpmsModuleName in Compile).?.value
    if (testOnlyJpmsNames.isEmpty) Nil
    else {
      val jpmsModulePath = testOnlyJpmsNames.mkString(java.io.File.pathSeparator)
      val addModules = List("--add-modules", if (isJavaOptions) "ALL-MODULE-PATH" else testOnlyJpmsNames.mkString(","))
      val addReads = (testModuleName, compileModuleName) match {
        case (Some(t), Some(c)) if t == c =>
          testOnlyJpmsNames.distinct.flatMap(name => List("--add-reads", c + "=" + name))
        case _ =>
          Nil
      }
      addModules ++ addReads
    }
  }

  def testModuleSettings: Seq[Setting[_]] = Seq(
    javacOptions in Test ++= testAddModulesAddReads(isJavaOptions = false).value,
    javaOptions in Test ++= testAddModulesAddReads(isJavaOptions = true).value
  ) ++ patchModuleSettings

  def commonSettings: Seq[Setting[_]] = Seq(
    jpmsLibraryDependencyName := {
      val deps = libraryDependencies.value
      val extra = libraryDependencies.value.map(dep => (dep, dep.extraAttributes.get(JpmsNameKey)))
      extra.collect { case (x, Some(y)) => (x, y)}
    },
    jpmsPatchTest := {
      val testModuleName = (jpmsModuleName in Test).?.value
      val compileModuleName = (jpmsModuleName in Compile).?.value
      (testModuleName, compileModuleName) match {
        case (Some(t), Some(c)) if t == c => true
        case _ => false
      }
    }
  ) ++ (
    testModuleSettings ++ Seq(Compile, Test).flatMap(scope => inConfig(scope)(commonConfigSettings))
  )

  def overwriteModulePath(modulePath: Seq[File])(options: Seq[String]): Seq[String] = {
    val modPathString = modulePath.map(_.getAbsolutePath).mkString(java.io.File.pathSeparator)
    overwriteOption("--module-path", modPathString)(options)
  }
  def overwriteOption(option: String, value: String, moveToEnd: Boolean = false)(options: Seq[String]): Seq[String] = {
    val index = options.indexWhere(_ == option)
    if (index == -1) options ++ List(option, value)
    else if (moveToEnd) options.patch(index, Nil, 2) ++ List(option, value)
    else options.patch(index + 1, List(value), 1)
  }
}

