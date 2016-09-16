package org.lorenzos.emmet;

import io.emmet.Emmet;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.lorenzos.emmet.editor.EmmetEditor;
import org.netbeans.modules.csl.spi.GsfUtilities;

/**
 *
 * @author junichi11
 */
public final class TabKeyExpander implements KeyListener {

	private final JTextComponent component;
	private static final KeyStroke DEFAULT_EXPANSION_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0); // 0 : no modifiers
	private static final KeyStroke ALT_TAB_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
	private static final Logger LOGGER = Logger.getLogger(TabKeyExpander.class.getName());

	/**
	 * Get an instance for the editor.
	 *
	 * @param component the editor
	 * @return the instance
	 */
	public static TabKeyExpander get(JTextComponent component) {
		assert component != null;
		TabKeyExpander expander = (TabKeyExpander) component.getClientProperty(TabKeyExpander.class);
		if (expander == null) {
			expander = new TabKeyExpander(component);
			component.putClientProperty(TabKeyExpander.class, expander);
		}
		return expander;
	}

	/**
	 * Remove the instance from the editor.
	 *
	 * @param component the editor
	 */
	public static synchronized void remove(JTextComponent component) {
		TabKeyExpander expander = (TabKeyExpander) component.getClientProperty(TabKeyExpander.class);
		if (expander != null) {
			assert expander.component == component : "Wrong component: TabKeyExpansion.component=" + expander.component + ", component=" + component;
			expander.uninstall();
			component.putClientProperty(TabKeyExpander.class, null);
		}
	}

	private TabKeyExpander(JTextComponent component) {
		this.component = component;
		install();
	}

	/**
	 * Add a key listener.
	 */
	private void install() {
		if (component != null) {
			component.addKeyListener(this);
		}
	}

	/**
	 * Remove a key listener.
	 */
	private void uninstall() {
		if (component != null) {
			component.removeKeyListener(this);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		expand(e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		expand(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		expand(e);
	}

	/**
	 * Expand an abbreviation. Do nothing if a code template is being edited, or
	 * text is selected, or the context is not in available syntaxes.
	 *
	 * @param keyEvent the key event
	 */
	void expand(KeyEvent keyEvent) {
		EmmetOptions options = EmmetOptions.getInstance();
		if (!options.expandWithTab()) {
			return;
		}

		Document document = component.getDocument();
		if (GsfUtilities.isCodeTemplateEditing(document)) {
			return;
		}

		String selectedText = component.getSelectedText();
		if (selectedText != null && !selectedText.isEmpty()) {
			return;
		}

		KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(keyEvent);

		// check whether there is whitespace before the caret position
		try {
			int caretPosition = component.getCaretPosition();
			int previousCaretPosition = caretPosition - 1;
			if (previousCaretPosition >= 0) {
				String text = document.getText(previousCaretPosition, 1);
				if (!Character.isWhitespace(text.charAt(0))) {
					if (ALT_TAB_KEY.equals(keyStroke)) {
						Keymap keymap = component.getKeymap();
						Action action = keymap.getAction(DEFAULT_EXPANSION_KEY);
						if (action != null) {
							action.actionPerformed(null);
							keyEvent.consume();
						}
					}
				}
			}
		} catch (BadLocationException ex) {
			LOGGER.log(Level.WARNING, null, ex);
		}

		if (DEFAULT_EXPANSION_KEY.equals(keyStroke)) {
			if (expand()) {
				keyEvent.consume();
			}
		}
	}

	/**
	 * Expand an abbreviation.
	 *
	 */
	private boolean expand() {
		assert EventQueue.isDispatchThread();
		try {
			long start = System.currentTimeMillis();
			final Emmet emmet = Emmet.getSingleton();
			final EmmetEditor editor = EmmetEditor.create(component);
			// no available syntax, don't expand
			if (editor.getSyntax() == null) {
				return false;
			}

			boolean result = emmet.runAction(editor, "expand_abbreviation"); // NOI18N

			// Restore scrolling position
			editor.restoreInitialScrollingPosition();
			long end = System.currentTimeMillis();
			LOGGER.log(Level.FINE, "Emmet Action(expand_abbreviation): {0}ms", end - start);
			return result;
		} catch (Exception ex) {
			LOGGER.log(Level.WARNING, null, ex);
		}
		return false;
	}

}
