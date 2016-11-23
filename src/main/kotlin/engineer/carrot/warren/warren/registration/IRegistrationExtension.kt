package engineer.carrot.warren.warren.registration

interface IRegistrationExtensionListener {

    fun onSuccess()
    fun onFailure()

}

interface IRegistrationExtension {

    var listener: IRegistrationExtensionListener?
    fun startRegistration()
    fun onRegistrationSucceeded()
    fun onRegistrationFailed()

}