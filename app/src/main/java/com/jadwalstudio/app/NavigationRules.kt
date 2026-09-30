package com.jadwalstudio.app

internal fun groupReturnDestination(origin: Screen): Screen = when (origin) {
    Screen.HOME, Screen.TASKS, Screen.GROUPS -> origin
    Screen.CREATE_GROUP -> Screen.GROUPS
    else -> Screen.TASKS
}

internal fun backDestination(screen: Screen, taskOrigin: Screen, groupOrigin: Screen,
                             groupsOrigin: Screen, addTaskOrigin: Screen): Screen = when (screen) {
    Screen.LOGIN, Screen.REGISTER -> Screen.LOGIN
    Screen.TASK_EDIT -> taskOrigin
    Screen.TASK_DETAIL -> Screen.TASKS
    Screen.ADD_TASK_CHOICE -> addTaskOrigin
    Screen.ADD_TASK_INDIVIDUAL -> Screen.ADD_TASK_CHOICE
    Screen.ADD_TASK_GROUP, Screen.GROUP_MEMBERS, Screen.GROUP_INVITE, Screen.GROUP_LOGS -> Screen.GROUP_DETAIL
    Screen.GROUP_DETAIL -> groupOrigin
    Screen.CREATE_GROUP -> Screen.GROUPS
    Screen.GROUPS -> groupsOrigin
    Screen.ADD_SCHEDULE -> Screen.SCHEDULE
    else -> Screen.HOME
}
