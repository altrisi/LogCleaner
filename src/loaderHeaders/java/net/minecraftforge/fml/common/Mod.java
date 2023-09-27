package net.minecraftforge.fml.common;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 1.8+
 * 
 * @author MinecraftForge
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Mod {
	/**
	 * @since at some point, 1.13 I'd say
	 */
	String value();
	
	/**
	 * @until 1.13 probably
	 */
	String modid();
	
	/**
	 * @until 1.13 probably
	 */
	String acceptableRemoteVersions();
}
