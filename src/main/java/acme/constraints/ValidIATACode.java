
package acme.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@ReportAsSingleViolation

@NotBlank
@Length(min = 3, max = 3)
@Pattern(regexp = "^[A-Z]{3}$")
public @interface ValidIATACode {

	// Standart validation properties ---------------------------

	String message() default "IATACode not valid";

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
