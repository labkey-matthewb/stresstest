package stresstest;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

public class Main
{
	static final File stressTestProperties = new File("stresstest.properties");

	static Map<String,String> getProperties(boolean interactive)
	{
 		try
		{
			if (!(stressTestProperties.exists()))
				stressTestProperties.createNewFile();
			var props = new Properties();
			props.load(new FileReader(stressTestProperties));

			if (interactive && (!props.containsKey("baseURI") ||
				!props.containsKey("port") ||
				!props.containsKey("basePath")))
			{
				var s = new Scanner(System.in);
				for (var prop : Arrays.asList("baseURI", "port", "basePath"))
				{
					System.out.print(prop + ": ");
					props.setProperty(prop, s.nextLine().trim());
				}
				props.store(new FileWriter(stressTestProperties),"");
			}
			var ret = new TreeMap<String,String>();
			for (Map.Entry<Object, Object> e : props.entrySet())
				ret.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
			return ret;
		}
 		catch(IOException io)
		{
			io.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	void labkeyLogout(RestSession s)
	{
		var body = s.get("/login-whoami.api").body();
		var id = body.jsonPath().getInt("id");
		if (0==id)
			return;
		s.with().post("/login-logoutApi.api").body().print();
		s.get("/login-whoami.api").then().body("id", equalTo(0));
	}

	void labkeyLogin(RestSession s, String email, String password)
	{
		var body = s.get("/login-whoami.api").body();
		if (email.equals(body.jsonPath().getString("email")))
			return;
		if (0 != body.jsonPath().getInt("id"))
			s.post("/login-logoutApi.api");
		s.with().params("email",email,"password",password).post("/login-loginApi.api");
		s.get("/login-whoami.api").then().body("email", equalTo(email));
	}

	void checkLabKeyEmail(RestSession s, String expected)
	{
		s.get("/login-whoami.api").then()
			.body(matchesJsonSchemaInClasspath("whoami-schema.json"))
			.body("email", equalTo(expected));
	}

	public void run(Map<String,String> properties)
	{
		RestSession s1 = RestSession.createLabKeySession(properties);
		RestSession s2 = RestSession.createLabKeySession(properties);
		checkLabKeyEmail(s1, "guest");
		checkLabKeyEmail(s2, "guest");
		labkeyLogin(s1, properties.get("email"), properties.get("password"));
		checkLabKeyEmail(s1, properties.get("email"));
		checkLabKeyEmail(s2, "guest");
		labkeyLogout(s1);
		checkLabKeyEmail(s1, "guest");
		checkLabKeyEmail(s2, "guest");
		System.out.println("SUCCESSS!");
	}

	public static void main(String[] args) throws Exception
	{
		Map<String,String> properties = getProperties(false);

		Map<String,String> wcpProperties = new TreeMap<>(properties);
		properties.forEach((k,v) -> {if (StringUtils.startsWith(String.valueOf(k),"wcp.")) wcpProperties.put(String.valueOf(k).substring(4),String.valueOf(v));});
		new TestWCP(wcpProperties).run();

		Map<String,String> regProperties = new TreeMap<>(properties);
		properties.forEach((k,v) -> {if (StringUtils.startsWith(String.valueOf(k),"reg.")) regProperties.put(String.valueOf(k).substring(4),String.valueOf(v));});
		new TestReg(regProperties).run();
	}
}
