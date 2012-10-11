package xdi2.connector.allfiled.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.xri3.impl.XRI3Segment;

public class AllfiledApi {

	private static final Logger log = LoggerFactory.getLogger(AllfiledApi.class);

	private String appId;
	private String appSecret;
	private HttpClient httpClient;

	public AllfiledApi() {

		this.appId = null;
		this.appSecret = null;
		this.httpClient = new DefaultHttpClient();
	}

	public AllfiledApi(String appId, String appSecret) {

		this.appId = appId;
		this.appSecret = appSecret;
		this.httpClient = new DefaultHttpClient();
	}

	public void init() {

	}

	public void destroy() {

		this.httpClient.getConnectionManager().shutdown();
	}

	public String startOAuth(HttpServletRequest request, String redirectUri, XRI3Segment userXri) throws IOException {

		String clientId = this.getAppId();
		if (redirectUri == null) redirectUri = uriWithoutQuery(request.getRequestURL().toString());
		String scope = "read";
		String state = userXri.toString();

		// TODO: here write code to start the oauth flow

		log.debug("Starting OAuth...");

		StringBuffer location = new StringBuffer("https://demo.allfiled.com/api/oauth/authorize?");
		location.append("client_id=" + URLEncoder.encode(clientId, "UTF-8"));
		location.append("&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8"));
		location.append("&response_type=code");
		location.append("&scope=" + URLEncoder.encode(scope, "UTF-8"));
		location.append("&state=" + URLEncoder.encode(state, "UTF-8"));

		// done

		log.debug("OAuth URI: " + location.toString());
		return location.toString();
	}

	public void checkState(HttpServletRequest request, XRI3Segment userXri) throws IOException {

		String state = request.getParameter("state");

		if (state == null) {

			log.warn("No OAuth state received.");
			return;
		}

		if (! userXri.toString().equals(state)) throw new IOException("Invalid state: " + state);

		log.debug("State OK");
	}

	public String exchangeCodeForAccessToken(HttpServletRequest request) throws IOException, HttpException, JSONException {

		String clientId = this.getAppId();
		String clientSecret = this.getAppSecret();
		String redirectUri = uriWithoutQuery(request.getRequestURL().toString());
		String code = request.getParameter("code");

		log.debug("Exchanging Code '" + code + "'");

		// send request

		StringBuffer location = new StringBuffer("https://demo.allfiled.com/api/oauth/token?");
		location.append("client_id=" + URLEncoder.encode(clientId, "UTF-8"));
		location.append("&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8"));
		location.append("&grant_type=authorization_code");
		location.append("&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8"));
		location.append("&code=" + URLEncoder.encode(code, "UTF-8"));

		HttpGet httpGet = new HttpGet(URI.create(location.toString()));
		HttpResponse httpResponse = this.httpClient.execute(httpGet);
		HttpEntity httpEntity = httpResponse.getEntity();

		// read response

		String accessToken = null;

		String content = EntityUtils.toString(httpEntity);
		JSONObject tokenObject = new JSONObject(content);

		accessToken = tokenObject == null ? null : tokenObject.getString("access_token");

		EntityUtils.consume(httpEntity);

		// done

		log.debug("Access Token: " + accessToken);
		return accessToken;
	}

	public void revokeAccessToken(String accessToken) throws IOException, JSONException {

		if (accessToken == null) throw new NullPointerException();

		log.debug("Revoking Access Token '" + accessToken + "'");

		// TODO: here write code to revoke an access token

		// done

		log.debug("Access token revoked.");
	}

	public JSONObject getFile(String accessToken, String categoryIdentifier, String fileIdentifier) throws IOException, JSONException {

		if (accessToken == null) throw new NullPointerException();

		log.debug("Retrieving File for Access Token '" + accessToken + "' (category: " + categoryIdentifier + ", file: " + fileIdentifier + ")");

		// retrieve category

		StringBuffer location = new StringBuffer("https://demo.allfiled.com/api/file/list/" + categoryIdentifier);

		HttpGet httpGet = new HttpGet(URI.create(location.toString()));
		httpGet.addHeader("Authorization", "Bearer " + accessToken);
		HttpResponse httpResponse = this.httpClient.execute(httpGet);
		HttpEntity httpEntity = httpResponse.getEntity();

		String content = EntityUtils.toString(httpEntity);
		JSONArray files = new JSONArray(content);
		log.debug("Files: " + files);

		EntityUtils.consume(httpEntity);

		JSONObject file = null;

		for (int i=0; i<files.length(); i++) {

			if (fileIdentifier.equals(files.getJSONObject(i).getString("fileType"))) {

				file = files.getJSONObject(i);
				break;
			}
		}

		if (file == null) return null;

		// retrieve file

		StringBuffer location2 = new StringBuffer("https://demo.allfiled.com/api/file/" + file.getInt("id"));

		HttpGet httpGet2 = new HttpGet(URI.create(location2.toString()));
		httpGet2.addHeader("Authorization", "Bearer " + accessToken);
		HttpResponse httpResponse2 = this.httpClient.execute(httpGet2);
		HttpEntity httpEntity2 = httpResponse2.getEntity();

		String content2 = EntityUtils.toString(httpEntity2);
		file = new JSONObject(content2);

		EntityUtils.consume(httpEntity2);

		// build fields map

		JSONArray fileFields = file.getJSONArray("fields");
		JSONObject fileFieldsMap = new JSONObject();

		for (int i=0; i<fileFields.length(); i++) {

			JSONObject fileField = fileFields.getJSONObject(i);
			fileFieldsMap.put(fileField.getString("name"), fileField.getString("value"));
		}

		file.put("fieldsMap", fileFieldsMap);
		
		// done

		log.debug("File: " + file);
		return file;
	}

	private static String uriWithoutQuery(String url) {

		return url.contains("?") ? url.substring(url.indexOf("?")) : url;
	}

	public String getAppId() {

		return this.appId;
	}

	public void setAppId(String appId) {

		this.appId = appId;
	}

	public String getAppSecret() {

		return this.appSecret;
	}

	public void setAppSecret(String appSecret) {

		this.appSecret = appSecret;
	}
}
