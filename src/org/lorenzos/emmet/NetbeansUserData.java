package org.lorenzos.emmet;

import com.google.gson.Gson;
import io.emmet.Emmet;
import io.emmet.IUserData;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

public class NetbeansUserData implements IUserData {

	@Override
	public void load(Emmet ctx) {
		// nothing to load yet from NetBeans itself
	}

	@Override
	public void loadExtensions(Emmet ctx) {
		Preferences prefs = NbPreferences.forModule(EmmetPanel.class);
		String extPath = prefs.get("extPath", "");
		if (extPath != null && extPath.length() > 0) {
			File extDir = new File(extPath);
			if (extDir.exists() && extDir.isDirectory()) {
				File[] files = extDir.listFiles();
				ArrayList<String> extFiles = new ArrayList<>();

				try {
					for (File f : files) {
						extFiles.add(f.getCanonicalPath());
					}
				} catch (Exception e) {}

				Gson gson = new Gson();
				ctx.execJSFunction("javaLoadExtensions", gson.toJson(extFiles));
			}
		}
	}
}
