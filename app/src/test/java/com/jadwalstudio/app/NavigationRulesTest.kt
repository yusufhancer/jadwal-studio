package com.jadwalstudio.app

import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationRulesTest {
    private fun back(screen: Screen, group: Screen = Screen.TASKS, groups: Screen = Screen.TASKS,
                     add: Screen = Screen.TASKS, task: Screen = Screen.TASK_DETAIL) =
        backDestination(screen, task, group, groups, add)

    @Test fun groupOpenedFromTasksReturnsToTasks() {
        assertEquals(Screen.TASKS, back(Screen.GROUP_DETAIL, groupReturnDestination(Screen.TASKS)))
    }
    @Test fun groupOpenedFromDashboardReturnsToDashboard() {
        assertEquals(Screen.HOME, back(Screen.GROUP_DETAIL, groupReturnDestination(Screen.HOME)))
    }
    @Test fun createdGroupDoesNotReopenCreateForm() {
        assertEquals(Screen.GROUPS, back(Screen.GROUP_DETAIL, groupReturnDestination(Screen.CREATE_GROUP)))
    }
    @Test fun groupListAndAddChoiceRememberOrigins() {
        assertEquals(Screen.ADD_TASK_CHOICE, back(Screen.GROUPS, groups = Screen.ADD_TASK_CHOICE))
        assertEquals(Screen.TASK_DETAIL, back(Screen.ADD_TASK_CHOICE, add = Screen.TASK_DETAIL))
    }
    @Test fun nestedGroupPagesReturnToDetail() {
        listOf(Screen.GROUP_MEMBERS, Screen.GROUP_INVITE, Screen.GROUP_LOGS, Screen.ADD_TASK_GROUP).forEach {
            assertEquals(Screen.GROUP_DETAIL, back(it))
        }
    }
    @Test fun taskEditorReturnsToItsOrigin() {
        listOf(Screen.TASKS, Screen.TASK_DETAIL, Screen.GROUP_DETAIL).forEach {
            assertEquals(it, back(Screen.TASK_EDIT, task = it))
        }
    }
}
