package org.lorenzos.emmet;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author junichi11
 */
public final class EmmetOptions {

	private static final EmmetOptions INSTANCE = new EmmetOptions();
	static final String EXT_PATH = "extPath"; // NOI18N
	private static final String EXPAND_WITH_TAB = "emmet-expand-with-tab"; // NOI18N

	private EmmetOptions() {
	}

	public static EmmetOptions getInstance() {
		return INSTANCE;
	}

	public String getExtPath() {
		return getPreferences().get(EXT_PATH, ""); // NOI18N
	}

	public void setExtPath(String extPath) {
		getPreferences().put(EXT_PATH, extPath);
	}

	public boolean expandWithTab() {
		return getPreferences().getBoolean(EXPAND_WITH_TAB, false);
	}

	public void setExpandWithTab(boolean expandWithTab) {
		getPreferences().putBoolean(EXPAND_WITH_TAB, expandWithTab);
	}

	void addPreferenceChangeListener(PreferenceChangeListener listener) {
		getPreferences().addPreferenceChangeListener(listener);
	}

	void removePreferenceChangeListener(PreferenceChangeListener listener) {
		getPreferences().removePreferenceChangeListener(listener);
	}

	private Preferences getPreferences() {
		return NbPreferences.forModule(EmmetOptions.class);
	}
}
