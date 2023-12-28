package com.qq.utils.Exception;

import com.qq.models.Result;
import com.qq.utils.Valid.ValidatorException;
import com.qq.utils.Valid.ValidatorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     *
     * @param
     * @return
     */
    @ExceptionHandler(BizException.class)
    public Result handleBizException(BizException bizException) {
        log.warn("业务异常:{}", bizException.getMessage(), bizException);
        ExceptionCodeEnum error = bizException.getError();
        return Result.error().code(error.getCode()).message(error.getDesc());
    }

    /**
     * 运行时异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRunTimeException(RuntimeException e) {
        log.warn("运行时异常: {}", e.getMessage(), e);
        return Result.error().code(ExceptionCodeEnum.ERROR.getCode()).message(ExceptionCodeEnum.ERROR.getDesc());
    }

    /**
     * ValidatorUtils校验异常
     *
     * @param e
     * @return
     * @see ValidatorUtils
     */
    @ExceptionHandler(ValidatorException.class)
    public Result handleValidatorException(ValidatorException e) {
        // 打印精确的参数错误日志，方便后端排查
        log.warn("参数校验异常: {}", e.getMessage(), e);
        // 一般来说，给客户端展示泛化的错误信息即可，联调时可以返回精确的信息
        return Result.error().message(e.getMessage());
    }

    /**
     * ConstraintViolationException异常（散装GET参数校验）
     *
     * @param e
     * @return
     * @see ValidatorUtils
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("参数错误: {}", e.getMessage(), e);
        return Result.error().code(ExceptionCodeEnum.ERROR_PARAM.getCode()).message(ExceptionCodeEnum.ERROR_PARAM.getDesc());
    }

    /**
     * BindException异常（GET DTO校验）
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    public Result validationBindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(" && "));
        log.error("参数错误: {}", message, e);
        return Result.error().code(ExceptionCodeEnum.ERROR_PARAM.getCode()).message(ExceptionCodeEnum.ERROR_PARAM.getDesc());
    }

    /**
     * MethodArgumentNotValidException异常（POST DTO校验）
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result validationMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(" && "));
        log.error("参数错误: {}", message, e);
        return Result.error().code(ExceptionCodeEnum.ERROR_PARAM.getCode()).message(ExceptionCodeEnum.ERROR_PARAM.getDesc());
    }

}
