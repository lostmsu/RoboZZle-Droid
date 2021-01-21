package com.team242.robozzle.telemetry;

import RoboZZle.Telemetry.SessionLog;
import RoboZZle.Telemetry.TelemetryBag;
import RoboZZle.Telemetry.TelemetrySource;
import android.util.Base64;
import com.team242.robozzle.BuildConfig;
import com.team242.robozzle.RobozzleWebClient;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class TelemetryClient {
	public static final TelemetrySource telemetrySource;
	static {
		telemetrySource = new TelemetrySource();
		telemetrySource.setProduct("RDroid");
		telemetrySource.setVersion(BuildConfig.VERSION_NAME);
		telemetrySource.setIsTest(BuildConfig.DEBUG);
	}
    static final URL TELEMETRY_SINK_URL = makeUrl("https://robtelemetry.azurewebsites.net/api/Telemetry");

    static final JSONSerializer serializer = new JSONSerializer()
            .transform(new Iso8601DateTransformer(), Date.class)
            .exclude("*.class");
	static final JSONDeserializer<SessionLog> deserializer = new JSONDeserializer<SessionLog>()
			.use(Date.class, new Iso8601DateTransformer());

	public static SessionLog deserialize(String source){
		return deserializer.deserialize(source, SessionLog.class);
	}

    public <T> String serialize(T telemetry){
        return serializer.deepSerialize(telemetry);
    }

	public void submit(TelemetryBag telemetry, String login, String password) throws IOException {
		HttpsURLConnection connection = (HttpsURLConnection)TELEMETRY_SINK_URL.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setChunkedStreamingMode(0);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Authorization", makeAuthorizationToken(login, password));

		OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(connection.getOutputStream()));
		serializer.deepSerialize(telemetry, writer);
		writer.close();

		int statusCode = connection.getResponseCode();
		if (statusCode != 200)
			throw new IOException("Telemetry endpoint returned HTTP " + statusCode);
	}

	static String makeAuthorizationToken(String login, String password) {
		try {
			String loginPassword = login + ":" + RobozzleWebClient.computeHash(password);
			String encodedLoginPassword = Base64.encodeToString(loginPassword.getBytes("UTF8"), Base64.DEFAULT);
			return "Basic " + encodedLoginPassword;
		}catch (UnsupportedEncodingException e){
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e){
			throw new RuntimeException(e);
		}
	}

	static java.net.URL makeUrl(String urlString) {
		try {
			return new java.net.URL(urlString);
		} catch (java.net.MalformedURLException e) {
			return null;
		}
	}
}