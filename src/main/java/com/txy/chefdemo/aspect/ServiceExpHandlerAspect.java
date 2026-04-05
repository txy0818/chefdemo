package com.txy.chefdemo.aspect;


import com.txy.chefdemo.exp.BusinessException;
import com.txy.chefdemo.resp.BaseResp;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.utils.ObjectMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StopWatch;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Component
@Aspect
@Slf4j
@Order(1000)
public class ServiceExpHandlerAspect {
    // 方法上的注解
    @Pointcut("@annotation(com.txy.chefdemo.aspect.LogExecution)")
    private void pointCut() {
    }

    @Around("pointCut()")
    public Object serviceExceptionHandler(ProceedingJoinPoint joinPoint)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Object res;
        // 方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取该注解
        LogExecution annotation = signature.getMethod().getAnnotation(LogExecution.class);
        // 计时器
        final StopWatch stopWatch = new StopWatch();
        try {
            if (annotation.logTime()) {
                stopWatch.start();
            }
            res = joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("统一异常处理,入参:{} ", ObjectMapperUtils.toJSON(Arrays.stream(joinPoint.getArgs())
                    .map(LogArgFilter::filter)), throwable);
          	// 创建出返回值
            res = ReflectionUtils.accessibleConstructor(annotation.returnType()).newInstance();

            Field responseFiled;
            Field[] fields = annotation.returnType().getDeclaredFields();
            for (Field field : fields) {
                if (BaseResp.class.isAssignableFrom(field.getType())) {
                    ReflectionUtils.makeAccessible(field);
                    BaseResp responseInfo = buildBaseResp(throwable);
                    responseFiled = field;
                    ReflectionUtils.setField(responseFiled, res, responseInfo);
                    break;
                }
            }
        } finally {
            if (annotation.logTime()) {
                stopWatch.stop();
                log.info("接口总耗时:{} 毫秒", stopWatch.getTotalTimeMillis());
            }
        }
        return res;
    }

    private BaseResp buildBaseResp(Throwable throwable) {
        String message = throwable.getMessage();
        if (throwable instanceof BusinessException || throwable instanceof IllegalArgumentException) {
            if ("无权限".equals(message)) {
                return BaseRespConstant.FORBIDDEN;
            }
            if (BaseRespConstant.AUDIT_NOT_PASS.getDesc().equals(message)) {
                return BaseRespConstant.AUDIT_NOT_PASS;
            }
            return BaseRespConstant.failUnknown(message);
        }
        return BaseRespConstant.failUnknown(message != null ? message : "系统异常");
    }
}
