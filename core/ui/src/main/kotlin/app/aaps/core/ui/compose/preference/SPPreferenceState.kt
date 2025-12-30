/*
 * Copyright 2023 Google LLC
 * Adapted for AndroidAPS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.aaps.core.ui.compose.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.aaps.core.interfaces.sharedPreferences.SP

/**
 * Remembers a MutableState for a Boolean preference backed by SP.
 */
@Composable
fun rememberSPBooleanState(
    sp: SP,
    key: String,
    defaultValue: Boolean,
): MutableState<Boolean> {
    return remember(key, defaultValue, sp) {
        SPBooleanState(sp, key, defaultValue)
    }
}

/**
 * Remembers a MutableState for a String preference backed by SP.
 */
@Composable
fun rememberSPStringState(
    sp: SP,
    key: String,
    defaultValue: String,
): MutableState<String> {
    return remember(key, defaultValue, sp) {
        SPStringState(sp, key, defaultValue)
    }
}

/**
 * Remembers a MutableState for an Int preference backed by SP.
 */
@Composable
fun rememberSPIntState(
    sp: SP,
    key: String,
    defaultValue: Int,
): MutableState<Int> {
    return remember(key, defaultValue, sp) {
        SPIntState(sp, key, defaultValue)
    }
}

/**
 * Remembers a MutableState for a Long preference backed by SP.
 */
@Composable
fun rememberSPLongState(
    sp: SP,
    key: String,
    defaultValue: Long,
): MutableState<Long> {
    return remember(key, defaultValue, sp) {
        SPLongState(sp, key, defaultValue)
    }
}

/**
 * Remembers a MutableState for a Double preference backed by SP.
 */
@Composable
fun rememberSPDoubleState(
    sp: SP,
    key: String,
    defaultValue: Double,
): MutableState<Double> {
    return remember(key, defaultValue, sp) {
        SPDoubleState(sp, key, defaultValue)
    }
}

/**
 * Remembers a MutableState for a Float preference backed by SP (stored as Double).
 */
@Composable
fun rememberSPFloatState(
    sp: SP,
    key: String,
    defaultValue: Float,
): MutableState<Float> {
    return remember(key, defaultValue, sp) {
        SPFloatState(sp, key, defaultValue)
    }
}

@Stable
private class SPBooleanState(
    private val sp: SP,
    private val key: String,
    private val defaultValue: Boolean
) : MutableState<Boolean> {
    private val state = mutableStateOf(sp.getBoolean(key, defaultValue))

    override var value: Boolean
        get() = state.value
        set(value) {
            state.value = value
            sp.putBoolean(key, value)
        }

    override fun component1(): Boolean = value
    override fun component2(): (Boolean) -> Unit = { value = it }
}

@Stable
private class SPStringState(
    private val sp: SP,
    private val key: String,
    private val defaultValue: String
) : MutableState<String> {
    private val state = mutableStateOf(sp.getString(key, defaultValue))

    override var value: String
        get() = state.value
        set(value) {
            state.value = value
            sp.putString(key, value)
        }

    override fun component1(): String = value
    override fun component2(): (String) -> Unit = { value = it }
}

@Stable
private class SPIntState(
    private val sp: SP,
    private val key: String,
    private val defaultValue: Int
) : MutableState<Int> {
    private val state = mutableStateOf(sp.getInt(key, defaultValue))

    override var value: Int
        get() = state.value
        set(value) {
            state.value = value
            sp.putInt(key, value)
        }

    override fun component1(): Int = value
    override fun component2(): (Int) -> Unit = { value = it }
}

@Stable
private class SPLongState(
    private val sp: SP,
    private val key: String,
    private val defaultValue: Long
) : MutableState<Long> {
    private val state = mutableStateOf(sp.getLong(key, defaultValue))

    override var value: Long
        get() = state.value
        set(value) {
            state.value = value
            sp.putLong(key, value)
        }

    override fun component1(): Long = value
    override fun component2(): (Long) -> Unit = { value = it }
}

@Stable
private class SPDoubleState(
    private val sp: SP,
    private val key: String,
    private val defaultValue: Double
) : MutableState<Double> {
    private val state = mutableStateOf(sp.getDouble(key, defaultValue))

    override var value: Double
        get() = state.value
        set(value) {
            state.value = value
            sp.putDouble(key, value)
        }

    override fun component1(): Double = value
    override fun component2(): (Double) -> Unit = { value = it }
}

@Stable
private class SPFloatState(
    private val sp: SP,
    private val key: String,
    private val defaultValue: Float
) : MutableState<Float> {
    private val state = mutableStateOf(sp.getDouble(key, defaultValue.toDouble()).toFloat())

    override var value: Float
        get() = state.value
        set(value) {
            state.value = value
            sp.putDouble(key, value.toDouble())
        }

    override fun component1(): Float = value
    override fun component2(): (Float) -> Unit = { value = it }
}

private fun <T> State<T>.asMutable(setValue: (T) -> Unit): MutableState<T> =
    AsMutableState(this, setValue)

@Stable
private class AsMutableState<T>(private val state: State<T>, private val setValue: (T) -> Unit) :
    MutableState<T> {
    override var value: T
        get() = state.value
        set(value) {
            setValue(value)
        }

    override fun component1(): T = state.value
    override fun component2(): (T) -> Unit = setValue
}
