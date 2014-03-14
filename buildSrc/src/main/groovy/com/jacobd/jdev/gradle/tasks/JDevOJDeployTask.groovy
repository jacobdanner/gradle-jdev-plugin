package com.jacobd.jdev.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import com.jacobd.jdev.gradle.helper.JprFileHelper

class JDevOJDeployTask extends DefaultTask
{
  JprFileHelper helper = new JprFileHelper()
  // TODO: major refactor here to structure this properly

  File jprFile

  private String getDeploymentProfileNames(File jprFile)
  {
    def deployProfiles = helper.getHashFromJpr(jprFile, JPR_DEP_PROFILES).list.find { it.@n == "profileList" }.string.collect {
      it.@v.text()
    }
    LOG.info("DEPLOY PROFILES: $deployProfiles")
    if (deployProfiles.size() > 1)
    {
      return "*"
    } else
    {
      deployProfiles.first()
    }
  }
  @TaskAction
  def runOjdeploy()
  {
    assert System.getenv("ORACLE_HOME") != null, "ORACLE_HOME env var needed to use ojdeploy"
    def oracleHome = new File(System.getenv("ORACLE_HOME"))
    assert oracleHome.exists(), "Deployment requires environment variable ORACLE_HOME pointing to an oracle installation that includes jdeveloper"
    String ojDeployPath = "jdeveloper/jdev/bin/ojdeploy"
    if (isWindows())
    {
      ojDeployPath += ".exe"
    }
    File ojDeploy = new File(oracleHome, ojDeployPath)
    // TODO: can we deduce workspace without adding a rootproject extension property
    // TODO: what should we do about the specific profile
    def ojDeployExec = ["${ojDeploy.getPath()}", "-workspace", project.rootDir.getPath() + File.separator + "SimpleJDevJava.jws",
        "-project", project.name, "-profile", "*"]
    // This last parameter could also be hardcoded as edtExtensionProfile

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
    if (System.properties['os.name'].toLowerCase().contains('windows'))
    {
      return true
    } else
    {
      return false
    }
  }
}