package com.app.figmaai.backend.credentials

import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import javax.persistence.EntityManager
import javax.persistence.Query
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

@Suppress("SqlResolve")
@Transactional(readOnly = true)
class DbCredentialsServiceImpl(
    private val entityManager: EntityManager,
    private val activeProfile: String
) : DbCredentialsService {

    private var encoderDecoderService: EncoderDecoderService? = null

    constructor(
        entityManager: EntityManager,
        activeProfile: String,
        encoderDecoderService: EncoderDecoderService
    ) : this(entityManager, activeProfile) {
        this.encoderDecoderService = encoderDecoderService
    }

    companion object {
        private const val defaultTable = "credentials"
        private const val paypalTable = "credentials_regions_paypal"
        private const val stripeTable = "credentials_regions_stripe"
        private const val intercassaTable = "credentials_regions_interkassa"
        private const val advCashTable = "credentials_regions_advcash"
        private const val powerCashTable = "credentials_regions_powercash"
        private val arrayClass: KClass<out Array<*>> = emptyArray<Any>()::class
        private val stringClass: KClass<String> = String::class
        private val bigDecimalClass: KClass<BigDecimal> = BigDecimal::class

        private const val updateCredentialsQuery: String = """
            UPDATE $defaultTable 
            SET %s
            WHERE lower(active_profile) = :activeProfile
        """

        private const val selectActiveSocialNetworks: String = """
            SELECT c.active_social_networks
            FROM credentials c 
            WHERE lower(c.active_profile) = :activeProfile
        """
    }

    private final val googleCredentialsUpdateQueryBuilder: Map<(T: SocialCredentials) -> Boolean, String> =
        linkedMapOf(
            { data: SocialCredentials -> data.clientId.isNotBlank() } to "google_client_id = :clientId",
            { data: SocialCredentials -> data.clientSecret.isNotBlank() } to "google_client_secret = :clientSecret",
            { data: SocialCredentials -> data.scope.isNotBlank() } to "google_scope = :scope"
        )

    private final val socialCredentialsUpdateStatement: Map<(T: SocialCredentials) -> Boolean, (Query, T: SocialCredentials) -> Unit> =
        linkedMapOf(
            { data: SocialCredentials -> data.clientId.isNotBlank() } to
                    { q, cred ->
                        q.setParameter(
                            "clientId",
                            encoderDecoderService?.encode(cred.clientId) ?: cred.clientId
                        )
                    },
            { data: SocialCredentials -> data.clientSecret.isNotBlank() } to
                    { q, cred ->
                        q.setParameter(
                            "clientSecret",
                            encoderDecoderService?.encode(cred.clientSecret) ?: cred.clientSecret
                        )
                    },
            { data: SocialCredentials -> data.scope.isNotBlank() } to
                    { q, cred -> q.setParameter("scope", cred.scope) }
        )

    private final val activeSocialNetworksUpdateQueryBuilder: Map<(ActiveSocialNetworks) -> Boolean, String> =
        linkedMapOf(
            { _: ActiveSocialNetworks -> true } to "active_social_networks = :networks"
        )
    private final val activeSocialNetworksUpdateStatement: Map<(ActiveSocialNetworks) -> Boolean, (Query, ActiveSocialNetworks) -> Unit> =
        linkedMapOf(
            { _: ActiveSocialNetworks -> true } to
                    { query, data -> query.setParameter("networks", data.networks) }
        )

    private fun getSelectSocialNetworkCredentialsQuery(network: String): String = """
        SELECT c.${network}_client_id,
        c.${network}_client_secret,
        c.${network}_scope
        FROM credentials c
        WHERE lower(c.active_profile) = :activeProfile
    """

    private final val selectGoogleCredentialsQuery: String = getSelectSocialNetworkCredentialsQuery("google")

    override fun getGoogleCredentials(): GoogleCredentials {
        val result = getSingleResult(selectGoogleCredentialsQuery, arrayClass)
        val clientId = result[0] as String
        val clientSecret = result[1] as String
        return GoogleCredentials(
            clientId = encoderDecoderService?.decode(clientId) ?: clientId,
            clientSecret = encoderDecoderService?.decode(clientSecret) ?: clientSecret,
            scope = result[2] as String
        )
    }

    override fun getActiveSocialNetworks(): ActiveSocialNetworks =
        ActiveSocialNetworks(getSingleResult(selectActiveSocialNetworks, stringClass))

    @Transactional(readOnly = false)
    override fun updateActiveSocialNetworks(activeSocialNetworks: ActiveSocialNetworks): Boolean =
        updateCredentials(
            activeSocialNetworksUpdateQueryBuilder,
            activeSocialNetworksUpdateStatement,
            activeSocialNetworks
        )

    @Suppress("UNCHECKED_CAST")
    @Transactional(readOnly = false)
    override fun updateGoogleCredentials(credentials: GoogleCredentials): Boolean =
        updateCredentials(googleCredentialsUpdateQueryBuilder, socialCredentialsUpdateStatement, credentials)

    private fun <T : CredentialsOverloadable> updateCredentials(
        builder: Map<(T) -> Boolean, String>,
        statement: Map<(T) -> Boolean, (Query, T) -> Unit>,
        credentials: T
    ): Boolean {
        val query = builder
            .filterKeys { it(credentials) }
            .map { it.value }
            .joinToString(separator = ", ")
            .let { String.format(updateCredentialsQuery, it) }
            .let { entityManager.createNativeQuery(it) }

        statement
            .filterKeys { it(credentials) }
            .map { it.value }
            .forEach { it(query, credentials) }
        return query
            .setParameter("activeProfile", activeProfile)
            .executeUpdate() > 0
    }

    private fun <C : Any, K : KClass<out C>> getSingleResult(query: String, clazz: K): C =
        entityManager.createNativeQuery(query)
            .setParameter("activeProfile", activeProfile)
            .singleResult
            .let { clazz.cast(it) }

    private fun <V : Any, C : List<V>, K : KClass<out C>> getListResult(query: String, clazz: K): C =
        entityManager.createNativeQuery(query)
            .setParameter("activeProfile", activeProfile)
            .resultList
            .let { clazz.cast(it) }
}
