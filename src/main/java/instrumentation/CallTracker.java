package instrumentation;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.JoinPoint.EnclosingStaticPart;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class CallTracker{
	public static Map<String, List<String>> libsToPkgs = new HashMap<String, List<String>>();
	public static Map<Map<String, String>, Integer> noOfCallsAcrossLibs = new HashMap<Map<String, String>, Integer>();
	public static DatabaseConnector connector;
	public static List<String> excludeMethods;

	CallTracker() {
		connector = DatabaseConnector.buildDatabaseConnector();
		connector.connect();
		libsToPkgs = connector.getLibsToPkgs();
		excludeMethods = connector.getListOfExcludedMethods();
	}

	@Pointcut("call(* *(..)) && !within(instrumentation.*) && if() && !cflow(call(* java*(..)))")
	public static boolean getCalls(JoinPoint jp, final EnclosingStaticPart thisEnclosingJoinPoint){ //pointcut name
		String completeCallerMethodName = thisEnclosingJoinPoint.getSignature().getDeclaringType().getName()
				+ "." + thisEnclosingJoinPoint.getSignature().getName();
		//String completeCalledMethodName = jp.getSignature().getDeclaringType().getName()
		//		+ "." + jp.getSignature().getName();
		if (excludeMethods.contains(completeCallerMethodName))
			return false;
		return true;
	}
	
	@Before("getCalls(jp, thisEnclosingJoinPoint)")//applying pointcut on before advice
	public void callsAdvice(JoinPoint jp, final EnclosingStaticPart thisEnclosingJoinPoint) // advice
	{
		Class<?> methodCallerClass = thisEnclosingJoinPoint.getSignature().getDeclaringType();
		Class<?> methodCalledClass = jp.getSignature().getDeclaringType();
		Entry<String, List<String>> unknownEntry = new AbstractMap.SimpleEntry<String, List<String>>("unknownLib", new ArrayList<String>());
		String callingMethodLibName = "";
		String calledMethodLibName = "";
		if (methodCallerClass.getPackage()==null || methodCalledClass.getPackage()==null)
			return;
		try {
			callingMethodLibName = libsToPkgs.entrySet().stream()
			.filter(map -> map.getValue().contains(methodCallerClass.getPackage().getName()))
			.findAny().orElse(unknownEntry).getKey();
			calledMethodLibName = libsToPkgs.entrySet().stream()
			.filter(map -> map.getValue().contains(methodCalledClass.getPackage().getName()))
			.findAny().orElse(unknownEntry).getKey();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		if (!callingMethodLibName.equals(calledMethodLibName)) {
				String callingMethodName = thisEnclosingJoinPoint.getSignature().getName();
				String callingClassName = methodCallerClass.getName();
				String callingDescriptorName = "";
				String calledMethodName = jp.getSignature().getName();
				String calledClassName = methodCalledClass.getName();
				String calledDescriptorName = "";

				try {
					callingDescriptorName = NameUtility.getDescriptor(((MethodSignature)thisEnclosingJoinPoint.getSignature()).getMethod());
				} catch (ClassCastException e) {
					try {
						callingDescriptorName = NameUtility.getDescriptor(((ConstructorSignature)thisEnclosingJoinPoint.getSignature()).getConstructor());
					} catch (Exception ex) {
						System.out.println("Error while casting to Signature "+ex.toString());
					}
				}
				try {
					calledDescriptorName = NameUtility.getDescriptor(((MethodSignature)jp.getSignature()).getMethod());
				} catch (ClassCastException e) {
					try {
						calledDescriptorName = NameUtility.getDescriptor(((ConstructorSignature)jp.getSignature()).getConstructor());
					} catch (Exception ex) {
						System.out.println("Error while casting to Signature "+ex.toString());
					}
				}			
				connector.updateCountInCallerCalleeCountTable(callingClassName+"::"+callingMethodName+callingDescriptorName,
						callingMethodLibName, calledClassName+"::"+calledMethodName+calledDescriptorName, 
						calledMethodLibName, 1);
		}
	}
}
