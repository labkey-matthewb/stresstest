package stresstest;

import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TestWCP
{
    final RestSession s;
    final HashMap<String,String> vars = new HashMap<>();

    public TestWCP(Properties props)
    {
        s = RestSession.createBasicAuth(props, null, null);
        // convert to map
        for (Map.Entry<Object, Object> e : props.entrySet())
            vars.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
    }

    public void apiTest()
    {
        Response response;

        // gatewayInfo

        s.given()
            .when()
                .get("/gatewayInfo")
            .then()
                .statusCode(200);

        // studyList

        response = s
            .given()
                .header("applicationId",vars.get("applicationId"))
                .header("orgId",vars.get("orgId"))
            .when()
                .get("/studyList");
        response
            .then()
                .statusCode(200);

        // if we don't have a studyId grab the first one
        if (isBlank(vars.get("studyId")))
        {
            var studyId = response.body().jsonPath().getString("studies[0].studyId");
            if (isNotBlank(studyId))
            {
                System.err.println("studyId=" + studyId);
                vars.put("studyId",studyId);
            }
        }
        System.err.println("studyId="+vars.get("studyId"));

        // studyInfo

        s.given()
                .param("studyId",vars.get("studyId"))
            .when()
                .get("/studyInfo")
            .then()
                .statusCode(200);

        // eligibilityConsent

        response = s
            .given()
                .param("studyId",vars.get("studyId"))
            .when()
                .get("/eligibilityConsent");
        response
            .then()
                .statusCode(200);

        String consentVersion = response.body().jsonPath().getString("consent.version");
        if (isNotBlank(consentVersion))
        {
            System.err.println("consentVersion="+consentVersion);
            vars.put("consentVersion", consentVersion);
        }

        // activityList

        response = s
            .given()
                .param("studyId",vars.get("studyId"))
            .when()
                .get("/activityList");
        response
            .then()
                .statusCode(200);

        if (isBlank(vars.get("activityId")) || isBlank(vars.get("activityVersion")))
        {
            String activityId = response.body().jsonPath().getString("activities[0].activityId");
            String activityVersion = response.body().jsonPath().getString("activities[0].activityVersion");
            System.err.println("activityId="+activityId);
            System.err.println("activityVersion="+activityVersion);
            vars.put("activityId", activityId);
            vars.put("activityVersion", activityVersion);
        }

        // consentDocument

        s.given()
                .param("studyId",vars.get("studyId"))
                .param("consentVersion",vars.get("consentVersion"))
            .when()
                .get("/consentDocument")
            .then()
                .statusCode(200);
    }

    public void run()
    {
        apiTest();
        System.out.println("success");
    }
}
