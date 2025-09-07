package com.rss.core.common.config;

import com.rss.core.account.infrastructure.security.UserPrincipal;
import com.rss.core.common.annotation.CurrentDriverId;
import com.rss.core.common.annotation.CurrentRiderId;
import com.rss.core.common.annotation.CurrentUserId;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new NonNullCurrentIdResolver());
    }

    static class NonNullCurrentIdResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return (parameter.hasParameterAnnotation(CurrentDriverId.class)
                    || parameter.hasParameterAnnotation(CurrentRiderId.class)
                    || parameter.hasParameterAnnotation(CurrentUserId.class))
                    && Long.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, org.springframework.web.context.request.NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
                throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Unauthenticated: principal not found");
            }

            Long value;
            if (parameter.hasParameterAnnotation(CurrentDriverId.class)) {
                value = principal.getDriverId();
            } else if (parameter.hasParameterAnnotation(CurrentRiderId.class)) {
                value = principal.getRiderId();
            } else {
                value = principal.getUserId();
            }

            if (value == null) {
                // If authenticated but role/id not matching the annotation, treat as forbidden
                throw new org.springframework.security.access.AccessDeniedException("Required identity not present for this user");
            }
            return value;
        }
    }
}
