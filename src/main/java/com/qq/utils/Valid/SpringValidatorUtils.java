package com.qq.utils.Valid;

import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * 做的事情本质是和 @Validated 是一模一样的。
 * @Validated 通过注解方式让 Spring 使用 Validator 帮我们校验，
 * 而 SpringValidatorUtils 则是我们从 Spring 那借来 Validator 自己校验：
 * @PostMapping("insertUser")
 * public Result<Boolean> insertUser(@RequestBody User user) {
 *     SpringValidatorUtils.validate(user);
 *     System.out.println("进来了");
 *     return Result.success(null);
 * }
 * 此时不需要加 @Validated
 */
public final class SpringValidatorUtils {
    private SpringValidatorUtils() {}

    /**
     * 校验器
     */
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 校验参数
     *
     * @param param  待校验的参数
     * @param groups 分组校验，比如Update.class（可以不传）
     * @param <T>
     */
    public static <T> void validate(T param, Class<?>... groups) {
        Set<ConstraintViolation<T>> validateResult = validator.validate(param, groups);
        if (!CollectionUtils.isEmpty(validateResult)) {
            StringBuilder validateMessage = new StringBuilder();
            for (ConstraintViolation<T> constraintViolation : validateResult) {
                validateMessage.append(constraintViolation.getMessage()).append(" && ");
            }
            // 去除末尾的 &&
            validateMessage.delete(validateMessage.length() - 4, validateMessage.length());
            // 抛给全局异常处理
            throw new ValidatorException(validateMessage.toString());
        }
    }
}