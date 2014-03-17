package com.jacobd.jdev.gradle.plugins

import com.jacobd.jdev.gradle.helper.JprFileHelper
import com.jacobd.jdev.gradle.tasks.JDevOJDeployTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by jacobd on 3/15/14.
 */
class JDevJprOJDeployPlugin implements Plugin<Project>
{
  JprFileHelper helper = new JprFileHelper()
  File jpr
  void apply(Project project)
  {
    assert project.hasProperty("jprFile"), "the ${this.getClass().getName()} plugin requires project property named jprFile"
    assert project.hasProperty("oracleHome"), "the ${this.getClass().getName()} plugin requires property named oracleHome"

    jpr = new File(project.jprFile as String)
    def deployProfiles = helper.getDeploymentProfileNames( jpr)
    def workspacePath = ""
    if (project.hasProperty("workspaceFile")){
    //TODO: deduce this properly
      def workspacePaths = helper.getSourceOwnerURLFromDependencies(jpr)
      assert !workspacePaths.isEmpty()
      workspacePath = workspacePaths.asList().first()
    } else {
      workspacePath = project.workspaceFile
    }
    deployProfiles.each{profileNameValue ->
        JDevOJDeployTask task =project.task("ojdeploy_${profileNameValue}", type:JDevOJDeployTask)
        task.configure {
          workspace = project.file(workspacePath)
          profileName = profileNameValue
          jprFile = jpr
        }
    }
    if(deployProfiles.size()>1)
    {
      JDevOJDeployTask task = project.task("ojdeploy_all", type: JDevOJDeployTask)
      task.configure {
        workspace = file(workspacePath)
        profileName = "*"
        jprFile = jpr
      }
    }



  }

}
