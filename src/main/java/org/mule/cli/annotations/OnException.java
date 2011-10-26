package org.mule.cli.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotate a method using this together with Command in order to specify the
 * behavior when that exception is thrown.
 * 
 * @author Alberto Pose <alberto.pose@mulesoft.com>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OnException {
	/**
	 * Exception to be handled.
	 */
	Class<? extends Exception> value();

	/**
	 * Message to output to stedrr.
	 */
	String message();

	/**
	 * Return code that the CLI must return in case this happens. 
	 */
	int returnCode();
}