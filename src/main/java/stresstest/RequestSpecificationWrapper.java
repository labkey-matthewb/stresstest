package stresstest;

import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.http.*;
import io.restassured.mapper.ObjectMapper;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import io.restassured.specification.*;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RequestSpecificationWrapper implements RequestSpecification
{
    final RequestSpecification wrapped;

    RequestSpecificationWrapper(RequestSpecification rs)
    {
        wrapped = rs;
    }

    protected Response afterRequest(Response response)
    {
        return response;
    }

    @Override
    public RequestSpecification body(String body) {
        return wrapped.body(body);
    }

    @Override
    public RequestSpecification body(byte[] body) {
        return wrapped.body(body);
    }

    @Override
    public RequestSpecification body(File body) {
        return wrapped.body(body);
    }

    @Override
    public RequestSpecification body(InputStream body) {
        return wrapped.body(body);
    }

    @Override
    public RequestSpecification body(Object object) {
        return wrapped.body(object);
    }

    @Override
    public RequestSpecification body(Object object, ObjectMapper mapper) {
        return wrapped.body(object, mapper);
    }

    @Override
    public RequestSpecification body(Object object, ObjectMapperType mapperType) {
        return wrapped.body(object, mapperType);
    }

    @Override
    public RedirectSpecification redirects() {
        return wrapped.redirects();
    }

    @Override
    public RequestSpecification cookies(String firstCookieName, Object firstCookieValue, Object... cookieNameValuePairs) {
        return wrapped.cookies(firstCookieName, firstCookieValue, cookieNameValuePairs);
    }

    @Override
    public RequestSpecification cookies(Map<String, ?> cookies) {
        return wrapped.cookies(cookies);
    }

    @Override
    public RequestSpecification cookies(Cookies cookies) {
        return wrapped.cookies(cookies);
    }

    @Override
    public RequestSpecification cookie(String cookieName, Object value, Object... additionalValues) {
        return wrapped.cookie(cookieName, value, additionalValues);
    }

    @Override
    public RequestSpecification cookie(String cookieName) {
        return wrapped.cookie(cookieName);
    }

    @Override
    public RequestSpecification cookie(Cookie cookie) {
        return wrapped.cookie(cookie);
    }

    @Override
    public RequestSpecification params(String firstParameterName, Object firstParameterValue, Object... parameterNameValuePairs) {
        return wrapped.params(firstParameterName, firstParameterValue, parameterNameValuePairs);
    }

    @Override
    public RequestSpecification params(Map<String, ?> parametersMap) {
        return wrapped.params(parametersMap);
    }

    @Override
    public RequestSpecification param(String parameterName, Object... parameterValues) {
        return wrapped.param(parameterName, parameterValues);
    }

    @Override
    public RequestSpecification param(String parameterName, Collection<?> parameterValues) {
        return wrapped.param(parameterName, parameterValues);
    }

    @Override
    public RequestSpecification queryParams(String firstParameterName, Object firstParameterValue, Object... parameterNameValuePairs) {
        return wrapped.queryParams(firstParameterName, firstParameterValue, parameterNameValuePairs);
    }

    @Override
    public RequestSpecification queryParams(Map<String, ?> parametersMap) {
        return wrapped.queryParams(parametersMap);
    }

    @Override
    public RequestSpecification queryParam(String parameterName, Object... parameterValues) {
        return wrapped.queryParam(parameterName, parameterValues);
    }

    @Override
    public RequestSpecification queryParam(String parameterName, Collection<?> parameterValues) {
        return wrapped.queryParam(parameterName, parameterValues);
    }

    @Override
    public RequestSpecification formParams(String firstParameterName, Object firstParameterValue, Object... parameterNameValuePairs) {
        return wrapped.formParams(firstParameterName, firstParameterValue, parameterNameValuePairs);
    }

    @Override
    public RequestSpecification formParams(Map<String, ?> parametersMap) {
        return wrapped.formParams(parametersMap);
    }

    @Override
    public RequestSpecification formParam(String parameterName, Object... parameterValues) {
        return wrapped.formParam(parameterName, parameterValues);
    }

    @Override
    public RequestSpecification formParam(String parameterName, Collection<?> parameterValues) {
        return wrapped.formParam(parameterName, parameterValues);
    }

    @Override
    public RequestSpecification pathParam(String parameterName, Object parameterValue) {
        return wrapped.pathParam(parameterName, parameterValue);
    }

    @Override
    public RequestSpecification pathParams(String firstParameterName, Object firstParameterValue, Object... parameterNameValuePairs) {
        return wrapped.pathParams(firstParameterName, firstParameterValue, parameterNameValuePairs);
    }

    @Override
    public RequestSpecification pathParams(Map<String, ?> parameterNameValuePairs) {
        return wrapped.pathParams(parameterNameValuePairs);
    }

    @Override
    public RequestSpecification config(RestAssuredConfig config) {
        return wrapped.config(config);
    }

    @Override
    public RequestSpecification keyStore(String pathToJks, String password) {
        return wrapped.keyStore(pathToJks, password);
    }

    @Override
    public RequestSpecification keyStore(File pathToJks, String password) {
        return wrapped.keyStore(pathToJks, password);
    }

    @Override
    public RequestSpecification trustStore(String path, String password) {
        return wrapped.trustStore(path, password);
    }

    @Override
    public RequestSpecification trustStore(File path, String password) {
        return wrapped.trustStore(path, password);
    }

    @Override
    public RequestSpecification trustStore(KeyStore trustStore) {
        return wrapped.trustStore(trustStore);
    }

    @Override
    public RequestSpecification keyStore(KeyStore keyStore) {
        return wrapped.keyStore(keyStore);
    }

    @Override
    public RequestSpecification relaxedHTTPSValidation() {
        return wrapped.relaxedHTTPSValidation();
    }

    @Override
    public RequestSpecification relaxedHTTPSValidation(String protocol) {
        return wrapped.relaxedHTTPSValidation(protocol);
    }

    @Override
    public RequestSpecification headers(String firstHeaderName, Object firstHeaderValue, Object... headerNameValuePairs) {
        return wrapped.headers(firstHeaderName, firstHeaderValue, headerNameValuePairs);
    }

    @Override
    public RequestSpecification headers(Map<String, ?> headers) {
        return wrapped.headers(headers);
    }

    @Override
    public RequestSpecification headers(Headers headers) {
        return wrapped.headers(headers);
    }

    @Override
    public RequestSpecification header(String headerName, Object headerValue, Object... additionalHeaderValues) {
        return wrapped.header(headerName, headerValue, additionalHeaderValues);
    }

    @Override
    public RequestSpecification header(Header header) {
        return wrapped.header(header);
    }

    @Override
    public RequestSpecification contentType(ContentType contentType) {
        return wrapped.contentType(contentType);
    }

    @Override
    public RequestSpecification contentType(String contentType) {
        return wrapped.contentType(contentType);
    }

    @Override
    public RequestSpecification accept(ContentType contentType) {
        return wrapped.accept(contentType);
    }

    @Override
    public RequestSpecification accept(String mediaTypes) {
        return wrapped.accept(mediaTypes);
    }

    @Override
    public RequestSpecification multiPart(MultiPartSpecification multiPartSpecification) {
        return wrapped.multiPart(multiPartSpecification);
    }

    @Override
    public RequestSpecification multiPart(File file) {
        return wrapped.multiPart(file);
    }

    @Override
    public RequestSpecification multiPart(String controlName, File file) {
        return wrapped.multiPart(controlName, file);
    }

    @Override
    public RequestSpecification multiPart(String controlName, File file, String mimeType) {
        return wrapped.multiPart(controlName, file, mimeType);
    }

    @Override
    public RequestSpecification multiPart(String controlName, Object object) {
        return wrapped.multiPart(controlName, object);
    }

    @Override
    public RequestSpecification multiPart(String controlName, Object object, String mimeType) {
        return wrapped.multiPart(controlName, object, mimeType);
    }

    @Override
    public RequestSpecification multiPart(String controlName, String filename, Object object, String mimeType) {
        return wrapped.multiPart(controlName, filename, object, mimeType);
    }

    @Override
    public RequestSpecification multiPart(String controlName, String fileName, byte[] bytes) {
        return wrapped.multiPart(controlName, fileName, bytes);
    }

    @Override
    public RequestSpecification multiPart(String controlName, String fileName, byte[] bytes, String mimeType) {
        return wrapped.multiPart(controlName, fileName, bytes, mimeType);
    }

    @Override
    public RequestSpecification multiPart(String controlName, String fileName, InputStream stream) {
        return wrapped.multiPart(controlName, fileName, stream);
    }

    @Override
    public RequestSpecification multiPart(String controlName, String fileName, InputStream stream, String mimeType) {
        return wrapped.multiPart(controlName, fileName, stream, mimeType);
    }

    @Override
    public RequestSpecification multiPart(String controlName, String contentBody) {
        return wrapped.multiPart(controlName, contentBody);
    }

    @Override
    public RequestSpecification multiPart(String controlName, String contentBody, String mimeType) {
        return wrapped.multiPart(controlName, contentBody, mimeType);
    }

    @Override
    public AuthenticationSpecification auth() {
        return wrapped.auth();
    }

    @Override
    public RequestSpecification port(int port) {
        return wrapped.port(port);
    }

    @Override
    public RequestSpecification spec(RequestSpecification requestSpecificationToMerge) {
        return wrapped.spec(requestSpecificationToMerge);
    }

    @Override
    public RequestSpecification sessionId(String sessionIdValue) {
        return wrapped.sessionId(sessionIdValue);
    }

    @Override
    public RequestSpecification sessionId(String sessionIdName, String sessionIdValue) {
        return wrapped.sessionId(sessionIdName, sessionIdValue);
    }

    @Override
    public RequestSpecification urlEncodingEnabled(boolean isEnabled) {
        return wrapped.urlEncodingEnabled(isEnabled);
    }

    @Override
    public RequestSpecification filter(Filter filter) {
        return wrapped.filter(filter);
    }

    @Override
    public RequestSpecification filters(List<Filter> filters) {
        return wrapped.filters(filters);
    }

    @Override
    public RequestSpecification filters(Filter filter, Filter... additionalFilter) {
        return wrapped.filters(filter, additionalFilter);
    }

    @Override
    public RequestSpecification noFilters() {
        return wrapped.noFilters();
    }

    @Override
    public <T extends Filter> RequestSpecification noFiltersOfType(Class<T> filterType) {
        return wrapped.noFiltersOfType(filterType);
    }

    @Override
    public RequestLogSpecification log() {
        return wrapped.log();
    }

    @Override
    public ResponseSpecification response() {
        return wrapped.response();
    }

    @Override
    public RequestSpecification and() {
        return wrapped.and();
    }

    @Override
    public RequestSpecification with() {
        return wrapped.with();
    }

    @Override
    public ResponseSpecification then() {
        return wrapped.then();
    }

    @Override
    public ResponseSpecification expect() {
        return wrapped.expect();
    }

    @Override
    public RequestSpecification when() {
        return wrapped.when();
    }

    @Override
    public RequestSpecification given() {
        return wrapped.given();
    }

    @Override
    public RequestSpecification that() {
        return wrapped.that();
    }

    @Override
    public RequestSpecification request() {
        return wrapped.request();
    }

    @Override
    public RequestSpecification baseUri(String baseUri) {
        return wrapped.baseUri(baseUri);
    }

    @Override
    public RequestSpecification basePath(String basePath) {
        return wrapped.basePath(basePath);
    }

    @Override
    public RequestSpecification proxy(String host, int port) {
        return wrapped.proxy(host, port);
    }

    @Override
    public RequestSpecification proxy(String host) {
        return wrapped.proxy(host);
    }

    @Override
    public RequestSpecification proxy(int port) {
        return wrapped.proxy(port);
    }

    @Override
    public RequestSpecification proxy(String host, int port, String scheme) {
        return wrapped.proxy(host, port, scheme);
    }

    @Override
    public RequestSpecification proxy(URI uri) {
        return wrapped.proxy(uri);
    }

    @Override
    public RequestSpecification proxy(ProxySpecification proxySpecification) {
        return wrapped.proxy(proxySpecification);
    }

    @Override
    public Response get(String path, Object... pathParams) {
        return afterRequest(wrapped.get(path, pathParams));
    }

    @Override
    public Response get(String path, Map<String, ?> pathParams) {
        return afterRequest(wrapped.get(path, pathParams));
    }

    @Override
    public Response post(String path, Object... pathParams) {
        return afterRequest(wrapped.post(path, pathParams));
    }

    @Override
    public Response post(String path, Map<String, ?> pathParams) {
        return afterRequest(wrapped.post(path, pathParams));
    }

    @Override
    public Response put(String path, Object... pathParams) {
        return afterRequest(wrapped.put(path, pathParams));
    }

    @Override
    public Response put(String path, Map<String, ?> pathParams) {
        return afterRequest(wrapped.put(path, pathParams));
    }

    @Override
    public Response delete(String path, Object... pathParams) {
        return afterRequest(wrapped.delete(path, pathParams));
    }

    @Override
    public Response delete(String path, Map<String, ?> pathParams) {
        return afterRequest(wrapped.delete(path, pathParams));
    }

    @Override
    public Response head(String path, Object... pathParams) {
        return afterRequest(wrapped.head(path, pathParams));
    }

    @Override
    public Response head(String path, Map<String, ?> pathParams) {
        return afterRequest(wrapped.head(path, pathParams));
    }

    @Override
    public Response patch(String path, Object... pathParams) {
        return afterRequest(wrapped.patch(path, pathParams));
    }

    @Override
    public Response patch(String path, Map<String, ?> pathParams) {
        return afterRequest(wrapped.patch(path, pathParams));
    }

    @Override
    public Response options(String path, Object... pathParams) {
        return afterRequest(wrapped.options(path, pathParams));
    }

    @Override
    public Response options(String path, Map<String, ?> pathParams) {
        return afterRequest(wrapped.options(path, pathParams));
    }

    @Override
    public Response get(URI uri) {
        return afterRequest(wrapped.get(uri));
    }

    @Override
    public Response post(URI uri) {
        return afterRequest(wrapped.post(uri));
    }

    @Override
    public Response put(URI uri) {
        return afterRequest(wrapped.put(uri));
    }

    @Override
    public Response delete(URI uri) {
        return afterRequest(wrapped.delete(uri));
    }

    @Override
    public Response head(URI uri) {
        return afterRequest(wrapped.head(uri));
    }

    @Override
    public Response patch(URI uri) {
        return afterRequest(wrapped.patch(uri));
    }

    @Override
    public Response options(URI uri) {
        return afterRequest(wrapped.options(uri));
    }

    @Override
    public Response get(URL url) {
        return afterRequest(wrapped.get(url));
    }

    @Override
    public Response post(URL url) {
        return afterRequest(wrapped.post(url));
    }

    @Override
    public Response put(URL url) {
        return afterRequest(wrapped.put(url));
    }

    @Override
    public Response delete(URL url) {
        return afterRequest(wrapped.delete(url));
    }

    @Override
    public Response head(URL url) {
        return afterRequest(wrapped.head(url));
    }

    @Override
    public Response patch(URL url) {
        return afterRequest(wrapped.patch(url));
    }

    @Override
    public Response options(URL url) {
        return afterRequest(wrapped.options(url));
    }

    @Override
    public Response get() {
        return afterRequest(wrapped.get());
    }

    @Override
    public Response post() {
        return afterRequest(wrapped.post());
    }

    @Override
    public Response put() {
        return afterRequest(wrapped.put());
    }

    @Override
    public Response delete() {
        return afterRequest(wrapped.delete());
    }

    @Override
    public Response head() {
        return afterRequest(wrapped.head());
    }

    @Override
    public Response patch() {
        return afterRequest(wrapped.patch());
    }

    @Override
    public Response options() {
        return afterRequest(wrapped.options());
    }

    @Override
    public Response request(Method method) {
        return afterRequest(wrapped.request(method));
    }

    @Override
    public Response request(String method) {
        return afterRequest(wrapped.request(method));
    }

    @Override
    public Response request(Method method, String path, Object... pathParams) {
        return afterRequest(wrapped.request(method, path, pathParams));
    }

    @Override
    public Response request(String method, String path, Object... pathParams) {
        return afterRequest(wrapped.request(method, path, pathParams));
    }

    @Override
    public Response request(Method method, URI uri) {
        return afterRequest(wrapped.request(method, uri));
    }

    @Override
    public Response request(Method method, URL url) {
        return afterRequest(wrapped.request(method, url));
    }

    @Override
    public Response request(String method, URI uri) {
        return afterRequest(wrapped.request(method, uri));
    }

    @Override
    public Response request(String method, URL url) {
        return afterRequest(wrapped.request(method, url));
    }
}
