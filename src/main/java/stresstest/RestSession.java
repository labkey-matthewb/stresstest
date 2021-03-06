package stresstest;

import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.filter.session.SessionFilter;
import io.restassured.http.Cookie;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * replace static RestAssured methods with instance methods, this lets us handle multiple user-agent sessions at the same time.
 *
 * see io.restassured.filter.session.SessionFilter
 */

public class RestSession
{
    final String baseURI;
    final int port;
    final String basePath;
    final ArrayList<Filter> filters = new ArrayList<Filter>();

    private RestSession(Map<String,String> props)
    {
        baseURI = defaultString(props.get("baseURI"), "http://localhost");
        port = Integer.parseInt(defaultString(props.get("port"), baseURI.startsWith("https:")?"443":"8080"));
        basePath = defaultString(props.get("basePath"), "");
        filters.add(new SessionFilter());
        System.err.println("URI: " + baseURI + ":" + port + basePath);
    }

    public static RestSession createSession(Map<String,String> props)
    {
        return new RestSession(props);
    }

    public static RestSession createLabKeySession(Map<String,String> props)
    {
        if (!props.containsKey("basePath"))
            props.put("basePath", "/labkey");
        var s = new RestSession(props);
        s.filters.add(new CsrfFilter());
        return s;
    }

    public static RestSession createBasicAuth(Map<String,String> props, String username_, String password_)
    {
        final String username = defaultString(username_, props.get("username"));
        final String password = defaultString(password_, props.get("password"));
        final Header header = new Header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)));
        return new RestSession(props)
        {
            @Override
            public RequestSpecification given(Stats stats)
            {
                return super.given(stats).header(header);
            }
        };
    }

    //
    //  replacements for RestAssured static methods
    //

    public RequestSpecification with()
    {
        return given(null);
    }

    public final RequestSpecification given()
    {
        return given(null);
    }

    public RequestSpecification given(Stats stats)
    {
        RestAssured.baseURI = baseURI;
        RestAssured.port = port;
        RestAssured.basePath = basePath;
        RestAssured.config().getSessionConfig().sessionIdName("JSESSIONID"); // default actually

//        var request = new RequestSpecificationWrapper(RestAssured.given());
        var request = RestAssured.given();
        request.filters(filters);
        if (null != stats)
            request.filters((req,res,ctx) -> {var ret = ctx.next(req,res); stats.update(ret.time()); return ret;});
        // for verbose logging
        //request.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        return request;
    }

    public Response get(String path, Object... pathParams)
    {
        return given().get(path, pathParams);
    }

    public Response get(String path, Map<String, ?> pathParams)
    {
        return given().get(path, pathParams);
    }

    public Response post(String path, Object... pathParams) {
        return given().post(path, pathParams);
    }

    static class CsrfFilter implements Filter
    {
        AtomicReference<Header> header = new AtomicReference<>();
        AtomicReference<Cookie> cookie = new AtomicReference<>();

        @Override
        public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx)
        {
            var h = header.get();
            if (null != h)
                requestSpec.header(h);
            var c = cookie.get();
            if (null != c)
                requestSpec.cookie(c);

            var response = ctx.next(requestSpec, responseSpec);

            var cookieValue = response.getCookie("X-LABKEY-CSRF");
            if (isNotBlank(cookieValue) && (null == h || !cookieValue.equals(h.getValue())))
            {
                header.set(new Header("X-LABKEY-CSRF", cookieValue));
                cookie.set(response.getDetailedCookie("X-LABKEY-CSRF"));
            }

            return response;
        }
    }

}

