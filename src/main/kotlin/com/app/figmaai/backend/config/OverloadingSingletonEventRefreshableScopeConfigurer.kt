package com.app.figmaai.backend.config

import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope
import java.util.concurrent.ConcurrentHashMap

class OverloadingSingletonEventRefreshableScopeConfigurer : Scope {

    companion object {
        private val container: MutableMap<String, Any> = ConcurrentHashMap()
    }

    override fun resolveContextualObject(key: String): Any? = null

    override fun remove(name: String): Any? = container.remove(name)

    override fun get(name: String, objectFactory: ObjectFactory<*>): Any =
        container.computeIfAbsent(name) { objectFactory.getObject() }

    override fun registerDestructionCallback(name: String, callback: Runnable) {}

    override fun getConversationId(): String? = null
}
