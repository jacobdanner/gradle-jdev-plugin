package com.jacobd.jdev.gradle.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 * Created by jacobd on 3/14/14.
 */
class JDevOJDeployTaskTest
{
  @Test
  public void canAddTaskToProject()
  {
    Project project = ProjectBuilder.builder().build()
    def task = project.task('ojdeploy', type: JDevOJDeployTask)
    assertTrue(task instanceof JDevOJDeployTask)
  }
}
