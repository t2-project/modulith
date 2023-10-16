package de.unistuttgart.t2.modulith.common.scaling.request;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Middleware to allow to deterministically trigger {@code SLO}s without having to shutdown the server.
 * <p>
 * This component has to ignore the principles of OOP programming as Spring does not seem to be able to autowire
 * interceptors.<br>
 * Also, in this case setting this globally makes sense as there is no reason to have a deterministic SLO that only
 * affects part of the application.
 *
 * @author Leon Hofmeister
 */
public final class RequestDenier implements HandlerInterceptor {

    private static volatile boolean blockRoutes;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws Exception {
        if (blockRoutes) {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value());
        }
        return !blockRoutes;
    }

    public static void shouldBlockAllRoutes(boolean block) {
        blockRoutes = block;
    }
}
