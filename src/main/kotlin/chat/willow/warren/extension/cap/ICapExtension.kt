package chat.willow.warren.extension.cap

interface ICapExtension {

    fun setUp()
    fun tearDown()
    fun valueSet(value: String?) = Unit

}