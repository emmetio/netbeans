
package org.lorenzos.emmet.actions;

import io.emmet.Emmet;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import org.lorenzos.utils.OutputUtils;
import org.lorenzos.emmet.editor.EmmetEditor;
import org.openide.cookies.EditorCookie;

public abstract class EmmetAbstractAction implements ActionListener {

	protected List<EditorCookie> context;
	protected String action;

	public EmmetAbstractAction(List<EditorCookie> context, String action) {
		this.context = context;
		this.action = action;
	}

	@Override
	public void actionPerformed(ActionEvent ev) {

		// For each EditorCookie
		ArrayList<Integer> editorCookieDone = new ArrayList<Integer>();
		for (EditorCookie editorCookie : this.context) {
			if (editorCookieDone.contains(editorCookie.hashCode())) continue;
			editorCookieDone.add(editorCookie.hashCode()); // Store

			// Do action
			try {

				// Create JS executor and setup a Zen editor
				final Emmet emmet = Emmet.getSingleton();
				final EmmetEditor editor = EmmetEditor.create(editorCookie);

				// CodeTemplate.insert is called within NbDocument.runAtomic.
				// So, move it into EmmetEditor.
				// Unfortunately, Undo/Redo behavior are changed.
				// see: #3
				emmet.runAction(editor, this.action);

				// Restore scrolling position
				editor.restoreInitialScrollingPosition();

			} catch (Exception ex) {
				ex.printStackTrace(OutputUtils.getErrorStream());
			}

		}
	}

}
