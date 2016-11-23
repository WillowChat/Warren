package engineer.carrot.warren.warren.registration

import engineer.carrot.warren.warren.loggerFor

interface IRegistrationManager {

    var listener: IRegistrationListener?
    fun register(extension: IRegistrationExtension)
    fun startRegistration()

}

interface IRegistrationListener {

    fun onRegistrationEnded()
    fun onRegistrationFailed()

}

enum class RegistrationExtensionLifecycle { READY, STARTED, SUCCEEDED, FAILED }

class RegistrationManager : IRegistrationManager {

    private val LOGGER = loggerFor<RegistrationManager>()

    override var listener: IRegistrationListener? = null

    private val extensions = mutableMapOf<IRegistrationExtension, RegistrationExtensionLifecycle>()

    override fun register(extension: IRegistrationExtension) {
        extensions += (extension to RegistrationExtensionLifecycle.READY)
    }

    override fun startRegistration() {
        // TODO: Add a registration timeout

        extensions. forEach { (key, value) ->
            key.listener = object : IRegistrationExtensionListener {
                override fun onSuccess() {
                    extensions[key] = RegistrationExtensionLifecycle.SUCCEEDED
                    onExtensionStateChanged()
                }

                override fun onFailure() {
                    extensions[key] = RegistrationExtensionLifecycle.FAILED
                    onExtensionStateChanged()
                }
            }

            extensions[key] = RegistrationExtensionLifecycle.STARTED
            key.startRegistration()
        }
    }

    private fun onExtensionStateChanged() {
        val failedExtensions = extensions.mapNotNull { (extension, lifecycle) ->
            when (lifecycle) {
                RegistrationExtensionLifecycle.FAILED -> extension
                else -> null
            }
        }

        val succeededExtensions = extensions.mapNotNull { (extension, lifecycle) ->
            when (lifecycle) {
                RegistrationExtensionLifecycle.SUCCEEDED -> extension
                else -> null
            }
        }

        if (failedExtensions.isNotEmpty()) {
            LOGGER.error("Some registration extensions failed: $failedExtensions")
            listener?.onRegistrationFailed()
            return
        }

        if (succeededExtensions.size == extensions.size) {
            LOGGER.info("All extensions completed, ending registration")
            listener?.onRegistrationEnded()
        } else {
            val remainingExtensions = extensions.keys - succeededExtensions
            LOGGER.info("Waiting for extensions to finish: $remainingExtensions")
        }
    }

}