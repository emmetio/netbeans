
package org.lorenzos.emmet.actions;

import java.util.List;
import org.openide.cookies.EditorCookie;

public final class EmmetRemoveTag extends EmmetAbstractAction {

	public EmmetRemoveTag(List<EditorCookie> context) {
		super(context, "remove_tag");
	}

}
