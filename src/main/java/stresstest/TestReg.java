package stresstest;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import stresstest.json.JSONArray;
import stresstest.json.JSONObject;

import javax.mail.Message;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class TestReg
{
    private final String baseURI;
    private final String basePath;
    private final int port;
    private final String base;
    private static final String methodPrefix = "fdahpuserregws-";

    private final String appId;
    private final String studyId;

    private final String email;
    private final String password;

    private String auth = null;
    private String userId = null;
    private String participantId = UUID.randomUUID().toString();

    private final MailHelper mailHelper;

    public TestReg(Map<String,String> props) throws Exception
    {
        baseURI  = defaultString(props.get("baseURI"), "https://hpreg-stage.lkcompliant.net");
        basePath = "";
        port = Integer.parseInt(defaultString(props.get("port"), "443"));
        base = baseURI+":"+port+basePath;

        email = props.get("username");
        password = props.get("password");

        appId = defaultString(props.get("applicationId"), "FMSA001");
        studyId = defaultString(props.get("studyId"), "Arthritis001");

        mailHelper = new MailHelper(props);
    }

    public void run() throws Exception
    {
        try (CloseableHttpClient httpclient = HttpClients.createDefault())
        {
            {
                HttpGet httpGet = new HttpGet(base + "/" + methodPrefix + "ping.api");
                execute(httpclient, httpGet);
            }

            // CONSIDER: option to save away registration or create new
            JSONObject registerJson;
            {
                HttpPost register = getHttpPost("register.api", Map.of("emailId", email, "password", password));
                registerJson = execute(httpclient, register);
            }

            // TODO: Need to send verification code back to /verify API before invoking any other APIs
            {
                // For now, get verification code by magic
                String verification = getVerificationCode();

                HttpPost verify = getHttpPost("verify.api", Map.of("emailId", email, "code", verification));
                execute(httpclient, verify);
            }

            // Now set auth and userId; these headers are needed for all subsequent calls
            auth = (String)registerJson.get("auth");
            userId = (String)registerJson.get("userId");

            {
                HttpGet userProfile = getHttpGet("confirmRegistration.api");
                JSONObject json = execute(httpclient, userProfile);
                assertThat(json.getBoolean("verified"), is(true));
            }

            {
                HttpGet userProfile = getHttpGet("userProfile.api");
                execute(httpclient, userProfile);
            }

            {
                HttpGet studyState = getHttpGet("studyState.api");
                JSONObject response = execute(httpclient, studyState);
                assertThat(response.getJSONArray("studies").length(), is(0));
            }

            {
                JSONObject json = new JSONObject();
                json.put("studies", new JSONArray(List.of(Map.of(
                        "studyId", studyId,
                        "status", "inProgress",
                        "participantId", participantId,
                        "bookmarked", true,
                        "completion", 1,
                        "adherence", 100
                ))));
                HttpPost updateStudyState = getHttpPost("updateStudyState.api", Map.of());
                updateStudyState.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));
                execute(httpclient, updateStudyState);
            }

            {
                HttpGet studyState = getHttpGet("studyState.api");
                JSONObject response = execute(httpclient, studyState);
                JSONArray studies = response.getJSONArray("studies");
                assertThat(studies.length(), is(1));
                JSONObject study = (JSONObject)studies.get(0);
                assertThat(study.get("status"), is("inProgress"));
                assertThat(study.get("studyId"), is("Arthritis001"));
            }

            {
                String pdf = Base64.getEncoder().encodeToString(Files.readAllBytes(Path.of("/temp/consent.pdf")));
                JSONObject consent = new JSONObject();
                consent.put("version", "1.0");
                consent.put("status", "completed");
                consent.put("pdf", pdf);

                JSONObject params = new JSONObject();
                params.put("studyId", studyId);
                params.put("eligibility", true);
                params.put("sharing", "");
                params.put("consent", consent);

                HttpPost updateConsent = getHttpPost("updateEligibilityConsentStatus.api", Map.of());
                StringEntity entity = new StringEntity(params.toString(), ContentType.APPLICATION_JSON);
                updateConsent.setEntity(entity);
                execute(httpclient, updateConsent);
            }

            {
                HttpGet studyState = getHttpGet("studyState.api");
                JSONObject response = execute(httpclient, studyState);
                assertThat(response.getJSONArray("studies").length(), is(1));
            }
        }
    }

    private String getVerificationCode() throws Exception
    {
        List<Message> messages = mailHelper.find(email, "FDAMyStudiesReg@mystudiesapp.org", 60_000, null);
        // assume there's only one such message for now
        Message message = messages.get(0);
        var content = MailHelper.getMessageContent(message, "text/html");
        assertThat(content.getKey(), equalTo("text/html"));
        Matcher match = Pattern.compile("<span>\\s*<strong>\\s*Verification\\s+Code:\\s*</strong>\\s*(\\w\\w\\w\\w\\w\\w)\\s*</span>").matcher(content.getValue());

        boolean found = match.find();
        assertThat(found, is(true));
        return match.group(1);
    }

    private HttpGet getHttpGet(String endpoint)
    {
        return addHeaders(new HttpGet(base + "/" + methodPrefix + endpoint));
    }

    private HttpPost getHttpPost(String endpoint, Map<String, String> paramsMap) throws UnsupportedEncodingException
    {
        HttpPost httpPost = addHeaders(new HttpPost(base + "/" + methodPrefix + endpoint));

        if (!paramsMap.isEmpty())
        {
            List<NameValuePair> params = paramsMap.entrySet().stream()
                    .map(e->new BasicNameValuePair(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
            httpPost.setEntity(new UrlEncodedFormEntity(params));
        }

        return httpPost;
    }

    private <R extends HttpUriRequest> R addHeaders(R request)
    {
        request.addHeader("applicationId", appId);
        request.addHeader("orgId", "OrgName");

        if (null != auth)
            request.addHeader("auth", auth);
        if (null != userId)
            request.addHeader("userId", userId);

        return request;
    }

    private JSONObject execute(CloseableHttpClient httpClient, HttpUriRequest request) throws Exception
    {
        try (CloseableHttpResponse response = httpClient.execute(request))
        {
            ResponseHandler<String> handler = new BasicResponseHandler();
            StatusLine status = response.getStatusLine();
            System.out.println(request.getURI());

            if (status.getStatusCode() == HttpStatus.SC_OK || status.getStatusCode() == HttpStatus.SC_CREATED)
            {
                String stringResponse = handler.handleResponse(response);
                System.out.println(stringResponse);
                JSONObject json = new JSONObject(stringResponse);

                if (json.has("message"))
                    assertThat(json.getString("message"), equalToIgnoringCase("success"));

                if (json.has("success"))
                    assertThat(json.getBoolean("success"), is(true));

                return json;
            }
            else
            {
                String message = String.format("Received response status %d using uri %s", status.getStatusCode(), request.getURI());
                System.out.println(message);
                throw new Exception(message);
            }
        }
    }
}
