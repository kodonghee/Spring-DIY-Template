package com.diy.framework.web.mvc.handler;

import com.diy.framework.web.beans.annotation.Controller;
import com.diy.framework.web.beans.factory.BeanFactory;
import com.diy.framework.web.mvc.annotation.RequestMapping;
import com.diy.framework.web.mvc.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AnnotationHandlerMapping implements HandlerMapping{

    private final BeanFactory beanFactory;
    private final Map<HandlerKey, HandlerExecution> handlerMap;

    public AnnotationHandlerMapping(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.handlerMap = new HashMap<>();
    }

    public void initialize() {
        for (Object bean : beanFactory.getBeans().values()) {
            Class<?> clazz = bean.getClass();

            if (!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }

            for (Method method : clazz.getDeclaredMethods()) {

                for (Annotation annotation : method.getAnnotations()) {
                    RequestMapping requestMapping = annotation.annotationType().getAnnotation(RequestMapping.class);
                    if (requestMapping == null) continue;

                    try {
                        String url = (String) annotation.annotationType().getMethod("value").invoke(annotation);
                        for (RequestMethod requestMethod : requestMapping.methods()) {
                            String httpMethod = requestMethod.name();
                            handlerMap.put(new HandlerKey(url, httpMethod), new HandlerExecution(bean, method));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public HandlerExecution getHandler(HttpServletRequest request) {
        String url = request.getRequestURI();
        String httpMethod = request.getMethod();
        return handlerMap.get(new HandlerKey(url, httpMethod));
    }
}
