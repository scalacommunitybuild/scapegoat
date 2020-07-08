package com.sksamuel.scapegoat

import java.io.{File, FileNotFoundException}
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files

import scala.tools.nsc.reporters.ConsoleReporter

/**
 * @author Stephen Samuel
 */
trait PluginRunner {

  val settings = {
    val s = new scala.tools.nsc.Settings
    for (_ <- Option(System.getProperty("printphases"))) {
      s.Xprint.value = List("all")
      s.Yrangepos.value = true
      s.Yposdebug.value = true
    }
    s.stopAfter.value = List("refchecks") // no need to go all the way to generating classfiles
    s.usejavacp.value = true
    s.feature.value = true
    s
  }

  val inspections: Seq[Inspection]
  val reporter = new ConsoleReporter(settings)
  lazy val compiler = new ScapegoatCompiler(settings, inspections, reporter)

  def writeCodeSnippetToTempFile(code: String): File = {
    val file = Files
      .write(Files.createTempFile("scapegoat_snippet", ".scala"), code.getBytes(StandardCharsets.UTF_8))
      .toFile
    file.deleteOnExit()
    file
  }

  def compileCodeSnippet(code: String): ScapegoatCompiler =
    compileSourceFiles(writeCodeSnippetToTempFile(code))
  def compileSourceResources(urls: URL*): ScapegoatCompiler =
    compileSourceFiles(urls.map(_.getFile).map(new File(_)): _*)
  def compileSourceFiles(files: File*): ScapegoatCompiler = {
    reporter.flush()
    val command = new scala.tools.nsc.CompilerCommand(files.map(_.getAbsolutePath).toList, settings)
    new compiler.Run().compile(command.files)
    compiler
  }
}

class ScapegoatCompiler(
  settings: scala.tools.nsc.Settings,
  inspections: Seq[Inspection],
  reporter: ConsoleReporter
) extends scala.tools.nsc.Global(settings, reporter) {

  val scapegoat = new ScapegoatComponent(this, inspections)
  scapegoat.disableHTML = true
  scapegoat.disableXML = true
  scapegoat.disableScalastyleXML = true
  scapegoat.verbose = false
  scapegoat.summary = false

  override def computeInternalPhases(): Unit = {
    super.computeInternalPhases()
    phasesSet.add(scapegoat)
    phasesDescMap.put(scapegoat, "scapegoat")
  }
}
