package org.mule.barn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.lang3.Validate;
import org.mule.barn.annotations.Command;
import org.mule.barn.annotations.OnException;

/**
 * Annotation based Command-Line Interface.
 * 
 * @author Alberto Pose <alberto.pose@mulesoft.com>
 * 
 * @param <T>
 *            Type of the annotated class.
 */
public class Barn<T> {

	/**
	 * Invalid parameters return code.
	 */
	public static final int INVALID_PARAMETERS = -1;

	/**
	 * Success return code.
	 */
	public static final int SUCCESS = 0;

	/**
	 * Annotated instance.
	 */
	private T instance;

	/**
	 * Annotated instance class.
	 */
	private Class<?> klass;

	/**
	 * sh script name (in order to use it in help)
	 */
	private String appName;

	/**
	 * Registers the instance for Barn parsing.
	 * 
	 * @param instance
	 *            annotated instance for Barn parsing
	 */
	public Barn(T instance, String appName) {
		Validate.notNull(instance);
		Validate.notNull(appName);
		Validate.notBlank(appName);

		this.appName = appName;
		this.instance = instance;
		this.klass = instance.getClass();
	}

	/**
	 * Execute the command line parser using the specified arguments. Return
	 * code is != 0 in case of error.
	 * 
	 * @param args
	 *            Command-Line Arguments
	 * @return the return code that the process should return.
	 */
	public int runCommandLine(String[] args) {
		Validate.notNull(args);
		if (args.length == 0) {
			help();
			return SUCCESS;
		}

		for (Method method : klass.getMethods()) {
			if (method.isAnnotationPresent(Command.class)
					&& method.getName().equals(args[0])
					&& method.getParameterTypes().length == args.length - 1) {
				try {
					method.invoke(instance,
							(Object[]) Arrays.copyOfRange(args, 1, args.length));
					return SUCCESS;
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					if (method.isAnnotationPresent(OnException.class)) {
						OnException annotation = method
								.getAnnotation(OnException.class);
						if (annotation != null
								&& annotation.value().equals(
										e.getCause().getClass())) {
							System.err.println(annotation.message());
							return annotation.returnCode();
						}
					}
					throw new RuntimeException(e);
				}
			}
		}
		help();
		return SUCCESS;
	}

	public void help() {
		System.err.println("Usage: " + appName + " <command> [<args>]");
		
		
		for (Method method : klass.getMethods()) {
			
			if (method.isAnnotationPresent(Command.class)) {
				StringBuffer stringBuffer = new StringBuffer();
				for (int i = 0; i < method.getParameterTypes().length; i++) {
					stringBuffer.append(" <param").append(i).append(">");
				}
				
				System.err.println(String.format("   %-7s %s", method.getName(),
						stringBuffer.toString()));
			}
		}
		
	}

}