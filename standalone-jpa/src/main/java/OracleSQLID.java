import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Compute SQL_ID for SQL string 
 * @author Ivan Brezina(ibre5041)
 *
 */
class OracleSQLID {
	/**
	 * Try to replace JDBC question marks with Oracle bind variables
	 * Implement very simple Oracle Lexer
	 * Handle: Single Quoted Strings, Double Quoted Strings, Perl Style Quoted Strings, Single Line Comments, Multi line comments
	 */
	class Lexer {
		int bindNumber = 1;
		int position = 0;
		String stmt;
		StringBuffer out;

		Lexer(String stmt) {
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

    /**
     * Compute sqlid for a statement, the same way as Oracle does
     * http://www.slaviks-blog.com/2010/03/30/oracle-sql_id-and-hash-value/
     * https://blog.tanelpoder.com/2009/02/22/sql_id-is-just-a-fancy-representation-of-hash-value/
     * @param stmt - SQL string without trailing 0x00 Byte
     * @return sql_id as computed by Oracle
     */ 	
	private static String SQL_ID(String stmt)
	{
		String result = "(sql_id)";
		
		try
		{
			// compute MD5 sum from SQL string - including trailing 0x00 Byte
			byte[] message  = (stmt).getBytes("utf8");
			byte[] bytesMessage = new byte[message.length+1];
			System.arraycopy(message, 0, bytesMessage, 0, message.length); 		
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] b = md.digest(bytesMessage);

			// most significant unsigned int
			long val_msb = (((b[11] & 0xff) * 0x100 + (b[10] & 0xff)) * 0x100 + (b[9] & 0xff))  * 0x100 + (b[8] & 0xff);
			val_msb = Integer.toUnsignedLong((int)val_msb);

			// least significant unsigned int
			long val_lsb = (((b[15] & 0xff) * 0x100 + (b[14] & 0xff)) * 0x100 + (b[13] & 0xff)) * 0x100 + (b[12] & 0xff);
			val_lsb = Integer.toUnsignedLong((int)val_lsb);

			// Java does not have unsigned long long, use BigInteger as bite array		
			BigInteger sqln = BigInteger.valueOf(val_msb);
			sqln = sqln.shiftLeft(32);
			sqln = sqln.add(BigInteger.valueOf(val_lsb));

			// Compute Base32, take 13x 5bits
			char alphabet [] = new String("0123456789abcdfghjkmnpqrstuvwxyz").toCharArray();
			result = ""; 
			for (int i = 0; i < 13; i++) // max sql_id length is 13 chars, 13 x 5 => 65bits most significant is always 0
			{
				int idx = sqln.and(BigInteger.valueOf(31)).intValue();
				result = alphabet[idx] + result;
				sqln = sqln.shiftRight(5);
			}
		} catch (Exception e) {
			
		}
		return result;
	}

	static String get(String originalSql) {
		OracleLexer l = new OracleLexer(originalSql);
		String actutalSql = l.getRevisedSql();			    	
		return SQL_ID(actutalSql);
	}
	
}
