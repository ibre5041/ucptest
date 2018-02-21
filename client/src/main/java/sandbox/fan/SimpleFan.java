// https://martincarstenbach.wordpress.com/2011/05/26/how-to-use-the-rac-fan-java-api/

package sandbox.fan;

import oracle.simplefan.FanSubscription;
import oracle.simplefan.FanEventListener;
import oracle.simplefan.FanManager;
import oracle.simplefan.LoadAdvisoryEvent;
import oracle.simplefan.NodeDownEvent;
import oracle.simplefan.ServiceDownEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SimpleFan {

	public static Properties loadProperties() {
		Properties props = new Properties();
		try {
			InputStream is = SimpleFan.class.getResourceAsStream("/database.properties");
			if (is != null) {
				props.load(is);
				is.close();
			}
		}
		catch(IOException ioe) {
			System.err.println("IOException in loadProps");
			for(StackTraceElement ste : ioe.getStackTrace())
				System.err.println(ste.toString());
			System.exit(-1);
		}
		return props;
	}

	SimpleFan() {
		System.out.println("Hello");

		{
			Properties props = loadProperties();

			String oraclehome = props.getProperty("oracle.ons.oraclehome");
			if (oraclehome == null)
				oraclehome = System.getenv("ORACLE_HOME");
			System.setProperty("oracle.ons.oraclehome", oraclehome);
			System.out.println(System.getProperty("oracle.ons.oraclehome"));
		}
		
		Properties fanProperties = new Properties();
		fanProperties.put("serviceName", "ACTEST");
		FanSubscription sub = FanManager.getInstance().subscribe(fanProperties);

		System.out.println("I'm subscribed!");

		sub.addListener(new FanEventListener() {

			public void handleEvent(ServiceDownEvent arg0) {
				System.out.println("Service Down registered!");
			}

			public void handleEvent(NodeDownEvent arg0) {
				System.out.println("Node Down Event Registered");
			}

			public void handleEvent(LoadAdvisoryEvent arg0) {
				System.out.println("Just got a Load Advisory event");

				System.out.println("originating database: " + arg0.getDatabaseUniqueName());
				System.out.println("originating instance: " + arg0.getInstanceName());
				System.out.println("Service Quality     : " + arg0.getServiceQuality());
				System.out.println("Percent             : " + arg0.getPercent());
				System.out.println("Service Name        : " + arg0.getServiceName());
				System.out.println("Service Quality     : " + arg0.getServiceQuality());
				System.out.println("Observed at         : " + arg0.getTimestamp() + "\n\n");
			} } );
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimpleFan tc = new SimpleFan();

		int i = 0;
		while ( i < 100000)  {
			try {
				Thread.sleep(100);
				i++;
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		System.out.println("execution ended");
	}

}
