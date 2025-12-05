package app.aaps.core.interfaces.userEntry

import androidx.annotation.DrawableRes
import app.aaps.core.data.model.UE
import app.aaps.core.data.ue.Sources
import app.aaps.core.data.ue.ValueWithUnit

interface UserEntryPresentationHelper {

    @DrawableRes fun iconId(source: Sources): Int
    fun listToPresentationString(list: List<ValueWithUnit>): String
    fun userEntriesToCsv(userEntries: List<UE>): String
}
