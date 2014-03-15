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
    //TODO: deduce this properly
    def workspacePath = helper.deduceWorkspaceFromDependencies(jpr)[0]

    deployProfiles.each{profileNameValue ->
        JDevOJDeployTask task =project.task("ojdeploy_${profileNameValue}", type:JDevOJDeployTask)
        task.configure {
          workspace = file(workspacePath)
          profileName = profileNameValue
          jprFile = jpr
        }
    }
    if(deployProfiles.size()>1)
    {
      JDevOJDeployTask task = project.task("ojdeploy_all", type: JDevOJDeployTask)
      task.configure {
        workspace = file(workspacePath)
        jprFile = jpr
      }
    }



  }

}
