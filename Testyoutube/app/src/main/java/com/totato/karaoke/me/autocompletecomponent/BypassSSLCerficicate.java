package com.totato.karaoke.me.autocompletecomponent;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class BypassSSLCerficicate {
static BypassSSLCerficicate singleton;

	private BypassSSLCerficicate(){
	}
	public static BypassSSLCerficicate getInstance(){
		if(singleton==null) {
			singleton= new BypassSSLCerficicate();
			enableAllCertificates();
		}
		return singleton;
	}

	private static void enableAllCertificates(){
		try{
			X509TrustManager trustManager = new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}
				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			TrustManager[] trustAllCerts = new TrustManager[] {trustManager};

			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			HostnameVerifier allHostsValid = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private HttpURLConnection getURLConnection(String urlStr, boolean isGET) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setInstanceFollowRedirects(true);
		conn.setConnectTimeout(1000 * 60);
		conn.setReadTimeout(1000 * 60);
		conn.setUseCaches(false);
		conn.setDoInput(true);
		if(isGET) conn.setRequestMethod("GET");
		else {
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
		}
		return conn;
	}

	private String getTextContent(HttpURLConnection conn) throws IOException {
		StringBuilder content = new StringBuilder();
		
		int code = conn.getResponseCode();
		if(code == 200){
			InputStream is = conn.getInputStream();
			byte[] bytes = new byte[1024];
			int numRead = 0;
			while ((numRead = is.read(bytes)) >= 0) {
				content.append(new String(bytes, 0, numRead));
			}
		}
		return content.toString();
	}
	public String sendGET(String url){
		String content = "";
		try{
			HttpURLConnection conn = getURLConnection(url, true);
			content = getTextContent(conn);
		}catch(Exception e){
			content = "";
		}
		return content;
	}
	
	//***************************************************************************
	
	public static String[] decodeArray(String source){
        try{
        	JSONArray o = new JSONArray(source);
            int length = o.length();
            if(length>0){
                String[] array = new String[length];
                for(int i=0; i<length; i++){
                    array[i] =o.getString(i);
                }
                return array;
            }
        }catch(Exception e){}
        return null;
    }
}
