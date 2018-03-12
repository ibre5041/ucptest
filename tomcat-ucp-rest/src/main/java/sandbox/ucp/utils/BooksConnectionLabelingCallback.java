package sandbox.ucp.utils;

import java.util.Properties;
import java.util.Map;
import java.util.Set;
import java.sql.Connection;
import oracle.ucp.ConnectionLabelingCallback;
import oracle.ucp.jdbc.LabelableConnection;

// https://blogs.oracle.com/weblogicserver/data-source-connection-labeling

public class BooksConnectionLabelingCallback implements ConnectionLabelingCallback {
	public BooksConnectionLabelingCallback() {
	}  
	
	/*
	 *   Projects the cost of configuring connections considering 
	 *   label-matching differences.  
	 */
	public int cost(Properties reqLabels, Properties currentLabels) {
		// Case 1: exact match
		if (reqLabels.equals(currentLabels)) {
			System.out.println("## Exact match found!! ##");
			return 0;
		}

		// Case 2: Partial match where some labels match with current labels
		String iso1 = (String) reqLabels.get("TRANSACTION_ISOLATION");
		String iso2 = (String) currentLabels.get("TRANSACTION_ISOLATION");
		boolean match = (iso1 != null && iso2 != null && iso1.equalsIgnoreCase(iso2));
		Set rKeys = reqLabels.keySet();
		Set cKeys = currentLabels.keySet();
		if (match && rKeys.containsAll(cKeys)) {
			System.out.println("## Partial match found!! ##");
			return 10;
		}
		// Case 3: No match 
		// Do not choose this connection.
		System.out.println("## No match found!! ##");
		return Integer.MAX_VALUE;
	}

	/*
	 *  Configures the selected connection for a borrowing request before 
	 *  returning the connection to the application.
	 */
	public boolean configure(Properties reqLabels, Object conn) {
		try {
			String isoStr = (String) reqLabels.get("TRANSACTION_ISOLATION");
			((Connection) conn).setTransactionIsolation(Integer.valueOf(isoStr));

			LabelableConnection lconn = (LabelableConnection) conn;

			// Find the unmatched labels on this connection
			Properties unmatchedLabels = lconn.getUnmatchedConnectionLabels(reqLabels);

			// Apply each label <key,value> in unmatchedLabels to connection
			for (Map.Entry<Object, Object> label : unmatchedLabels.entrySet()) {
				String key = (String) label.getKey();
				String value = (String) label.getValue();
				lconn.applyConnectionLabel(key, value);
			}
		}
		catch (Exception exc) {
			return false;
		}
		return true;
	}
} 
