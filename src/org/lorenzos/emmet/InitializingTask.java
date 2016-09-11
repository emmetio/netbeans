package org.lorenzos.emmet;

import io.emmet.Emmet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.EditorKit;
import org.lorenzos.emmet.editor.EmmetEditor;
import org.lorenzos.emmet.editor.EmmetEditorException;
import org.openide.text.CloneableEditorSupport;
import org.openide.windows.OnShowing;

@OnShowing
public class InitializingTask implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(InitializingTask.class.getName());

	@Override
	public void run() {
		// for loading core files
		Emmet emmet = Emmet.getSingleton();

		// warming-up
		JEditorPane editor = new JEditorPane();
		EditorKit editorKit = CloneableEditorSupport.getEditorKit("text/html"); // NOI18N
		editor.setEditorKit(editorKit);
		editor.setText("p>ul>li*3"); // NOI18N
		editor.setCaretPosition(9);
		try {
			EmmetEditor emmetEditor = EmmetEditor.create(editor);
			emmet.execJSFunction("runEmmetAction", emmetEditor, "expand_abbreviation"); // NOI18N
		} catch (EmmetEditorException ex) {
			LOGGER.log(Level.WARNING, null, ex);
		}
	}

}
