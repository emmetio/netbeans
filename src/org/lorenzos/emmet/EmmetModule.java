package org.lorenzos.emmet;
import io.emmet.Emmet;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbPreferences;

public class EmmetModule extends ModuleInstall {

	@Override
	public void restored() {
		super.restored();
		Emmet.setUserDataDelegate(new NetbeansUserData());

		Preferences prefs = NbPreferences.forModule(EmmetPanel.class);
		prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent evt) {
				if (evt.getKey().equals("extPath")) {
					Emmet.reset();
				}
			}
		});
	}
}
