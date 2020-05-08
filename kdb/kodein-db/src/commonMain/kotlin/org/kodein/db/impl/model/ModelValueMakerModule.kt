package org.kodein.db.impl.model

import org.kodein.db.Value
import org.kodein.db.ValueConverter
import org.kodein.db.ValueMaker
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.ReadBuffer

internal interface ModelValueMakerModule : ValueMaker {

    val mdb: ModelDBImpl

    private fun Any.toValue(): Value {
        when (this) {
            is Value -> return this
            is ByteArray -> return Value.of(this)
            is ReadBuffer -> return Value.of(this)
            is Allocation -> return Value.of(this)
            is Boolean -> return Value.of((if (this) 1 else 0).toByte())
            is Byte -> return Value.of(this)
            is Char -> return Value.ofAscii(this)
            is Short -> return Value.of(this)
            is Int -> return Value.of(this)
            is Long -> return Value.of(this)
            is String -> return Value.ofAscii(this)
            else -> {
                mdb.valueConverters.forEach {
                    it.toValue(this)?.let { return it }
                }
                throw IllegalArgumentException("invalid value: $this")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun valueOf(value: Any): Value = when (value) {
        is Collection<*> ->
            if (value.size == 1) value.requireNoNulls().first().toValue()
            else valueOfAll(*value.toTypedArray().requireNoNulls())
        is Array<*> ->
            if (value.size == 1) (value as Array<Any?>).requireNoNulls().first().toValue()
            else valueOfAll(*(value as Array<Any?>).requireNoNulls())
        else -> value.toValue()
    }

    override fun valueOfAll(vararg values: Any): Value {
        if (values.isEmpty())
            return Value.emptyValue

        if (values.size == 1 && values[0] is Value)
            return values[0] as Value

        val sized = Array(values.size) {
            values[it].toValue()
        }

        return Value.of(*sized)
    }
}