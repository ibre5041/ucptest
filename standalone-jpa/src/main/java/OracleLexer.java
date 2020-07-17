/**
 * Try to replace JDBC question marks with Oracle bind variables
 * Implement very simple Oracle Lexer
 * Handle: Single Quoted Strings, Double Quoted Strings, Perl Style Quoted Strings, Single Line Comments, Multi line comments
 */
class OracleLexer {
	int bindNumber = 1;
	int position = 0;
	String stmt;
	StringBuffer out;

	OracleLexer(String stmt) {
		this.stmt = stmt;
		this.out = new StringBuffer(stmt.length() + 64);
	}


	char peek() {
		if (position < stmt.length()) {
			return stmt.charAt(position);
		}
		return (char)0;
	}

	char peekNext() {
		if (position+1 < stmt.length()) {
			return stmt.charAt(position+1);
		}
		return (char)0;			
	}

	char take() {
		char r = peek();
		position++;
		return r;
	}

	void consumeDQString() {
		out.append(take()); // '"'
		while (peek() != (char)0) {
			if (peek() == '"') {
				out.append(take());
				break;
			} else {
				out.append(take());
			}
		}			
	}

	void consumeSQString() {
		out.append(take()); // '\''
		while (peek() != (char)0) {
			if (peek() == '\'' && peekNext() != '\'') { // End of String was found
				out.append(take()); // '\''
				break;
			} else if (peek() == '\'' && peekNext() == '\'') { // Double apostrophe was found in String -> continue consuming
				out.append(take()); // '\''
				out.append(take()); // '\''
			} else {
				out.append(take());
			}
		}
	}

	/**
	 * q'#Oracle's quote operator#'
	 * q'[Oracle's quote operator]'
	 */
	void consumePQString() {
		out.append(take()); // 'q'
		out.append(take()); // "'"

		if (peek() == (char)0) {
			return;
		}

		char stopMark = take();
		out.append(stopMark); // '['
		if (stopMark == '<') { stopMark = '>'; }
		if (stopMark == '{') { stopMark = '}'; }
		if (stopMark == '[') { stopMark = ']'; }
		if (stopMark == '(') { stopMark = ')'; }

		while (peek() != (char)0) {
			if (peek() == stopMark && peekNext() == '\'') { // End of String was found ]'
				out.append(take()); // ]
				out.append(take()); // '
				break;
			} else {
				out.append(take());
			}
		}

	}

	void consumeSLComment() {
		out.append(take()); // '-'
		out.append(take()); // '-'
		while (peek() != (char)0) {
			if (peek() == '\n' || peek() == '\r') {
				break;
			} else {
				out.append(take()); // '-'
			}
		}
	}

	void consumeMLComment() {
		out.append(take()); // '/'
		out.append(take()); // '*'
		while (peek() != (char)0) {
			if (peek() == '*' && peekNext() == '/') {
				out.append(take()); // '*'
				out.append(take()); // '/'
				break;
			} else {
				out.append(take());
			}

		}
	}

	void consumeBind() {
		take(); // '?'
		out.append(':');
		out.append(String.valueOf(bindNumber++));
		out.append(String.valueOf(' ')); // every named bind has to be followed by space - just for sure
	}

	void consumeCharacter() {
		out.append(take());
	}

	public String getRevisedSql() {
		while (peek() != (char)0) {

			switch (peek()) {
			case '"':
				consumeDQString();
				break;

			case '\'':
				consumeSQString();
				break;

			case 'q':
			case 'Q':
				if (peekNext() == '\'') {
					consumePQString();
				} else {
					consumeCharacter();
				}
				break;

			case '-':
				if (peekNext() == '-') {
					consumeSLComment();
				} else {
					consumeCharacter();
				}
				break;

			case '/':
				if (peekNext() == '*') {
					consumeMLComment();
				} else {
					consumeCharacter();
				}
				break;

			case '?':
				consumeBind();
				break;

			default:
				consumeCharacter();
			}

		}

		return out.toString();
	}


}
