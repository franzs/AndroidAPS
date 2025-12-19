package app.aaps.plugins.source

import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.shared.tests.TestBaseWithProfile
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock

class GlimpPluginTest : TestBaseWithProfile() {

    @Mock lateinit var persistenceLayer: PersistenceLayer
    @Mock lateinit var uiInteraction: UiInteraction

    private lateinit var glimpPlugin: GlimpPlugin

    @BeforeEach
    fun setup() {
        glimpPlugin = GlimpPlugin(rh, aapsLogger, preferences, persistenceLayer, dateUtil, profileUtil, uiInteraction)
    }

    @Test
    fun advancedFilteringSupported() {
        assertThat(glimpPlugin.advancedFilteringSupported()).isFalse()
    }

    @Test
    fun preferenceScreenTest() {
        val screen = preferenceManager.createPreferenceScreen(context)
        glimpPlugin.addPreferenceScreen(preferenceManager, screen, context, null)
        assertThat(screen.preferenceCount).isGreaterThan(0)
    }
}
