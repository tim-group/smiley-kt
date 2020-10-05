package com.timgroup.smileykt

import com.timgroup.tucker.info.Component
import com.timgroup.tucker.info.Health
import com.timgroup.tucker.info.Stoppable
import com.timgroup.tucker.info.async.AsyncComponent
import com.timgroup.tucker.info.component.JarVersionComponent
import com.timgroup.tucker.info.servlet.ApplicationInformationServlet
import com.timgroup.tucker.info.status.StatusPageGenerator
import java.time.Clock

class AppStatus(
        appName: String,
        clock: Clock,
        private val health: Health = Health.ALWAYS_HEALTHY,
        private val stoppable: Stoppable = Stoppable.ALWAYS_STOPPABLE,
        private val basicComponents: List<Component> = emptyList(),
        private val asyncComponents: List<AsyncComponent> = emptyList()
) {
    private val statusPageGenerator = StatusPageGenerator(appName, JarVersionComponent(this.javaClass), clock).apply {
        basicComponents.forEach(this::addComponent)
        asyncComponents.forEach(this::addComponent)
    }

    fun createServlet() = ApplicationInformationServlet(statusPageGenerator, stoppable, health)
}
