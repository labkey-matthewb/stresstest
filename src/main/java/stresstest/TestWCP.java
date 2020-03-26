package stresstest;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.*;
import java.util.function.Function;

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

    RequestSpecification given()
    {
        return s.given();
    }

    RequestSpecification when()
    {
        return s.given().when();
    }

    // silent parameter is used to silently skip test without required vars
    // useful for calling APIs in random order

    private ExtractableResponse<Response> getGatewayAppResourcesInfo(boolean silent)
    {
        return when()
                .get("/gatewayInfo")
            .then()
                .statusCode(200)
            .extract();
    }

    private ExtractableResponse<Response> getStudyList(boolean silent)
    {
        if (silent)
        {
            if (isBlank(vars.get("applicationId")) ||
                isBlank(vars.get("orgId")))
                return null;
        }
        final var ret = given()
                .header("applicationId",vars.get("applicationId"))
                .header("orgId",vars.get("orgId"))
            .when()
                .get("/studyList")
            .then()
                .statusCode(200)
            .extract();

        // if we don't have a studyId grab the first one
        if (isBlank(vars.get("studyId")))
        {
            var studyId = ret.body().jsonPath().getString("studies[0].studyId");
            if (isNotBlank(studyId))
            {
                System.err.println("studyId=" + studyId);
                vars.put("studyId",studyId);
            }
        }
        System.err.println("studyId="+vars.get("studyId"));

        return ret;
    }

    private ExtractableResponse<Response> getEligibilityConsentMetadata(boolean silent)
    {
        final var ret = given()
                .param("studyId",vars.get("studyId"))
            .when()
                .get("/eligibilityConsent")
            .then()
                .statusCode(200)
            .extract();

        String consentVersion = ret.body().jsonPath().getString("consent.version");
        if (isNotBlank(consentVersion))
        {
            System.err.println("consentVersion="+consentVersion);
            vars.put("consentVersion", consentVersion);
        }

        return ret;
    }

    private ExtractableResponse<Response> getConsentDocument(boolean silent)
    {
        if (silent)
        {
            if (isBlank(vars.get("studyId")) || isBlank(vars.get("consentVersion")))
                return null;
        }
        return given()
                .param("studyId",vars.get("studyId"))
                .param("consentVersion",vars.get("consentVersion"))
            .when()
                .get("/consentDocument")
            .then()
                .statusCode(200)
            .extract();
    }

    private ExtractableResponse<Response> getResourcesForStudy(boolean silent)
    {
        if (silent && isBlank(vars.get("studyId")))
            return null;
        return given()
                .param("studyId",vars.get("studyId"))
            .when()
                .get("/resources")
            .then()
                .statusCode(200)
            .extract();
    }


    private ExtractableResponse<Response> getStudyInfo(boolean silent)
    {
        return given()
                .param("studyId", vars.get("studyId"))
            .when()
                .get("/studyInfo")
            .then()
                .statusCode(200)
            .extract();
    }

    private ExtractableResponse<Response> getActivityList(boolean silent)
    {
        var ret = given()
                .param("studyId",vars.get("studyId"))
            .when()
                .get("/activityList")
            .then()
                .statusCode(200)
            .extract();

        if (isBlank(vars.get("activityId")) || isBlank(vars.get("activityVersion")))
        {
            String activityId = ret.body().jsonPath().getString("activities[0].activityId");
            String activityVersion = ret.body().jsonPath().getString("activities[0].activityVersion");
            System.err.println("activityId="+activityId);
            System.err.println("activityVersion="+activityVersion);
            vars.put("activityId", activityId);
            vars.put("activityVersion", activityVersion);
        }

        return ret;
    }


    public void apiTest()
    {
        // gatewayInfo
        getGatewayAppResourcesInfo(false);

        // studyList
        getStudyList(false);

        getResourcesForStudy(false);

        getStudyInfo(false);

        getEligibilityConsentMetadata(false);

        getActivityList(false);

        getConsentDocument(false);

        // activity
        //TODO

        // studyDashboard
        //TODO

        // termsPolicy
        //TODO

        // notifications
        //TODO

        // feedback
        //TODO

        // contactUs
        //TODO

        // appUpdates
        //TODO

        // studyUpdates
        //TODO

        System.out.println("apiTest: passed");
    }

    public void randomApiTest()
    {
        List<Function<Boolean,ExtractableResponse<Response>>> fn = Arrays.asList(
                this::getResourcesForStudy,
                this::getConsentDocument,
                this::getActivityList,
                this::getEligibilityConsentMetadata,
                this::getGatewayAppResourcesInfo,
                this::getStudyInfo,
                this::getStudyList
        );
        long start = System.currentTimeMillis();
        var r = new Random();
        for (int i=0 ; i<100 ; i++)
        {
            fn.get(r.nextInt(fn.size())).apply(true);
            try {Thread.sleep(10);} catch (InterruptedException x) { /* pass */ }
        }
        System.out.println("randomApiTest: 100 api calls");
        System.out.println("randomApiTest: passed in " + (System.currentTimeMillis()-start) + "ms");
    }

    public void run()
    {
        apiTest();
        randomApiTest();
        System.out.println("success");
    }
}
