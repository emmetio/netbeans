
package org.lorenzos.emmet.editor;

import io.emmet.Emmet;
import io.emmet.IEmmetEditor;
import java.awt.Rectangle;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.lorenzos.utils.EditorUtilities;
import org.lorenzos.utils.OutputUtils;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.openide.cookies.EditorCookie;
import io.emmet.SelectionData;
import java.io.File;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.mozilla.javascript.Context;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileUtil;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

public class EmmetEditor implements IEmmetEditor {

	private static final String MIME_XCSS = "text/x-css";
	private static final String MIME_CSS = "text/css";
	private static final String MIME_LESS = "text/less";
	private static final String MIME_XLESSCSS = "text/x-lesscss";
	private static final String MIME_LESSCSS = "text/lesscss";
	private static final String MIME_XSASS = "text/x-sass";
	private static final String MIME_SASS = "text/sass";
	private static final String MIME_XSCSS = "text/x-scss";
	private static final String MIME_SCSS = "text/scss";
	private static final String SYNTAX_CSS = "css";
	private static final String SYNTAX_HAML = "haml";
	private static final String SYNTAX_HTML = "html";
	private static final String SYNTAX_LESS = "less";
	private static final String SYNTAX_SASS = "sass";
	private static final String SYNTAX_SCSS = "scss";
	private static final String SYNTAX_STYL = "styl";
	private static final String SYNTAX_STYLUS = "stylus";
	private static final String SYNTAX_XSL = "xsl";

	private JTextComponent textComp;
	private Document doc;
	private String contentType;

	private Rectangle initialScrollingPosition;

	private int caretPosition;
	private int lineStart;
	private int lineEnd;

	private EmmetEditor(JTextComponent textComp) throws EmmetEditorException {
		this.textComp = textComp;
		this.initialScrollingPosition = textComp.getVisibleRect();
		this.setup();
	}

	public static EmmetEditor create(EditorCookie context) throws EmmetEditorException {
		for (JEditorPane pane : context.getOpenedPanes()) {
			return new EmmetEditor(pane); }
		throw new EmmetEditorException();
	}

	public static EmmetEditor create(JTextComponent textComp) throws EmmetEditorException {
		return new EmmetEditor(textComp);
	}

	public StyledDocument getDocument() {
		return (StyledDocument)this.doc;
	}

	@Override
	public SelectionData getSelectionRange() {
		return new SelectionData(
			this.textComp.getSelectionStart(),
			this.textComp.getSelectionEnd()
		);
	}

	@Override
	public void createSelection(int start, int end) {
		this.textComp.setSelectionStart(start);
		this.textComp.setSelectionEnd(end);
	}

	@Override
	public SelectionData getCurrentLineRange() {
		return new SelectionData(
			this.lineStart,
			this.lineEnd
		);
	}

	@Override
	public int getCaretPos() {
		return this.caretPosition;
	}

	@Override
	public void setCaretPos(int pos) {
		this.textComp.setCaretPosition(pos);
	}

	@Override
	public String getCurrentLine() {
		return this.getLine();
	}

	@Override
	public void replaceContent(String value) {
		this.replaceContent(value, 0, this.doc.getLength());
	}

	@Override
	public void replaceContent(String value, int start) {
		this.replaceContent(value, start, this.doc.getLength());
	}

	@Override
	public void replaceContent(String value, int start, int end) {
		this.replaceContent(value, start, end, false);
	}

	@Override
	public void replaceContent(String value, final int start, final int end, boolean no_indent) {
		// Indent string
		if (!no_indent) {
			value = EditorUtilities.stringIndent(value, this.getIndentation()).trim();
		}

		// Expand TAB to SPACES if required
		if (IndentUtils.isExpandTabs(this.doc)) {
			String indent = "";
			for (int i = 0; i < IndentUtils.indentLevelSize(this.doc); i++) {
				indent += " ";
			}
			value = value.replaceAll("\\t", indent);
		}

		// Manage placeholder
		Emmet emmet = Emmet.getSingleton();
		value = Context.toString(emmet.execJSFunction("nbTransformTabstops", value));
		CodeTemplate ct = CodeTemplateManager.get(this.doc).createTemporary(value);

		// see #3
		StyledDocument styledDocument = (StyledDocument) this.doc;
		NbDocument.runAtomic(styledDocument, new Runnable() {
			@Override
			public void run() {
				try {
					EmmetEditor.this.doc.remove(start, end - start);
				} catch (BadLocationException ex) {
					Exceptions.printStackTrace(ex);
				}
			}
		});
		// Replace content
		setCaretPos(start);
		ct.insert(textComp);
	}

	@Override
	public String getContent() {
		try {
			return this.doc.getText(0, this.doc.getLength());
		} catch (BadLocationException ex) {
			ex.printStackTrace(OutputUtils.getErrorStream());
			return "";
		}
	}

