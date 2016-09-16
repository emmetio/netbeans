package org.lorenzos.emmet;

import io.emmet.Emmet;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.modules.ModuleInstall;

public class EmmetModule extends ModuleInstall {

	private static final long serialVersionUID = -1885215995373870937L;

	private final PropertyChangeListener editorsTracker = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() == null || EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
				TabKeyExpander.get((JTextComponent) evt.getNewValue());
			}
		}
	};

	private final PreferenceChangeListener extPathChangeListener = new PreferenceChangeListener() {
		@Override
		public void preferenceChange(PreferenceChangeEvent evt) {
			if (evt.getKey().equals(EmmetOptions.EXT_PATH)) {
				Emmet.reset();
			}
		}
	};

	@Override
	public void restored() {
		super.restored();
		Emmet.setUserDataDelegate(new NetbeansUserData());

		EmmetOptions options = EmmetOptions.getInstance();
		options.addPreferenceChangeListener(extPathChangeListener);

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
		EmmetOptions options = EmmetOptions.getInstance();
		options.removePreferenceChangeListener(extPathChangeListener);

		EditorRegistry.removePropertyChangeListener(editorsTracker);
		for (JTextComponent jtc : EditorRegistry.componentList()) {
			TabKeyExpander.remove(jtc);
		}
	}

}
