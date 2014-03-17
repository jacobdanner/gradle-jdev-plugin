package com.jacobd.jdev.gradle.tasks

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 * Created by jacobd on 3/14/14.
 */
class JDevOJDeployTaskTest
{
  @Test(expected = MissingPropertyException)
  public void missingPropertiesCannotAddTaskToProject()
  {
    Task task = createDeployTask()
    assertTrue(task instanceof JDevOJDeployTask)
  }
  /*
  @Test(expected=AssertionError.class)
  public void verifyRequiresOracleHome()
  {
    JDevOJDeployTask task = createDeployTask()
    task.runOjdeploy()
  }

  @Test
  public void testBasicOjDeployTask()
  {
    JDevOJDeployTask task = createDeployTask()
  }
  */
  private JDevOJDeployTask createDeployTask(){
    final Project project = Projectbuilder.builder().build()
    project.task("ojdeploy", type: JDevOJDeployTask)
  }
}
