package app.clientpkg;

import app.otherpkg.OtherPkgHelper;
import bar.Middle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class Client {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// method in same class
		clientMethod();
		
		// method in different class, same package
		Helper h = new Helper();
		h.helperMethod();
		
		// method in different package
		OtherPkgHelper oph = new OtherPkgHelper();
		System.out.println("Calling OtherPkgHelper.otherPkgHelperMethod in different package - " + oph.otherPkgHelperMethod());
		
		// method in different library
		Middle m = new Middle();
		System.out.println("Calling Middle.getValue in different library - " + m.getValue());
		System.out.println("Calling Middle.getValue in different library - " + m.getValue());
	
		Logger logger = LogManager.getLogger(Client.class);
		logger.info("Log4j logging example");
		
		org.slf4j.Logger logger2 = LoggerFactory.getLogger("ExampleLogger");
	
	}
	
	public static void clientMethod() {
		System.out.println("Client.clientMethod, same pkg as Client");
	}

}
