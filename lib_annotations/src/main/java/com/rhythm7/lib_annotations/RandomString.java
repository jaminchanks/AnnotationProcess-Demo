package com.rhythm7.lib_annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by Jaminchanks on 2018-02-10.
 */
@Retention(CLASS)
@Target(value = FIELD)
public @interface RandomString {
}
