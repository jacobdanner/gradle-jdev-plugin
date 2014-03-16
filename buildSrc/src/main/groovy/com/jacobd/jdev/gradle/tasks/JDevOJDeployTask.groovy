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

  File oracleHome // = System.getenv("ORACLE_HOME")
  File workspace
  File jprFile
  String profileName

  File getOjDeployCmdPath(File oracleHome)
  {
    assert oracleHome.exists(), "Deployment requires environment variable ORACLE_HOME pointing to an oracle installation that includes jdeveloper"
    String ojDeployPath = "jdeveloper/jdev/bin/ojdeploy"
    if (isWindows())
    {
      ojDeployPath += ".exe"
    }
    File ojDeploy = new File(oracleHome, ojDeployPath)
    return ojDeploy
  }

  List<String> getFullOjDeployCommand(File oracleHome)
  {
    def ojDeployExec = [getOjDeployCmdPath(oracleHome).getPath()]
    ojDeployExec << "-workspace"
    ojDeployExec << workspace //helper.getSourceOwnerURLFromDependencies(project.jprFile).asList().first()
    ojDeployExec << "-project"
    ojDeployExec << project.name
    ojDeployExec.add("-profile")
    ojDeployExec.add(profileName)
    //  def profileValue = helper.getDeploymentProfile(jprFile)

    println ojDeployExec.asList()

    return ojDeployExec
  }


  @TaskAction
  def runOjdeploy()
  {
    //System.getenv("ORACLE_HOME") != null &&
    if (!project.hasProperty("oracleHome"))
    {
      throw new GradleException(" project.oracleHome or ORACLE_HOME env var needed to use ojdeploy should be set")
    }
    // TODO: can we deduce workspace without adding a rootproject extension property
    // TODO: what should we do about the specific profile
    assert project.hasProperty("jprFile"), "the ${this.getClass().getName()} plugin requires project property named jprFile"
    assert project.hasProperty("oracleHome"), "the ${this.getClass().getName()} plugin requires property named oracleHome"

    def ojDeployExec = getFullOjDeployCommand(project.file(project['oracleHome']))
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
    println "Going to run: ${ojDeployExec.join(" ").toString()}"
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


  def getAntBuilderForOJDeploy()
  {
    /**
     * oracle.jdeveloper.ojdeploy.path=E\:\\jdev\\gr_jdev\\gold\\jdeveloper\\jdev\\bin\\ojdeploy.exe
     * oracle.jdeveloper.ant.library=E\:\\jdev\\gr_jdev\\gold\\jdeveloper\\jdev\\/lib/ant-jdeveloper.jar
     * oracle.jdeveloper.deploy.outputfile=E\:\\jdev\\gr_jdev\\gold\\jdeveloper\\mywork\\Application2\\Extension\\deploy\\${profile.name}* oracle.jdeveloper.workspace.path=E\:\\jdev\\gr_jdev\\gold\\jdeveloper\\mywork\\Application2\\Application2.jws
     * oracle.jdeveloper.deploy.profile.name=*
     * oracle.jdeveloper.deploy.dir=E\:\\jdev\\gr_jdev\\gold\\jdeveloper\\mywork\\Application2\\Extension\\deploy
     * oracle.jdeveloper.ojdeploy.path=E\:\\jdev\\gr_jdev\\gold\\jdeveloper\\jdev\\bin\\ojdeploy.exe
     * oracle.jdeveloper.project.name=Extension
     *
     * <target name="deploy" description="Deploy JDeveloper profiles" depends="init">
     *     <taskdef name="ojdeploy"
     *          classname="oracle.jdeveloper.deploy.ant.OJDeployAntTask"
     *          uri="oraclelib:OJDeployAntTask"
     *          classpath="${oracle.jdeveloper.ant.library}"/>
     *     <ora:ojdeploy
     *          xmlns:ora="oraclelib:OJDeployAntTask"
     *          executable="${oracle.jdeveloper.ojdeploy.path}"
     *          ora:buildscript="${oracle.jdeveloper.deploy.dir}/ojdeploy-build.xml"
     *          ora:statuslog="${oracle.jdeveloper.deploy.dir}/ojdeploy-statuslog.xml">
     *          <ora:deploy>
     *            <ora:parameter name="workspace" value="${oracle.jdeveloper.workspace.path}"/>
     *            <ora:parameter name="project" value="${oracle.jdeveloper.project.name}"/>
     *            <ora:parameter name="profile" value="${oracle.jdeveloper.deploy.profile.name}"/>
     *            <ora:parameter name="nocompile" value="false"/>
     *            <ora:parameter name="outputfile" value="${oracle.jdeveloper.deploy.outputfile}"/>
     *            </ora:deploy>
     *    </ora:ojdeploy>
     *  </target>
     */
  }
}