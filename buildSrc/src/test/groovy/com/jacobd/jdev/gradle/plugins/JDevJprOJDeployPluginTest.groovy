package com.jacobd.jdev.gradle.plugins

import com.jacobd.jdev.gradle.helper.JprFileHelper
import com.jacobd.jdev.gradle.tasks.JDevOJDeployTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

/**
 * Created by jacobd on 3/15/14.
 */
class JDevJprOJDeployPluginTest
{
  ProjectBuilder builder = ProjectBuilder.builder()
  final JprFileHelper jfh = new JprFileHelper()

  final static String extJprProj = "src/test/resources/TestJwsPlugin/SimpleJDevJava/Extension/Extension.jpr"
  final static String clientJprProj = "src/test/resources/TestJwsPlugin/SimpleJDevJava/Client/Client.jpr"

  @Test
  public void testCreateDeployPlugin()
  {
    def (File jprFile, Project project) = setup_test_impl(extJprProj)
    project.ext.jprFile = jprFile
    project.ext.workspaceFile = new File(jprFile.getParentFile().getParentFile(), "SimpleJDevJava.jws")
    project.ext.oracleHome = jprFile.getParentFile()
    project.apply plugin: JDevJprOJDeployPlugin

    Set<String> names = jfh.getDeploymentProfileNames(jprFile)
    names.each{ name ->
      assert project.tasks.findByName("ojdeploy_${name}")
    }
  }

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
