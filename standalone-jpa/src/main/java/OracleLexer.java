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

	/**
	 * Fetch Double quoted string from input: "abc"
	 */
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

	
	/**
	 * Fetch Single quoted string from input: 'abc' '''a'''
	 */
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
	 * Fetch Perl style quoted string
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
		out.append(stopMark); // '[' '#'
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

	/**
	 * Fetch Single-line comment: -- abc \r\n 
	 */
	void consumeSLComment() {
		out.append(take()); // '-'
		out.append(take()); // '-'
		while (peek() != (char)0) {
			if (peek() == '\n' || peek() == '\r') {
				break;
			} else {
				out.append(take());
			}
		}
	}

	/**
	 * Fetch Multi-line comment
	 */
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

	/**
	 * Fetch JDBC style bind variable placeholder, and replace it with enumerated one  
	 */
	void consumeBind() {
		take(); // '?'
		out.append(':');
		out.append(String.valueOf(bindNumber++));
		out.append(String.valueOf(' ')); // every named bind has to be followed by space - just for sure
	}

	/**
	 * Fetch any other character 
	 */
	void consumeCharacter() {
		out.append(take());
	}

	/**
	 * Process whole SQL string through primitive Lexer. Return transformed SQL
	 * @return Transformed SQL, having '?' replaced with enumerated Binds
	 */
	public String getRevisedSql() {
		/* Iterate through whole input SQL */
		while (peek() != (char)0) {
			switch (peek()) {
			// Double quoted string was found
			case '"':
				consumeDQString();
				break;

			// Single quoted string was found
			case '\'':
				consumeSQString();
				break;

			// Perl style string was found - or just pure character
			case 'q':
			case 'Q':
				if (peekNext() == '\'') {
					consumePQString();
				} else {
					consumeCharacter();
				}
				break;

			// Single-line comment was found - or just pure dash 
			case '-':
				if (peekNext() == '-') {
					consumeSLComment();
				} else {
					consumeCharacter();
				}
				break;

			// Double-line comment was found - or just pure slash
			case '/':
				if (peekNext() == '*') {
					consumeMLComment();
				} else {
					consumeCharacter();
				}
				break;

			// Bind variable was found - but outside String or Comment
			case '?':
				consumeBind();
				break;

			// Match any other character
			default:
				consumeCharacter();
			}

		}

		return out.toString();
	}


}
