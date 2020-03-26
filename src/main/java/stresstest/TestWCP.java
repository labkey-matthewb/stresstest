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
    final boolean verbose = true;
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


    void verbose(String v)
    {
        if (verbose)
            System.out.println(v);
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
            {
                verbose("skipping getStudyList");
                return null;
            }
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
            var studyVersion = ret.body().jsonPath().getString("studies[0].studyVersion");
            if (isNotBlank(studyId))
            {
                verbose("studyId=" + studyId);
                verbose("studyVersion=" + studyVersion);
                vars.put("studyId",studyId);
                vars.put("studyVersion", studyVersion);
            }
        }
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
            verbose("consentVersion="+consentVersion);
            vars.put("consentVersion", consentVersion);
        }

        return ret;
    }

    private ExtractableResponse<Response> getConsentDocument(boolean silent)
    {
        if (silent)
        {
            if (isBlank(vars.get("studyId")) || isBlank(vars.get("consentVersion")))
            {
                verbose("skipping getConsentDocument");
                return null;
            }
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
        {
            verbose("skipping getResourcesForStudy");
            return null;
        }
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
        if (silent && isBlank(vars.get("studyId")))
        {
            verbose("skipping getResourcesForStudy");
            return null;
        }
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
            verbose("activityId="+activityId);
            verbose("activityVersion="+activityVersion);
            vars.put("activityId", activityId);
            vars.put("activityVersion", activityVersion);
        }

        return ret;
    }


    private ExtractableResponse<Response> getStudyActivityMetadata(boolean silent)
    {
        if (silent && (isBlank(vars.get("studyId")) || isBlank(vars.get("activityId")) || isBlank(vars.get("activityVersion"))))
        {
            verbose("skipping getStudyActivityMetadata");
            return null;
        }
        return given()
                .param("studyId", vars.get("studyId"))
                .param("activityId", vars.get("activityId"))
                .param("activityVersion", vars.get("activityVersion"))
            .when()
                .get("/activity")
            .then()
                .statusCode(200)
            .extract();
    }


    private ExtractableResponse<Response> getStudyDashboardInfo(boolean silent)
    {
        if (silent && isBlank(vars.get("studyId")))
        {
            verbose("skipping getStudyDashboardInfo");
            return null;
        }
        return given()
                .param("studyId", vars.get("studyId"))
            .when()
                .get("/studyDashboard")
            .then()
                .statusCode(200)
            .extract();
    }


    private ExtractableResponse<Response> getTermsPolicy(boolean silent)
    {
        return when()
                .get("/termsPolicy")
            .then()
                .statusCode(200)
            .extract();
    }

    private ExtractableResponse<Response> getNotifications(boolean silent)
    {
        return given()
                .param("skip", "0")
            .when()
                .get("/notifications")
            .then()
                .statusCode(200)
            .extract();
    }

    private ExtractableResponse<Response> getAppUpdates(boolean silent)
    {
        if (silent && (isBlank(vars.get("app")) || isBlank(vars.get("appVersion"))))
        {
            verbose("skipping getAppUpdates");
            return null;
        }
        return given()
                .param("app", vars.get("app"))
                .param("appVersion", vars.get("appVersion"))
            .when()
                .get("/appUpdates")
            .then()
                .statusCode(200)
            .extract();
    }

    private ExtractableResponse<Response> getStudyUpdates(boolean silent)
    {
        if (silent && (isBlank(vars.get("studyId")) || isBlank(vars.get("studyVersion"))))
        {
            verbose("skipping getStudyUpdates");
            return null;
        }
        return given()
                .param("studyId", vars.get("studyId"))
                .param("studyVersion", vars.get("studyVersion"))
            .when()
                .get("/studyUpdates")// NOTE documentation typo
            .then()
                .statusCode(200)
            .extract();
    }

    public void apiTest()
    {
        getGatewayAppResourcesInfo(false);

        getStudyList(false);

        getResourcesForStudy(false);

        getStudyInfo(false);

        getEligibilityConsentMetadata(false);

        getActivityList(false);

        getConsentDocument(false);

        getStudyActivityMetadata(false);

        getStudyDashboardInfo(false);

        getTermsPolicy(false);

        getNotifications(false);

        // feedback
        //TODO

        // contactUs
        //TODO

        // NOTE: app and appVersion need to be provided in stresstest.properties for this method
        getAppUpdates(true);

        // NOTE: studyVersion need to be provided in stresstest.properties for this method
        getStudyUpdates(true);

        System.out.println("apiTest: passed");
    }

    public void randomApiTest(int count)
    {
        List<Function<Boolean,ExtractableResponse<Response>>> fn = Arrays.asList(
                this::getActivityList,
                this::getAppUpdates,
                this::getConsentDocument,
                // this::postContactUs,
                this::getEligibilityConsentMetadata,
                //this::postFeedback,
                this::getGatewayAppResourcesInfo,
                this::getNotifications,
                this::getResourcesForStudy,
                this::getStudyActivityMetadata,
                this::getStudyDashboardInfo,
                this::getStudyInfo,
                this::getStudyList,
                this::getStudyUpdates,
                this::getTermsPolicy
        );
        long start = System.currentTimeMillis();
        var r = new Random();
        for (int i=0 ; i<count ; i++)
        {
            fn.get(r.nextInt(fn.size())).apply(true);
            try {Thread.sleep(10);} catch (InterruptedException x) { /* pass */ }
        }
        System.out.println("randomApiTest: " + count + " api calls");
        System.out.println("randomApiTest: passed in " + (System.currentTimeMillis()-start) + "ms");
    }

    public void run()
    {
        apiTest();
        randomApiTest(100);
        System.out.println("TestWCP: success");
    }
}
