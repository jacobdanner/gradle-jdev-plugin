import groovy.xml.*
import groovy.util.slurpersupport.*

import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.SourceSet

import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import java.util.logging.Logger

def JPR_DEPENDENCY_CONFIGURATION = "oracle.ide.model.DependencyConfiguration"
String JPR_SRC_PATH = "oracle.jdeveloper.model.PathsConfiguration"
 
def jprFile = new File("E:/groovy_stuffs/gradle-projects/SimpleJDevJava/buildSrc/src/test/resources/TestJwsPlugin/SimpleJDevJava/JavaAppMultiSrc/JavaAppMultiSrc.jpr")
def jpr = jprFile
GPathResult getHashFromJpr(File jprFile, String hashValue)
  {
    assert jprFile.exists()
    //LOG.info("About to parse ${jprFile.getPath()} ${jprFile.exists()} ${jprFile.getAbsolutePath()}" )
    def ext = new XmlSlurper(false, false).parse(jprFile)
    def response = ext.hash.find { it.@n == hashValue }
    response
  }
ProjectBuilder builder = ProjectBuilder.builder()
  
builder.withName(jprFile.name)
builder.withProjectDir(jprFile.getParentFile())
Project project = builder.build()
  
  
def pathHash = getHashFromJpr(jpr, JPR_SRC_PATH).hash.find{it.@n.text() == "javaContentSet"}.list.hash.collect{it}
println "PATHASH->$pathHash"    
pathHash.collect{ h ->
  println "H -> ${h.children().size()}"
      def dirPath = h.list.findAll{ it.@n.text() == "url-path"}.url.collect{it.@path.text()}
      println "DIRPATH -> ${dirPath}"
      def paths = h.list.findAll { it.@n.text() == "pattern-filters" }.string.collect { it.@v.text() }
      println "PATHS: "+paths
      def includes= paths.findAll{it.startsWith("+")}.collect{String p -> p.substring(1) }
      println "INCLUDES ->"+includes
      def excludes = paths.findAll { it.startsWith("-") }.collect { String p -> p.substring(1) }
      println "EXCLUDES ->"+excludes
      
      FileTree ft = project.fileTree(dir:dirPath, include:includes, exclude:excludes)
      /*
      FileTree ft = project.fileTree(dirPath)
      includes.each{ inc -> ft.include(inc)}
      excludes.each { exc -> ft.exclude(exc)}
      */
      println "FT -> ${ft}"
      ft
      
    }

 def x = getHashFromJpr(jprFile, JPR_DEPENDENCY_CONFIGURATION)
 
 println "${XmlUtil.serialize(x)}"
 def r = x.breadthFirst().findAll {
      it.@n.text() == "sourceOwnerURL"
    }
 println "R: ${r.toString()}"
 println "${XmlUtil.serialize(r)}"
 