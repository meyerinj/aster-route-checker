package io.asterisms.spike.endpoints;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/incorrect")
public class IncorrectController {

    @Get
    public HttpResponse<String> doAThing() {
        return HttpResponse.ok("Good job!");
    }
}
