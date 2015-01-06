package so.sauru;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author sio4
 *
 */
public final class SwissKnife {
	/**
	 * replace javax.net.ssl.HttpsURLConnection's Host Verifier and
	 * SocketFactory with Fake Methods. <br>
	 * <br>
	 * CAUTION!!! this is not safe way to get connection with SSL server. use it
	 * for test/development purpose or REALLY REALLY trusted environment only.
	 * 
	 * @return true on success, false on exception.
	 */
	public static boolean setFakeSSLVerifier() {
		/* hostname verifier */
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
				new javax.net.ssl.HostnameVerifier() {
					public boolean verify(String hostname,
							javax.net.ssl.SSLSession sslSession) {
						if (hostname.contains(".example.com")) {
							System.out.println("OK, " + hostname);
							return true;
						}
						System.out.println("HostnameVerifier: " + hostname);
						return true;
					}
				});

		/* trusted certification checkers */
		TrustManager[] trustAll = new X509TrustManager[] {
				new X509TrustManager() {
					@Override
					public void checkClientTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						System.out.println("checkClientTrusted...");
					}

					@Override
					public void checkServerTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						System.out.println("checkServerTrusted...");
					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						System.out.println("getAcceptedIssuers...");
						return null;
					}
				}
		};
		try {
			SSLContext sslContext;
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAll, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
					.getSocketFactory());
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
		}
		return false;

	}
}
