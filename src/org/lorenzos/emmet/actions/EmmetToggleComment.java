
package org.lorenzos.emmet.actions;

import java.util.List;
import org.openide.cookies.EditorCookie;

public final class EmmetToggleComment extends EmmetAbstractAction {

	public EmmetToggleComment(List<EditorCookie> context) {
		super(context, "toggle_comment");
	}

}
