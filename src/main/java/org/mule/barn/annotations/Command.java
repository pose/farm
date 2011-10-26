package org.mule.barn.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotate a method with this in order to process Command Line Arguments.
 * 
 * @author Alberto Pose <alberto.pose@mulesoft.com>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
}