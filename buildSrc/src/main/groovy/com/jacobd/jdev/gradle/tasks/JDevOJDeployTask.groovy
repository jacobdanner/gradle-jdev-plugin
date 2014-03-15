package com.jacobd.jdev.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import com.jacobd.jdev.gradle.helper.JprFileHelper
import org.gradle.internal.os.OperatingSystem

class JDevOJDeployTask extends DefaultTask
{
  JprFileHelper helper = new JprFileHelper()
  // TODO: major refactor here to structure this properly

  File oracleHomePath = System.getenv("ORACLE_HOME")
  File workspace
  File jprFile

  String profileName = "*"

  File getOjDeployPath()
  {
    def oracleHome = new File(System.getenv("ORACLE_HOME"))
    assert oracleHome.exists(), "Deployment requires environment variable ORACLE_HOME pointing to an oracle installation that includes jdeveloper"
    String ojDeployPath = "jdeveloper/jdev/bin/ojdeploy"
    if (isWindows())
    {
      ojDeployPath += ".exe"
    }
    File ojDeploy = new File(oracleHome, ojDeployPath)
    return ojDeploy
  }

  @TaskAction
  def runOjdeploy()
  {
    if(System.getenv("ORACLE_HOME") != null){throw new GradleException("ORACLE_HOME env var needed to use ojdeploy")}
    // TODO: can we deduce workspace without adding a rootproject extension property
    // TODO: what should we do about the specific profile

    def ojDeployExec = ["${ojDeploy.getPath()}", "-workspace", project.rootDir.getPath() + File.separator + "SimpleJDevJava.jws",
        "-project", project.name]
    def profileValue = helper.getDeploymentProfile(jprFile)
    if (profileValue.length() > 0)
    {
      ojDeployExec.add("-profile")
      ojDeployExec.add(profileValue)
    }

    // TODO: would it be simpler to run via AntBuilder
    /*
    def ant = new AntBuilder()   // create an antbuilder
    ant.exec(outputproperty:"cmdOut",
               errorproperty: "cmdErr",
               resultproperty:"cmdExit",
               failonerror: "true",
               executable: '${ojDeploy.getPath()}') {
                arg(value:"-workspace")
                arg(value:project.rootDir.getPath()+File.separator+"SimpleJDevJava.jws")
                arg(value:"-project")
                arg(value:project.name)
                arg(value:"-profile")
                arg(value:"*")
               }
    println "return code:  ${ant.project.properties.cmdExit}"
    println "stderr:         ${ant.project.properties.cmdErr}"
    println "stdout:        ${ ant.project.properties.cmdOut}"
     */
    println "Going to run: ${ojDeployExec.toString()}"
    def proc = ojDeployExec.execute()
    proc.waitFor()
    println proc.text
    assert proc.exitValue() != 0, "Process returned a non-zero exit code, assuming FAILED result\n${proc.err.text}"
  }

  def isWindows()
  {
    /*isWindows = org.gradle.internal.os.OperatingSystem.current().windows

    if (System.properties['os.name'].toLowerCase().contains('windows'))
    {
      return true
    } else
    {
      return false
    }
    */
    return OperatingSystem.current().windows
  }
}