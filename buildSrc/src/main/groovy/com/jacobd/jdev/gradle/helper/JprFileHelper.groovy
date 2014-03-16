package com.jacobd.jdev.gradle.helper

import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.SourceSet

import java.util.logging.Logger

/**
 * @author: jdanner
 */

class JprFileHelper
{
  private static final Logger LOG = Logger.getLogger(JprFileHelper.class.getName())

  final static String JPR_OUTPUT_DIR = "oracle.jdevimpl.config.JProjectPaths"
  final static String JPR_SRC_PATH = "oracle.jdeveloper.model.PathsConfiguration"
  final static String JPR_DEP_PROFILES = "oracle.jdeveloper.deploy.dt.DeploymentProfiles"
  final static String JPR_DEFAULT_PKG = "defaultPackage"
  final static String JPR_TECH_SCOPES = "oracle.ide.model.TechnologyScopeConfiguration"
  static final String JPR_EXT_DT_PROPERTIES = "extensiondtProperties"
  static final String JPR_MODULE_CONFIGURATION = "oracle.jdevimpl.buildtools.ModuleConfiguration"
  static final String JPR_PROJECT_LIBRARIES = "oracle.jdevimpl.config.JProjectLibraries"
  static final String JPR_FACELETS_TAGLIBS = "oracle.jdevimpl.webapp.facelets.libraries.ProjectFaceletsTagLibraries"
  static final String JPR_TAGLIBS = "oracle.jdevimpl.webapp.jsp.libraries.model.ProjectTagLibraries"


  def WEB_TECHNOLOGIES = ["JSF", "JSP", "JAVASCRIPT", "HTML", "TRINIDAD", "WebSocket", "ADF_FACES"]
  static final String JPR_DEFAULT_OUTPUT_DIR = "classes"
  static final String JPR_DEFAULT_SRC_DIR = "src"
  static final String JPR_DEFAULT_HTML_DIR = "public_html"
  def JPR_DEPENDENCY_CONFIGURATION = "oracle.ide.model.DependencyConfiguration"
  def JPR_DEPENDENCY_CONFIGURATION_SOURCEURL = "sourceURL"
  def JPR_DEPENDENCY_CONFIGURATION_SOURCEOWNERURL = "sourceOwnerURL"
  //webappDirName
  //static final String JPR_DEFAULT_

  /**
   * Returns the name of the project. For example, a project called
   * "foo.jpr" would return "foo" from this method.
   *
   * @return the name of the project.
   */
  String trimExt(File jprFile)
  {
    String fileName = jprFile.getName();
    int dot = fileName.lastIndexOf('.');
    if (dot == -1)
    {
      return fileName
    };
    return fileName.substring(0, dot);
  }

  GPathResult getHashFromJpr(File jprFile, String hashValue)
  {
    assert jprFile.exists()
    //LOG.info("About to parse ${jprFile.getPath()} ${jprFile.exists()} ${jprFile.getAbsolutePath()}" )
    def ext = new XmlSlurper(false, false).parse(jprFile)
    def response = ext.hash.find { it.@n == hashValue }
    response
  }

  def getMapofHash(File jpr, String key)
  {
    // TODO: flatten this
    getHashFromJpr(jpr, key).children().collect { ["${it.@n}": it.@v] }
  }

  /**
   * Find and returns the path value from:
   *  <hash n="oracle.jdevimpl.config.JProjectPaths">
   *    <url n="outputDirectory" path="classes/"/>
   *  </hash>
   * @param jpr
   * @return
   */
  String getProjectOutputDir(File jprFile)
  {
    getHashFromJpr(jprFile, JPR_OUTPUT_DIR).url.find { it.@n == "outputDirectory" }.@path.text()
  }

  /**
   * Find and returns the path value from:
   *  <hash n="oracle.jdevimpl.config.JProjectPaths">
   *    <url n="outputDirectory" path="classes/"/>
   *  </hash>
   * @param jpr
   * @return File path of outputDir
   */
  File getProjectOutputDirFile(File jpr, Project project)
  {
    def output = getProjectOutputDir(jpr)
    if (output)
    {
      return new File(project.getProjectDir(), output)
    } else
    {
      return new File(project.getProjectDir(), JPR_DEFAULT_OUTPUT_DIR)
    }
  }

  /**
   * returns the value/url/value
   *  <hash n="extensiondtProperties">
   *    <value n="extensiondt.deploymentdir" v="jdev/extensions/"/>
   *    <url n="extensiondt.manifest" path="src/META-INF/extension.xml"/>
   *    <value n="extensiondt.platform" v="dt_jdev"/>
   *  </hash>
   * TODO: flatten this out into proper map
   * @param jpr
   * @return [[key:val],...]
   *
   */
  def getExtensionDtProperties(File jpr)
  {
    getMapofHash(jpr, JPR_EXT_DT_PROPERTIES)
  }

  def getModuleConfiguration(File jpr)
  {
    getMapofHash(jpr, JPR_MODULE_CONFIGURATION)
  }