	private boolean matchesSyntax(Object... vargs) {
		String ct = this.getContentType();
		for (int i = 0; i < vargs.length; i++) {
			if (ct.equals(vargs[i])) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String getSyntax() {
		// NetBeans returns content type as 'text/x-css' before version 7.3 beta and returns 'text/css' from 7.3 beta
		String syntax = SYNTAX_HTML;
		if (matchesSyntax(MIME_XCSS, MIME_CSS)) {
			syntax = SYNTAX_CSS;
		} else if (matchesSyntax(MIME_XSCSS, MIME_SCSS)) {
			syntax = SYNTAX_SCSS;
		} else if (matchesSyntax(MIME_XSASS, MIME_SASS)) {
			syntax = SYNTAX_SASS;
		} else if (matchesSyntax(MIME_LESS, MIME_XLESSCSS, MIME_LESSCSS)) {
			syntax = SYNTAX_LESS;
		} else {
			String[] knownSyntaxes = {SYNTAX_HAML, SYNTAX_XSL, SYNTAX_STYL, SYNTAX_STYLUS};
			String ct = getContentType();

			for (String s : knownSyntaxes) {
				if (ct.indexOf(s) != -1) {
					syntax = s;
					break;
				}
			}
		}

		Emmet emmet = Emmet.getSingleton();
		return Context.toString(emmet.execJSFunction("javaDetectSyntax", this, syntax));
	}

	@Override
	public String getProfileName() {
		Emmet emmet = Emmet.getSingleton();
		return Context.toString(emmet.execJSFunction("javaDetectProfile", this));
	}

	@Override
	public String prompt(String title) {
		String response = JOptionPane.showInputDialog(null,
			title,
			"Wrap with Abbreviation",
			JOptionPane.QUESTION_MESSAGE);
		if (response == null) return "";
		return response;
	}

	@Override
	public String getSelection() {
		String sel = this.textComp.getSelectedText();
		if (sel == null) {
			sel = "";
		}
		return sel;
	}

	@Override
	public String getFilePath() {
		File file = FileUtil.toFile(NbEditorUtilities.getFileObject(this.doc));
		return file.getAbsolutePath();
	}

	private void setup() throws EmmetEditorException {

		try {

			// Init
			this.doc = this.textComp.getDocument();
			this.caretPosition = this.textComp.getCaretPosition();
			this.lineStart = caretPosition;
			this.lineEnd = caretPosition;
			String cTemp;

			// Get content type
			AbstractDocument abstractDoc = (AbstractDocument) doc;
			abstractDoc.readLock();
			try{
				TokenSequence tokenSequence = TokenHierarchy.get(this.doc).tokenSequence();
				while (tokenSequence != null) {
					tokenSequence.move(this.caretPosition - 1);
					if (tokenSequence.moveNext()) {
						this.setContentType(tokenSequence.language().mimeType());
						if(matchesSyntax(MIME_SCSS, MIME_XSCSS, MIME_LESS, MIME_LESSCSS, MIME_XLESSCSS, MIME_SASS, MIME_XSASS)){
							break;
						}
						tokenSequence = tokenSequence.embedded();
					} else {
						tokenSequence = null;
					}
				}
			} finally {
				abstractDoc.readUnlock();
			}

			// Search for line start
			if (this.lineStart > 0) {
				cTemp = this.doc.getText(this.lineStart - 1, 1);
				while (!cTemp.equals("\n") && !cTemp.equals("\r") && (this.lineStart > 0)) {
					this.lineStart--;
					if (this.lineStart > 0) cTemp = this.doc.getText(this.lineStart - 1, 1);
				}
			}

			// Search for line end
			if (this.lineEnd < this.doc.getLength()) {
				cTemp = this.doc.getText(this.lineEnd, 1);
				while (!cTemp.equals("\n") && !cTemp.equals("\r")) {
					this.lineEnd++;
					cTemp = this.doc.getText(this.lineEnd, 1);
				}
			}

		} catch (BadLocationException ex) {
			ex.printStackTrace(OutputUtils.getErrorStream());
			throw new EmmetEditorException();
		}

	}

	public String getLine() {
		try {
			int offset = lineEnd - lineStart;
			return doc.getText(lineStart, offset);
		} catch (BadLocationException ex) {
			ex.printStackTrace(OutputUtils.getErrorStream());
			return "";
		}
	}

	public void replaceLine(final String replacement) {
		// see #3
		StyledDocument styledDocument = (StyledDocument) this.doc;
		NbDocument.runAtomic(styledDocument, new Runnable() {
			@Override
			public void run() {
				try {
					int offset = lineEnd - lineStart;
					doc.remove(lineStart, offset);
					doc.insertString(lineStart, replacement, null);
					lineEnd = lineStart + replacement.length();
				} catch (BadLocationException ex) {
					Exceptions.printStackTrace(ex);
				}
			}
		});
	}

	public String getIndentation() {
		String ws = "";
		String line = this.getLine();
		int i = 0;
		while (Character.isWhitespace(line.charAt(i))) ws += line.charAt(i++);
		return ws;
	}

	public void restoreInitialScrollingPosition() {
		textComp.scrollRectToVisible(initialScrollingPosition);
	}

	public String getContentType() {
		return contentType;
	}

	private void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
