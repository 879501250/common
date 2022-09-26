package com.qq.utils.Valid;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.security.InvalidParameterException;
import java.util.Set;

/**
 * 更加自由，调用者决定抛异常还是返回错误信息
 */
public final class ValidationUtils {

    private static final Validator DEFAULT_VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private ValidationUtils() {
    }

    /**
     * 验证基于注解的对象
     *
     * @param target
     */
    public static <T> String validateReq(T target, boolean throwException) {
        if (null == target) {
            return errorProcess("校验对象不能为空", throwException);
        } else {
            Set<ConstraintViolation<T>> constraintViolations = DEFAULT_VALIDATOR.validate(target);
            ConstraintViolation<T> constraintViolation = Iterables.getFirst(constraintViolations, null);
            if (constraintViolation != null) {
                // 用户可以指定抛异常还是返回错误信息
                return errorProcess(constraintViolation.getPropertyPath() + ":" + constraintViolation.getMessage(),
                        throwException);
            }
        }
        return "";
    }

    private static String errorProcess(String errorMsg, boolean throwException) {
        if (throwException) {
            throw new InvalidParameterException(errorMsg);
        }
        return errorMsg;
    }
}
