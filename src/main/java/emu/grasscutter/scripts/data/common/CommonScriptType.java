package emu.grasscutter.scripts.data.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommonScriptType {
    /** Names of the file that this common script loads from */
    String name();
}
