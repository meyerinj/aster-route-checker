package io.asterisms.spike.event;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.MediaType;
import io.micronaut.http.uri.UriMatchTemplate;
import io.micronaut.management.endpoint.routes.RouteData;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import io.micronaut.web.router.MethodBasedRoute;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.UriRoute;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Requires(property = "asterisms.security.route.check", notEquals = StringUtils.FALSE)
public class RouteCheckStartupEventHandler implements ApplicationEventListener<ApplicationStartupEvent> {
    private final Logger logger = LoggerFactory.getLogger(RouteCheckStartupEventHandler.class);

    private final RouteData<Map<String, String>> routeData;
    private final Router router;
    private final List<String> managementEndpoints = List.of("/threaddump", "/health/{selector}", "/routes", "/health", "/beans", "/info", "/refresh");

    public RouteCheckStartupEventHandler(RouteData<Map<String, String>> routeData,
                                         Router router) {
        this.routeData = routeData;
        this.router = router;
    }

    @Override
    public void onApplicationEvent(ApplicationStartupEvent event) {
        router.uriRoutes()
                .forEach(uriRoute -> {
            UriMatchTemplate template = uriRoute.getUriMatchTemplate();
            String routeKey = getRouteKey(uriRoute);
            String path = template.toPathString();

            logger.trace(template.toPathString());
            if (!path.startsWith("/api") && !managementEndpoints.contains(path)) {
                String locationString = parseRouteLocation(routeData, uriRoute);
                logger.warn("{} found at {} does not start with '/api'", routeKey, locationString);
            }
        });
    }

    private String parseRouteLocation(RouteData<Map<String, String>> routeData, UriRoute uriRoute) {
        if (uriRoute instanceof MethodBasedRoute) {
            MethodBasedRoute mbr = (MethodBasedRoute) uriRoute;
            String controllerName = mbr.getTargetMethod().getDeclaringType().getName();
            String methodName = mbr.getTargetMethod().getMethodName();
            return controllerName + "." + methodName;
        }
        return routeData.getData(uriRoute).get("method");
    }

    /**
     * @param route The URI route
     * @return The route key
     */
    protected String getRouteKey(UriRoute route) {
        String produces = route
                .getProduces()
                .stream()
                .map(MediaType::toString)
                .collect(Collectors.joining(" || "));

        return new StringBuilder("{[")
                .append(route.getUriMatchTemplate())
                .append("],method=[")
                .append(route.getHttpMethodName())
                .append("],produces=[")
                .append(produces)
                .append("]}")
                .toString();
    }
}
