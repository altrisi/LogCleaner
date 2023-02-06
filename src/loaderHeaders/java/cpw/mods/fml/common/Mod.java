package cpw.mods.fml.common;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Up to 1.7.10
 * @author MinecraftForge
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Mod {
	String modid();
}
