package com.stu.socialnetworkapi.validation.annotation;

import com.stu.socialnetworkapi.validation.validator.ValidFileListValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidFileListValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFileList {
    String message() default "LIST_CONTAINS_INVALID_FILE";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
