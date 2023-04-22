package com.app.figmaai.backend.config

import org.springframework.beans.factory.config.Scope
import org.springframework.context.ApplicationListener

class OverloadBeansEventListener(
    private val scopeConfigurer: Scope
) : ApplicationListener<OverloadBeansEvent> {

    @Suppress("UNCHECKED_CAST")
    override fun onApplicationEvent(event: OverloadBeansEvent) {
        val beans = event.source as Array<String>
        beans.forEach { scopeConfigurer.remove(it) }
    }
}
