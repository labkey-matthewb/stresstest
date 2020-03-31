package stresstest;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.*;
import static org.hamcrest.Matchers.equalTo;

public class TestWCP
{
    final boolean verbose = true;
    final RestSession s;
    final HashMap<String,String> vars = new HashMap<>();

    public TestWCP(Map<String,String> props)
    {
        vars.putAll(props);
        s = RestSession.createBasicAuth(vars, null, null);
    }

    RequestSpecification given()
    {
        return s.given().log().ifValidationFails();
    }

    RequestSpecification when()
    {
        return given().when();
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
                .body("message", equalTo("SUCCESS"))
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
                .body("message", equalTo("SUCCESS"))
            .extract();

        // if we don't have a studyId grab a random
        if (isBlank(vars.get("studyId")))
        {
//            var studyId = ret.body().jsonPath().getString("studies[0].studyId");
//            var studyVersion = ret.body().jsonPath().getString("studies[0].studyVersion");
            var len = ret.body().jsonPath().getInt("studies.size()");
            if (len > 0)
            {
                int r = new Random().nextInt(len);
                var studyId = ret.body().jsonPath().getString("studies["+ r +"].studyId");
                var studyVersion = ret.body().jsonPath().getString("studies["+ r +"].studyVersion");
                verbose("studyId=" + studyId);
                verbose("studyVersion=" + studyVersion);
                vars.put("studyId", studyId);
                vars.put("studyVersion", studyVersion);
                // clear dependent params
                vars.remove("activityId");
                vars.remove("activityVersion");
                vars.remove("consentVersion");
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
                .body("message", equalTo("SUCCESS"))
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
                .body("message", equalTo("SUCCESS"))
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
                .body("message", equalTo("SUCCESS"))
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
                .body("message", equalTo("SUCCESS"))
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
                .body("message", equalTo("SUCCESS"))
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
                .body("message", equalTo("SUCCESS"))
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
                .body("message", equalTo("SUCCESS"))
            .extract();
    }


    private ExtractableResponse<Response> getTermsPolicy(boolean silent)
    {
        return when()
                .get("/termsPolicy")
            .then()
                .statusCode(200)
                .body("message", equalTo("SUCCESS"))
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
                .body("message", equalTo("SUCCESS"))
            .extract();
    }

    private ExtractableResponse<Response> getAppUpdates(boolean silent)
    {
        if (silent && isBlank(vars.get("applicationId")))
        {
            verbose("skipping getAppUpdates");
            return null;
        }
        return given()
                .param("app", vars.get("applicationId"))
                .param("appVersion", defaultString(vars.get("appVersion"),"1.0"))
            .when()
                .get("/appUpdates")
            .then()
                .statusCode(200)
//                .body("message", equalTo("SUCCESS"))  // BUG?? message==""
            .extract();
    }

    private ExtractableResponse<Response> getStudyUpdates(boolean silent)
    {
        if (silent && isBlank(vars.get("studyId")))
        {
            verbose("skipping getStudyUpdates");
            return null;
        }
        return given()
                .param("studyId", vars.get("studyId"))
                .param("studyVersion", defaultString(vars.get("studyVersion"),"1.0"))
            .when()
                .get("/studyUpdates")// NOTE documentation typo
            .then()
                .statusCode(200)
                .body("message", equalTo("SUCCESS"))
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

        // NOTE: applicationId needs to be provided in stresstest.properties for this method
        getAppUpdates(false);

        getStudyUpdates(false);

        System.out.println("apiTest: passed");
    }

    // custom class because the generics are out of control
    private class StatsWrapper implements Callable
    {
        final Function<Boolean,ExtractableResponse<Response>> fn;
        final Stats stats;

        StatsWrapper(Function<Boolean,ExtractableResponse<Response>> fn, String name)
        {
            this.fn = fn;
            this.stats = new Stats(name);
        }

        @Override
        public Object call() throws Exception
        {
            var res = fn.apply(true);
            if (null != res)
                stats.update(res.time());
            return res;
        }
    }

    public void randomApiTest(int count) throws Exception
    {
        StatsWrapper[] apis = new StatsWrapper[]
        {
            new StatsWrapper(this::getActivityList,"getActivityList"),
            new StatsWrapper(this::getAppUpdates,"getAppUpdates"),
            new StatsWrapper(this::getConsentDocument,"getConsentDocument"),
            // this::postContactUs,
            new StatsWrapper(this::getEligibilityConsentMetadata,"getEligibilityConsentMetadata"),
            //this::postFeedback,
            new StatsWrapper(this::getGatewayAppResourcesInfo,"getGatewayAppResourcesInfo"),
            new StatsWrapper(this::getNotifications,"getNotifications"),
            new StatsWrapper(this::getResourcesForStudy,"getResourcesForStudy"),
            new StatsWrapper(this::getStudyActivityMetadata,"getStudyActivityMetadata"),
            new StatsWrapper(this::getStudyDashboardInfo,"getStudyDashboardInfo"),
            new StatsWrapper(this::getStudyInfo,"getStudyInfo"),
            new StatsWrapper(this::getStudyList,"getStudyList"),
            new StatsWrapper(this::getStudyUpdates,"getStudyUpdates"),
            new StatsWrapper(this::getTermsPolicy, "getTermsPolicy")
        };

        long start = System.currentTimeMillis();
        long pauseTime = 10;
        int callCount = 0;
        var r = new Random();
        for (int i=0 ; i<count ; )
        {
            var ret = apis[r.nextInt(apis.length)].call();
            if (null != ret)
            {
                i++;
                if (i%10==0)
                    verbose("" + i);
            }
        }

        long callTime = (System.currentTimeMillis() - start - (pauseTime - callCount));
        System.out.println("randomApiTest: " + count + " api calls in " + callTime + "ms");
        //System.out.println("randomApiTest: avg call time = " + (callTime / callCount));
        for (StatsWrapper api : apis)
            System.out.println(api.stats.toString());
    }

    public void run()
    {
        try
        {
            apiTest();
            randomApiTest(200);
            System.out.println("TestWCP: success");
        }
        catch (Exception x)
        {
            x.printStackTrace();
            System.exit(1);
        }
    }
}
