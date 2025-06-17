package com.mjc.school.aspect;

import com.mjc.school.annotation.Valid;
import com.mjc.school.exception.ValidationException;
import com.mjc.school.validator.ConstraintViolation;
import com.mjc.school.validator.Validator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.stream.Stream;

import static com.mjc.school.exception.ExceptionErrorCodes.VALIDATION_EXCEPTION;

@Aspect
@Component
public class ValidationAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationAspect.class);

    private final Validator validator;

    @Autowired
    public ValidationAspect(Validator validator) {
        this.validator = validator;
    }

    @Pointcut(value = "execution(public * * (.., @com.mjc.school.annotation.Valid (*), ..))")
    public void validAnnotation() {
    }

    @Before(value = "validAnnotation()")
    public void validateBeforeExecuting(JoinPoint joinPoint) throws NoSuchMethodException {
        LOGGER.info("Validating args");
        if (joinPoint.getSignature() instanceof MethodSignature signature) {
            Method targetMethod = getTargetMethod(joinPoint, signature);
            var args = joinPoint.getArgs();
            var parameterAnnotations = targetMethod.getParameterAnnotations();
            var violations = new HashSet<ConstraintViolation>();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                if (requiredValidation(parameterAnnotations[i])) {
                    violations.addAll(validator.validate(args[i]));
                }
            }
            if (!violations.isEmpty()) {
                LOGGER.error("Validation failed: {}", violations);
                throw new ValidationException(String.format(VALIDATION_EXCEPTION.getErrorMessage(), violations.stream().findFirst().get().message()));
            }
        }
    }

    private Method getTargetMethod(JoinPoint joinPoint, MethodSignature signature) throws NoSuchMethodException {
        Method method = signature.getMethod();
        return joinPoint.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
    }

    private boolean requiredValidation(Annotation[] annotations) {
        return Stream.of(annotations).anyMatch(Valid.class::isInstance);
    }
}
