package com.jacobd.jdev.gradle.helper

import groovy.io.FileType
import groovy.xml.XmlUtil
//import org.dubh.jdant.ProjectFile
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test



/**
 * @author: jdanner
 */
class TestJprFileHelper extends GroovyTestCase
{

  ProjectBuilder builder = ProjectBuilder.builder()
  final JprFileHelper jfh = new JprFileHelper()

  final static String extJprProj = "src/test/resources/TestJwsPlugin/SimpleJDevJava/Extension/Extension.jpr"
  final static String clientJprProj = "src/test/resources/TestJwsPlugin/SimpleJDevJava/Client/Client.jpr"
  final static String multiSrcJprProj ="src/test/resources/TestJwsPlugin/SimpleJDevJava/JavaAppMultiSrc/JavaAppMultiSrc.jpr"



  public void testGetDefaultPackage()
  {
    def (jprFile, project) = setup_test_impl(clientJprProj)
    def defPkg = jfh.getDefaultPackage(jprFile)
    assert !defPkg.isEmpty()
    assert defPkg == "com.jacobd.test.app.javaws"
  }

  /**
   * The client project only has one implicit source element
   */
  public void testGetDefaultSource()
  {
    def (jprFile, project) = setup_test_impl(clientJprProj)

    def defaultSrc = jfh.getProjectSourcePathAsString(jprFile)//, project)
    assert !defaultSrc.isEmpty()
    println defaultSrc
    assert defaultSrc.size() == 1
    assert defaultSrc.contains("src")
    def defSrcFile = jfh.getProjectSourcePath(jprFile, project)
    assert defSrcFile.any{it.getName().endsWith("src")}
  }

  @Test
  public void testMultipleSourcePaths()
  {
    def (jprFile, project) = setup_test_impl(multiSrcJprProj)

    def srcPaths = jfh.getProjectSourcePathAsString(jprFile)
    assert !srcPaths.isEmpty()
    assert srcPaths.contains("diffSrc/")
    assert srcPaths.contains("foo/baz/")
  }


  @Test
  public void testTechnologyScopes()
  {
    def (jprFile, project) = setup_test_impl(multiSrcJprProj)

    def techScopes =  jfh.getTechnologyScopes(jprFile)
    println "TECHSCOPES == $techScopes"
    def expected = ["Ant","Java","JavaBeans","Swing/AWT"]

    techScopes.each{ it->
      //println "CLASSNAME: ${it.getClass().getName()}"
      expected.contains(it)
    }
    assert techScopes.containsAll(expected)
    assert techScopes.contains("Ant")
    assert techScopes.contains("Java")
    assert techScopes.contains("JavaBeans")
    assert techScopes.contains("Swing/AWT")
  }


  @Test
  public void testOutputDirectory()
  {
    def (jprFile, project) = setup_test_impl(clientJprProj)
    def outputPath = jfh.getProjectOutputDir(jprFile)
    assert !outputPath.isEmpty()
    assert outputPath.contains("classes/"), "OUTPUTPATH: ${outputPath}"
    File outputDir = jfh.getProjectOutputDirFile(jprFile, project)
    assert outputDir.getName().endsWith("classes")
  }

  @Test
  public void testDeploymentProfiles()
  {
    def (jprFile, project) = setup_test_impl(extJprProj)
    def profiles = jfh.getDeploymentProfileNames(jprFile)
    println "PROFILES: $profiles"
    assert !profiles.isEmpty()
    assert profiles.contains("edtExtensionProfile"), "profiles: ${profiles}"



    (jprFile, project) = setup_test_impl(clientJprProj)
    def emptyProfiles = jfh.getDeploymentProfileNames(jprFile)
    assert emptyProfiles.size() == 0, "profiles was not empty ${emptyProfiles}"

    // TODO: add test for more than one deployment profile
    //assert profiles.compareTo("*") == 0, "expected * when more than one profile is present

  }


  @Test
  public void testProjectLibraryIds()
  {
    def (jprFile, project) = setup_test_impl(extJprProj)
    def projectIds = jfh.getProjectLibraryIds(jprFile)
    println "ID: $projectIds"
    assert !projectIds.isEmpty()
  }

  @Test
  public void testFaceletsLibraryIds()
  {
    def (jprFile, project) = setup_test_impl(extJprProj)
    def projectIds = jfh.getFaceletsLibraryIds(jprFile)
    println "ID: $projectIds"
    assert !projectIds.isEmpty()
  }

  @Test
  public void testgetProjectTagLibraries()
  {
    def (jprFile, project) = setup_test_impl(extJprProj)
    def projectIds = jfh.getProjectTagLibraries(jprFile)
    println "ID: $projectIds"
    assert !projectIds.isEmpty()
  }

  @Test
  public void testExtensionDtProperties()
  {
    def (jprFile, project) = setup_test_impl(extJprProj)
    def projectIds = jfh.getExtensionDtProperties(jprFile)
    println "ID: $projectIds"
    assert !projectIds.isEmpty()
  }
  //getProjectJprDependencies

  @Test
  public void testProjectJprDependencies()
  {
    def (jprFile, project) = setup_test_impl(extJprProj)
    def projects = jfh.getProjectJprDependencies(jprFile)
    println "ID: $projects"
    assert !projects.isEmpty()
    assert projects.contains("../Client/Client.jpr")
  }

  @Test
  public void testProjectJprDependenciesAsFile()
  {
    def (jprFile, project) = setup_test_impl(extJprProj)
    def projects = jfh.getProjectJprDependenciesAsFile(jprFile, project)
    println "ID: $projects"
    assert !projects.isEmpty()
    assert projects.any{it.getName().endsWith("Client.jpr")}
    assert projects.every{it.exists()}

  }



 /* public void testProjectDependencyAddition()
  {
    def (jprFile, project) = setup_test_impl(extJprProj)
    def projects = jfh.addProjectJprDependenciesToProject(jprFile, project)
    assert !project.getDependsOnProjects().isEmpty()
    assert project.getDependsOnProjects().containsAll(projects)
  }*/




  def setup_test_impl(String jprPath)
  {
    //println new File(".foo").getAbsolutePath()
    // TODO: whats the best way to to resolve resources via this mechanism
    //String jpr =  new TestJprFileHelper().getClass().getResource(jprPath).toString()
    //assert jpr != null
    def jprFile = new File(jprPath)
    println jprFile.getAbsolutePath()
    assert jprFile.exists()
    println "JPR_PATH == ${jprFile.getAbsolutePath()}"
    builder.withName(jprFile.name)
    builder.withProjectDir(jprFile.getParentFile())
    Project project = builder.build()
    assert project != null
    return [jprFile, project]
  }



}
