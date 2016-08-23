
package org.lorenzos.emmet.actions;

import java.util.List;
import org.openide.cookies.EditorCookie;

public final class EmmetExpandAbbreviation extends EmmetAbstractAction {

	public EmmetExpandAbbreviation(List<EditorCookie> context) {
		super(context, "expand_abbreviation");
	}

}
