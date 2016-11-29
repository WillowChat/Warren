package engineer.carrot.warren.warren.registration

import engineer.carrot.warren.warren.loggerFor

interface IRegistrationManager {

    var listener: IRegistrationListener?
    fun register(extension: IRegistrationExtension)
    fun startRegistration()

    fun onExtensionSuccess(extension: IRegistrationExtension)
    fun onExtensionFailure(extension: IRegistrationExtension)

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
        // TODO: Assert there's more than one extension?

        extensions.forEach { extension, _ ->
            extensions[extension] = RegistrationExtensionLifecycle.STARTED

            extension.startRegistration()
        }
    }

    override fun onExtensionSuccess(extension: IRegistrationExtension) {
        if (!extensions.containsKey(extension)) {
            LOGGER.warn("tried to notify of extension success, but we aren't tracking it: $extension")
            return
        }

        extensions[extension] = RegistrationExtensionLifecycle.SUCCEEDED
        onExtensionStateChanged()
    }

    override fun onExtensionFailure(extension: IRegistrationExtension) {
        if (!extensions.containsKey(extension)) {
            LOGGER.warn("tried to notify of extension failure, but we aren't tracking it: $extension")
            return
        }

        extensions[extension] = RegistrationExtensionLifecycle.FAILED
        onExtensionStateChanged()
    }

    private fun onExtensionStateChanged() {
        val failedExtensions = extensions.filter { (_, lifecycle) ->
            lifecycle == RegistrationExtensionLifecycle.FAILED
        }.keys

        val succeededExtensions = extensions.filter { (_, lifecycle) ->
            lifecycle == RegistrationExtensionLifecycle.SUCCEEDED
        }.keys

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