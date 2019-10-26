
// HelloServer.java
// Copyright and License 
import HelloApp.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.io.Console;
import java.util.Properties;

class HelloImpl extends HelloPOA {

	private ORB orb;

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}

	// implement sayHello() method
	public String sayHello(int tableNumber, int printTime ) {
		String tableString = "";


		for (int i = tableNumber; i < tableNumber + printTime; i++) {

			MyThread t1 = new MyThread(i);
			t1.start();
			try {
				t1.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tableString += "Table of " + i  + " : ";
			tableString += t1.returnString() + "\n\n";
		}

		return tableString;
	}

	// implement shutdown() method
	public void shutdown() {
		orb.shutdown(false);
	}
}

class MyThread extends Thread {
	public volatile String myStr= "";
	public int TableNumber;

	public MyThread(int number) {
		this.myStr = "";
		this.TableNumber = number;
	}

	public void run() {
		synchronized(this)
		{
			for (int i = 1; i < 10; i++) {
				System.out.print(TableNumber + "*" + i + "=" + (TableNumber * i) + "\n");
				myStr += TableNumber + "*" + i + "=" + (TableNumber * i) + "\n";
			}
		}
	}

	public String returnString() {
		return myStr;
	}
}

public class HelloServer {

	public static void main(String args[]) {
		try {
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			HelloImpl helloImpl = new HelloImpl();
			helloImpl.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(helloImpl);
			Hello href = HelloHelper.narrow(ref);

			// get the root naming context
			// NameService invokes the name service
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			// Use NamingContextExt which is part of the Interoperable
			// Naming Service (INS) specification.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// bind the Object Reference in Naming
			String name = "Hello";
			NameComponent path[] = ncRef.to_name(name);
			ncRef.rebind(path, href);

			System.out.println("HelloServer ready and waiting ...");

			// wait for invocations from clients
			orb.run();
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println("HelloServer Exiting ...");

	}
}