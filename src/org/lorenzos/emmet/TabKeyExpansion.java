package org.lorenzos.emmet;

import io.emmet.Emmet;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.lorenzos.emmet.editor.EmmetEditor;
import org.netbeans.modules.csl.spi.GsfUtilities;

/**
 *
 * @author junichi11
 */
public final class TabKeyExpansion implements KeyListener {

	private static final KeyStroke DEFAULT_EXPANSION_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0); // 0 : no modifiers
	private final JTextComponent component;
	private static final Logger LOGGER = Logger.getLogger(TabKeyExpansion.class.getName());

	public static TabKeyExpansion get(JTextComponent component) {
		assert component != null;
		TabKeyExpansion expansion = (TabKeyExpansion) component.getClientProperty(TabKeyExpansion.class);
		if (expansion == null) {
			expansion = new TabKeyExpansion(component);
			component.putClientProperty(TabKeyExpansion.class, expansion);
		}
		return expansion;
	}

	public static synchronized void remove(JTextComponent component) {
		TabKeyExpansion expansion = (TabKeyExpansion) component.getClientProperty(TabKeyExpansion.class);
		if (expansion != null) {
			assert expansion.component == component : "Wrong component: TabKeyExpansion.component=" + expansion.component + ", component=" + component;
			expansion.uninstall();
			component.putClientProperty(TabKeyExpansion.class, null);
		}
	}

	private TabKeyExpansion(JTextComponent component) {
		this.component = component;
		install();
	}

	private void install() {
		if (component != null) {
			component.addKeyListener(this);
		}
	}

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

	private void expand(KeyEvent keyEvent) {
		if (GsfUtilities.isCodeTemplateEditing(component.getDocument())) {
			return;
		}

		KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(keyEvent);
		if (DEFAULT_EXPANSION_KEY.equals(keyStroke)) {
			if (expand()) {
				keyEvent.consume();
			}
		}
	}

	private boolean expand() {
		assert EventQueue.isDispatchThread();
		try {
			long start = System.currentTimeMillis();
			final Emmet emmet = Emmet.getSingleton();
			final EmmetEditor editor = EmmetEditor.create(component);

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
