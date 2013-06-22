package com.basicer.parchment.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Operation {
	String[] aliases() default {};
	String[] requires() default {};
	String desc() default "";
}
