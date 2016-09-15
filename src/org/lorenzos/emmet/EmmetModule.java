package org.lorenzos.emmet;

import io.emmet.Emmet;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbPreferences;

public class EmmetModule extends ModuleInstall {

	private static final long serialVersionUID = -1885215995373870937L;

	private final PropertyChangeListener editorsTracker = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() == null || EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
				TabKeyExpansion.get((JTextComponent) evt.getNewValue());
			}
		}
	};

	@Override
	public void restored() {
		super.restored();
		Emmet.setUserDataDelegate(new NetbeansUserData());

		Preferences prefs = NbPreferences.forModule(EmmetPanel.class);
		prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent evt) {
				if (evt.getKey().equals("extPath")) { // NOI18N
					Emmet.reset();
				}
			}
		});

		// support for tab key
		EditorRegistry.addPropertyChangeListener(editorsTracker);
	}

	@Override
	public void close() {
		finish();
	}

	@Override
	public void uninstalled() {
		finish();
	}

	private void finish() {
		EditorRegistry.removePropertyChangeListener(editorsTracker);
		for (JTextComponent jtc : EditorRegistry.componentList()) {
			TabKeyExpansion.remove(jtc);
		}
	}

}
