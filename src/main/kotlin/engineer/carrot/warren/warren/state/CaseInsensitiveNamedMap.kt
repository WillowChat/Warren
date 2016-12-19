package engineer.carrot.warren.warren.state

open class CaseInsensitiveNamedMap<NamedType : INamed>(var mappingState: CaseMappingState) {

    private val namedThings = mutableMapOf<String, NamedType>()

    val all: Map<String, NamedType>
        get() = namedThings

    fun put(value: NamedType) {
        namedThings[mappingState.mapping.toLower(value.name)] = value
    }

    fun remove(key: String): NamedType? {
        return namedThings.remove(mappingState.mapping.toLower(key))
    }

    fun contains(key: String): Boolean {
        return namedThings.contains(mappingState.mapping.toLower(key))
    }

    operator fun get(key: String): NamedType? {
        return namedThings[mappingState.mapping.toLower(key)]
    }

    operator fun plusAssign(namedThing: NamedType) {
        put(namedThing)
    }

    operator fun plusAssign(namedThings: Collection<NamedType>) {
        for (namedThing in namedThings) {
            put(namedThing)
        }
    }

    operator fun set(key: String, thing: NamedType) {
        namedThings.put(key, thing)
    }

    operator fun minusAssign(key: String) {
        remove(key)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is CaseInsensitiveNamedMap<*>) {
            return false
        }

        return namedThings == other.namedThings && mappingState == other.mappingState
    }

    override fun hashCode(): Int {
        var result = mappingState.hashCode()
        result = 31 * result + namedThings.hashCode()
        return result
    }

    fun clear() {
        namedThings.clear()
    }

    override fun toString(): String {
        return "CaseInsensitiveNamedMap(mappingState=$mappingState, namedThings=$namedThings)"
    }

}