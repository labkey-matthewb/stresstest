package stresstest;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

public class Main
{
	final File stressTestProperties = new File("stresstest.properties");

	Properties getProperties(boolean interactive)
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
			return props;
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
		s.with().params("email",email,"password",password).post("/login-loginApi.api").body().print();
		s.get("/login-whoami.api").then().body("email", equalTo(email));
	}

	public void run()
	{
		Properties properties = getProperties(true);
		RestSession s = RestSession.createLabKeySession(properties);
		var r = s.get("/login-whoami.api");
		r.then().assertThat().body(matchesJsonSchemaInClasspath("whoami-schema.json"));
		r.then()
				.body("id", equalTo(0))
				.body("email", equalTo("guest"));
		labkeyLogin(s, properties.getProperty("email"), properties.getProperty("password"));
		labkeyLogout(s);
		System.out.println("SUCCESSS!");
	}

	public static void main(String[] args)
	{
		new Main().run();
	}
}