  Set<String> getProjectLibraryIds(File jpr)
  {
    getHashFromJpr(jpr, JPR_PROJECT_LIBRARIES).children().find { it.@n == "libraryReferences" }.hash.value.findAll {
      it.@n == "id"
    }.collect { it.@v.text() }
  }

  Set<String> getFaceletsLibraryIds(File jpr)
  {
    getTagLibraryIds(jpr, JPR_FACELETS_TAGLIBS)
  }

  Set<String> getProjectTagLibraries(File jpr)
  {
    Set<String> libIds = getTagLibraryIds(jpr, JPR_TAGLIBS)
    return libIds
  }


  Map<String, Map<String, String>> getTagLibraryDetails(File jpr, String key)
  {
    Map<String, Map<String, String>> details = new HashMap<String, Map<String, String>>()
    getHashFromJpr(jpr, key).list.hash.each { node ->
      def name = node.value.find { it.@n.text() == "name" }.@v.text()
      details.put(name, new HashMap<String, String>())
      node.value.each { details.get(name).put(it.@n.text(), it.@v.text()) }
      node.hash.each { lib ->
        def prefix = lib.@n.text()
        lib.value.each {
          details.get(name).put(prefix + "-" + it.@n.text(), it.@v.text())
        }
      }
    }
    return details
  }

  Set<String> getTagLibraryIds(File jpr, String key)
  {
    getHashFromJpr(jpr, key).list.hash.value.findAll { it.@n == "name" }.collect { it.@v.text() }
  }

/**
 * There is no clear criteria to determine if a project is a test project or not
 * Check the project source paths and the name to see if it contains junit or test
 * @param jpr
 * @return true|false
 */
  boolean isTestProject(File jpr)
  {
    String name = trimExt(jpr)
    def hasTestPath = getProjectSourcePath().findAll { it.contains("test") || it.contains("junit") }
    !hasTestPath.isEmpty() || name.contains("junit") || name.contains("test")
  }

  boolean hasWebProjectTendencies(File jpr)
  {
    List<String> techScopes = jfh.getTechnologyScopes(jpr)
    techScopes.any { WEB_TECHNOLOGIES.contains(it) }
  }

  def addProjectJprDependenciesToProject(File jpr, Project project)
  {
    Set<File> jprDeps = getProjectJprDependenciesAsFile(jpr, project)
    Set<Project> jprProjects = new HashSet<Project>(jprDeps.size())

    jprDeps.each { File dep ->

      def rootProj = project.getRootProject()
      def name = trimExt(dep)
      if (rootProj != null)
      {
        rootProj.subprojects.each {
          println "ROOT SubProjects: ${it.name}"
          if (it.name == name)
          {
            //project.getDependsOnProjects().add(it)
            jprProjects.add(it)
          }
        }
      }
      def depProj = project.project(name)
      if (depProj == null)
      {
        println "could not determine project, trying based on file contents"
        // create project
      } else
      {
        println "Dep project found ${depProj.name}"
        jprProjects.add(depProj)
      }


      jprProjects.each { it ->
        project.getDependsOnProjects().add(it)
      }
      return jprProjects
      /*

   if (project.project(trimExt(dep)))
   def projectFiles = dep.getParentFile().listFiles(new FilenameFilter() {
     @Override
     boolean accept(File file, String s)
     {
       return s.endsWith(".gradle") || s.endsWith(".jpr")
     }
   })


   def gradleFiles = projectFiles.findAll{it.getName().endsWith(".gradle")}
   def jprFiles = projectFiles.findAll{it.getName().endsWith(".jpr")}

   if (!gradleFiles.isEmpty())
   {
     project.getDependsOnProjects().add(project.project(dep.getParentFile()))
   } else if (jprFiles.isEmpty())
   {

   } else {
     println "Could Not determine project dependencies to add"
   }*/


    }
/*Set<File> jprDeps = jfh.getProjectJprDependenciesAsFile(jpr, project)
      for (File jprDep: jprDeps)
      {
        String depProjectName = jfh.trimExt(jprDep)

        if ( project.getRootProject() != null || project.getParent()!= null)
        {
          Project depProj = project.evaluationDependsOn(depProjectName)
          //Project dep = project.getParent().project(depProjectName)
          //project.p

        }
        Project dep = project.getParent()
        jprDep.

      }
      project.dependsOnProjects.addAll(jfh.getProjectJprDependencies(jpr))//.dependsOnProjects().addAll()
        */

  }

  Set<String> getSourceOwnerURLFromDependencies(File jpr)
  {
    getHashFromJpr(jpr, JPR_DEPENDENCY_CONFIGURATION).depthFirst().findAll {
      it.@n.text() == JPR_DEPENDENCY_CONFIGURATION_SOURCEOWNERURL
    }.collect { it.@path.text() }
  }

  Set<String> getProjectJprDependencies(File jpr)
  {
    getHashFromJpr(jpr, JPR_DEPENDENCY_CONFIGURATION).depthFirst().findAll {
      it.@n == JPR_DEPENDENCY_CONFIGURATION_SOURCEURL
    }.collect { it.@path.text() }
  }

