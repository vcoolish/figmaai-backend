package com.app.figmaai.backend.config

import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope
import java.lang.String.format
import java.util.concurrent.ConcurrentHashMap

class SwitchSingletonEventRefreshableScopeConfigurer : Scope {

    companion object {
        private val container: MutableMap<String, SwitchSingletonIdentification> = ConcurrentHashMap()
        private const val beanNameFormat = "%s#%s"
    }

    override fun resolveContextualObject(key: String): Any? = null

    override fun remove(name: String): Any? = container.remove(name)

    override fun get(name: String, objectFactory: ObjectFactory<*>): Any =
        objectFactory.getObject()
            .let { it as SwitchSingletonIdentification }
            .let { bean -> container.computeIfAbsent(format(beanNameFormat, name, bean.getId())) { bean } }

    override fun registerDestructionCallback(name: String, callback: Runnable) {}

    override fun getConversationId(): String? = null
}
