package io.asterisms.spike.endpoints;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller
public class AlsoValidController {

    @Get("/api/correct")
    public HttpResponse<String> doAThing() {
        return HttpResponse.ok("Good job!");
    }
}