  Set<File> getProjectJprDependenciesAsFile(File jpr, Project project)
  {
    getProjectJprDependencies(jpr).collect { new File(project.getRootDir(), it) }
  }


  Set<String> getProjectSourcePathAsString(File jpr)
  {
    def pathHash = getHashFromJpr(jpr, JPR_SRC_PATH)
//jpr.hash.findAll{it.@n=="oracle.jdeveloper.model.PathsConfiguration"}
    if (pathHash.isEmpty())
    {
      return [JPR_DEFAULT_SRC_DIR]
    } else
    {
      pathHash.hash.list.hash.list.findAll { it.@n == "url-path" }.collect { it.url.@path.text() }
    }
  }

  Set<File> getProjectSourcePath(File jpr, Project project)
  {
    getProjectSourcePathAsString(jpr).collect { new File(project.getRootDir(), it) }
    //def pathHash = getHashFromJpr(jpr, JPR_SRC_PATH)//jpr.hash.findAll{it.@n=="oracle.jdeveloper.model.PathsConfiguration"}
    //return pathHash?.isEmpty() ? new File(project.getRootDir(), "src") :
    //  pathHash.hash.list.hash.list.findAll { it.@n == "url-path" }.collect { new File(project.getRootDir(), it.url.@path.text()) }
    //println "SRCPATHS -> $srcPaths"
  }

  Set<FileTree> getProjectSourcesAsFileTrees(File jpr, Project project)
  {

    //FileTree ft = project.fileTree(dir: dirPath, includes: includes, excludes: excludes)

    /*
    FileTree ft = project.fileTree(dirPath)
    includes.each { inc -> ft.include(inc) }
    excludes.each { exc -> ft.exclude(exc) }
    *//*
    project.fileTree {

      from dirPath
      include includes
      exclude excludes
    }*/
    //ft


    def pathHash = getHashFromJpr(jpr, JPR_SRC_PATH).hash.find { it.@n.text() == "javaContentSet" }.list.hash.collect {
      it
    }
    println "PATHASH->$pathHash"
    def ftAll = pathHash.collect { h ->
      println "H -> ${h.children().size()}"
      def dirPath = h.list.findAll { it.@n.text() == "url-path" }.url.collect { it.@path.text() }
      println "DIRPATH -> ${dirPath}"
      def paths = h.list.findAll { it.@n.text() == "pattern-filters" }.string.collect { it.@v.text() }
      println "PATHS: " + paths
      def includes = paths.findAll { it.startsWith("+") }.collect { String p -> p.substring(1) }
      println "INCLUDES ->" + includes
      def excludes = paths.findAll { it.startsWith("-") }.collect { String p -> p.substring(1) }
      println "EXCLUDES ->" + excludes

      FileTree ft = project.fileTree(dir: dirPath, include: includes, exclude: excludes)
      /*
      FileTree ft = project.fileTree(dirPath)
      includes.each{ inc -> ft.include(inc)}
      excludes.each { exc -> ft.exclude(exc)}
      */
      println "FT -> ${ft}"
      ft

    }
    println "FTALL -> $ftAll"
    return ftAll
  }

  SourceSet getProjectSourcesAsSourceSet(File jpr, Project project)
  {
    //throw
  }

  Set<String> getDeploymentProfileNames(File jprFile)
  {

    def deployProfiles = getHashFromJpr(jprFile, JPR_DEP_PROFILES).list.find { it.@n == "profileList" }.string.collect {
      it.@v.text()
    }
    return deployProfiles
  }

  String getDeploymentProfile(File jprFile)
  {
    Set<String> deployProfiles = getDeploymentProfileNames(jprFile)
    LOG.info("DEPLOY PROFILES: $deployProfiles")
    if( deployProfiles.size() == 1)
    {
      return deployProfiles.asList().first()
    }
    if (deployProfiles.size() > 1)
    {
      return "*"
    } else if (deployProfiles.isEmpty())
    {
      return ""
    } else
    {
      deployProfiles.join(",")
    }
  }

  String getDefaultPackage(File jprFile)
  {
    def ext = new XmlSlurper(false, false).parse(jprFile)
    ext.value.findAll { it.@n == JPR_DEFAULT_PKG }.@v.text()
  }

/**
 * Gets the following from the JPR
 * <hash n="oracle.ide.model.TechnologyScopeConfiguration">
 *   <list n="technologyScope">
 *     <string v="Ant"/>
 *     <string v="Java"/>
 *     <string v="JavaBeans"/>
 *     <string v="Swing/AWT"/>
 *   </list>
 * </hash>
 * @param jpr
 * @return
 */
  List<String> getTechnologyScopes(File jprFile)
  {
    def scope = getHashFromJpr(jprFile, JPR_TECH_SCOPES).list.string.collect { it.@v.text() }
    LOG.info("TECH SCOPES: $scope")
    return scope
  }


}
