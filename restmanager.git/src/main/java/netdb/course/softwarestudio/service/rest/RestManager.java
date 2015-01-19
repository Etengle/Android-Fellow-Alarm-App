package netdb.course.softwarestudio.service.rest;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import netdb.course.softwarestudio.service.rest.model.Image;
import netdb.course.softwarestudio.service.rest.model.PostResource;
import netdb.course.softwarestudio.service.rest.model.Resource;
import netdb.course.softwarestudio.service.rest.ui.RestImageView;

public class RestManager {
	public static final int HTTP_DISK_CACHE_SIZE = 5 * 1024 * 1024; // in bytes, 5MB
	public static final int IMAGE_MEMORY_CACHE_SIZE = 50; // in #images
	private static final String TAG = RestManager.class.getSimpleName();
	public static String serverUrl;
	public static RequestQueue reqQueue;
	public static ImageLoader imgLoader;
	private static RestManager m;

	private Context app;


	protected RestManager(Context app) {
		this.app = app;
		serverUrl = app.getString(R.string.app_rest_server_url);
		try {
			reqQueue = getRequestQueue();
			imgLoader = getImageLoader(reqQueue);
		} catch (Exception e) {
			// should never happen
			throw new RuntimeException("Cannot setup Volley correctly", e);
		}
	}

	public static RestManager getInstance(Context app) {
		if (m == null) {
			m = new RestManager(app);
		}
		return m;
	}

	public static String parseIdFromUrl(String loc) {
		return loc.substring(loc.lastIndexOf("/") + 1);
	}

	public void cancelAll(String tag) {
		reqQueue.cancelAll(tag);
	}

	public void clearCache() {
		reqQueue.getCache().clear();
	}

	public <T extends Resource> void getResource(final Class<T> cls, String id,
	                                             final GetResourceListener<T> listener, String tag,
	                                             Resource... parents) {
		try {
			Log.d(TAG, "Getting resource from " + getResourceUrl(cls, id, parents) + "...");

			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Accept", "application/json");

			GsonRequest<T> req = new GsonRequest<T>(getResourceUrl(cls, id, parents), null, null, cls,
					new Response.Listener<GsonResponse<T>>() {
						@Override
						public void onResponse(GsonResponse<T> gRes) {
							Log.d(TAG, "Response: " + gRes);
							if (listener != null) listener.onResponse(gRes.getCode(),
									gRes.getHeaders(), gRes.getBody());
						}
					}, new RestErrorListener(listener));
			req.setTag(tag);
			reqQueue.add(req);
		} catch (Exception e) {
			listener.onError(e.getMessage(), e, 0, null);
		}
	}

	/**
	 * Get an {@link javax.net.ssl.SSLContext} that trusts the self-signed certificate from StrikeUp servers.
	 * <p/>
	 * <p>Many web sites describe a poor alternative solution which is to install a TrustManager
	 * that does nothing. If you do this you might as well not be encrypting your communication,
	 * because anyone can attack your users at a public Wi-Fi hotspot by using DNS tricks to
	 * send your users' traffic through a proxy of their own that pretends to be your server.
	 * The attacker can then record passwords and other personal bytes. This works because the
	 * attacker can generate a certificate and—without a TrustManager that actually validates
	 * that the certificate comes from a trusted source—your app could be talking to anyone.
	 * So don't do this, not even temporarily.</p>
	 *
	 * @param crtPath the path to .crt file provided by the trusted server
	 */
	public static SSLContext getSslContext(String crtPath) throws CertificateException,
			IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		// Load CAs from an InputStream
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		InputStream caInput = new BufferedInputStream(new FileInputStream(crtPath));
		Certificate ca;
		try {
			ca = cf.generateCertificate(caInput);
		} finally {
			caInput.close();
		}
		// Create a KeyStore containing our trusted CAs
		String keyStoreType = KeyStore.getDefaultType();
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		keyStore.load(null, null);
		keyStore.setCertificateEntry("ca", ca);
		// Create a TrustManager that trusts the CAs in our KeyStore
		String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
		tmf.init(keyStore);
		// Create an SSLContext that uses our TrustManager
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, tmf.getTrustManagers(), null);
		return context;
	}

	public <T extends Resource> void listResource(Class<T> elemCls, Map<String, String> params,
	                                              final ListResourceListener<T> listener, String tag,
	                                              Resource... parents) {
		try {
			if(params == null) {
				Log.d(TAG, "Listing resource from " + getCollectionUrl(elemCls, parents) + "...");
			} else {
				Log.d(TAG, "Listing resource from " + getCollectionUrl(elemCls, parents) + " with parameters " +
					Arrays.toString(params.entrySet().toArray()) + "...");
			}
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Accept", "application/json");

			Class<T[]> arrayCls = (Class<T[]>) Class.forName("[L" + elemCls.getName() + ";");
			GsonRequest<T[]> req = new GsonRequest<T[]>(getCollectionUrl(elemCls, parents),
					params, headers,
					arrayCls, new Response.Listener<GsonResponse<T[]>>() {
				@Override
				public void onResponse(GsonResponse<T[]> gRes) {
					Log.d(TAG, "Response: " + gRes);
					if (listener != null) listener.onResponse(gRes.getCode(), gRes.getHeaders(),
							gRes.getBody() == null ? null : Arrays.asList(gRes.getBody()));
				}
			}, new RestErrorListener(listener));
			req.setTag(tag);
			reqQueue.add(req);
		} catch (Exception e) {
			listener.onError(e.getMessage(), e, 0, null);
		}
	}

	/**
	 * @param cls
	 * @param parents
	 * @param resource if of type {@link PostResource}, the {@link PostResource#setId(String)} will
	 *                 be called
	 * @param listener
	 * @param tag
	 * @param <T>
	 */
	public <T extends Resource> void postResource(Class<T> cls, final T resource,
	                                              final PostResourceListener listener,
	                                              String tag, Resource... parents) {
		try {
			Log.d(TAG, "Posting resource to " + getCollectionUrl(cls, parents) + "...");
			GsonRequest<T> req = new GsonRequest<T>(Request.Method.POST,
					getCollectionUrl(cls, parents), null, resource, cls,
					new Response.Listener<GsonResponse<T>>() {
						@Override
						public void onResponse(GsonResponse<T> gRes) {
							Log.d(TAG, "Response: " + gRes);
							if (resource instanceof PostResource &&
									200 <= gRes.getCode() && gRes.getCode() < 300 &&
									gRes.getHeaders().get("Location") != null) {
								((PostResource) resource).setId(
										parseIdFromUrl(gRes.getHeaders().get("Location")));
							}
							if (listener != null)
								listener.onResponse(gRes.getCode(), gRes.getHeaders());
						}
					}, new RestErrorListener(listener));
			req.setTag(tag);
			reqQueue.add(req);
		} catch (Exception e) {
			listener.onError(e.getMessage(), e, 0, null);
		}
	}

	public <T extends Resource> void putResource(Class<T> cls, T resource,
	                                             final PutResourceListener listener, String tag,
	                                             Resource... parents) {
		try {
			Log.d(TAG, "Putting resource to " + getResourceUrl(cls, resource.getId(), parents) + "...");
			GsonRequest<T> req = new GsonRequest<T>(Request.Method.PUT,
					getResourceUrl(cls, resource.getId(), parents), null, resource, cls, new Response.Listener<GsonResponse<T>>() {
				@Override
				public void onResponse(GsonResponse<T> gRes) {
					Log.d(TAG, "Response: " + gRes);
					if (listener != null) listener.onResponse(gRes.getCode(), gRes.getHeaders());
				}
			}, new RestErrorListener(listener));
			req.setTag(tag);
			reqQueue.add(req);
		} catch (Exception e) {
			listener.onError(e.getMessage(), e, 0, null);
		}
	}

	public <T extends Resource> void deleteResource(Class<T> cls, String id,
	                                                final DeleteResourceListener listener, String tag,
	                                                Resource... parents) {
		try {
			Log.d(TAG, "Deleting resource from " + getResourceUrl(cls, id, parents) + "...");
			GsonRequest<T> req = new GsonRequest<T>(Request.Method.DELETE,
					getResourceUrl(cls, id, parents), null, null, cls,
					new Response.Listener<GsonResponse<T>>() {
						@Override
						public void onResponse(GsonResponse<T> gRes) {
							Log.d(TAG, "Response: " + gRes);
							if (listener != null)
								listener.onResponse(gRes.getCode(), gRes.getHeaders());
						}
					}, new RestErrorListener(listener));
			req.setTag(tag);
			reqQueue.add(req);
		} catch (Exception e) {
			listener.onError(e.getMessage(), e, 0, null);
		}
	}

	public void loadImage(String url, RestImageView view, RestImageView.ImageLoadListener
			listener) {
		view.setImageUrl(url, imgLoader, listener);
	}

	/**
	 * TODO handle TAG
	 *
	 * @param cls
	 * @param image
	 * @param listener
	 * @param tag
	 * @param parents
	 * @param <T>
	 */
	public <T extends Image> void postImage(final Class<T> cls, final T image,
	                                        final PostImageListener listener, String tag,
	                                        final Resource... parents) {
		new AsyncTask<Void, Void, Void>() {
			int code;
			String location;
			Exception e;

			@Override
			protected Void doInBackground(Void... params) {
				HttpURLConnection conn = null;
				DataOutputStream dos = null;
				String lineEnd = "\r\n";
				String twoHyphens = "--";
				String boundary = "*****";
				try {
					Log.d(TAG, "Posting image to " + getCollectionUrl(cls, parents) + "...");
					conn = (HttpURLConnection) new URL(getCollectionUrl(cls, parents)).openConnection();
					conn.setDoInput(true);
					conn.setDoOutput(true);
					conn.setUseCaches(false); // don't use a cached Copy
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Connection", "Keep-Alive");
					conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

					dos = new DataOutputStream(conn.getOutputStream());
					// write "imageFile" part
					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data;name=\"imageFile\";filename=\""
							+ image.getName() + "\"" + lineEnd);
					dos.writeBytes("Content-Type: " + image.getMimeType() + lineEnd);
					dos.writeBytes(lineEnd);
					dos.write(image.getBytes());
					// write "use" part
					dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data;name=\"duration\"" + lineEnd);
					dos.writeBytes("Content-Type: text/plain" + lineEnd);
					dos.writeBytes(lineEnd);
					dos.writeBytes("perm");
					// end parts
					dos.writeBytes(lineEnd + twoHyphens + boundary + twoHyphens + lineEnd);
					dos.flush();

					// get response from the server
					code = conn.getResponseCode();
					location = conn.getHeaderField("Location");
					Log.d(TAG, "Response: " + code);
				} catch (Exception e) {
					this.e = e;
				} finally {
					if (dos != null) {
						try {
							dos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (conn != null) {
						conn.disconnect();
					}
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void v) {
				if (e == null && 200 <= code && code < 400) {
					image.setId(location);
					if (listener != null) listener.onResponse(code);
				} else {
					if (listener != null)
						listener.onError("Failed to post image " + image.getName(), e, code, null);
				}
			}
		}.execute();
	}

	public <T extends Resource> String getCollectionUrl(Class<T> cls, Resource... parents) throws
			NoSuchMethodException, InvocationTargetException, IllegalAccessException,
			InstantiationException {
		StringBuilder sb = new StringBuilder();
		String colName = (String) cls.getMethod("getCollectionName").invoke(null);
		if (colName != null && !colName.isEmpty()) {
			sb.insert(0, "/");
			sb.insert(0, colName);
		}
		for (int i = parents.length - 1; i >= 0; i--) {
			sb.insert(0, "/");
			sb.insert(0, parents[i].getId());

			colName = parents[i].getCollectionName();
			if (colName != null && !colName.isEmpty()) {
				sb.insert(0, "/");
				sb.insert(0, colName);
			}
		}
		sb.insert(0, serverUrl);
		return sb.toString();
	}

	public <T extends Resource> String getResourceUrl(Class<T> cls, String id, Resource... parents)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
			InstantiationException {
		StringBuilder sb = new StringBuilder(getCollectionUrl(cls, parents));
		sb.append(id);
		sb.append("/");
		return sb.toString();
	}

	private RequestQueue getRequestQueue() throws CertificateException, IOException,
			KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		// Setup cache
		Cache cache = new DiskBasedCache(app.getCacheDir(), HTTP_DISK_CACHE_SIZE);

		// Setup shared cookie manager
		CookieManager manager = new CookieManager();
		CookieHandler.setDefault(manager);

		// Don't follow redirection
		HttpURLConnection.setFollowRedirects(false);
		HttpsURLConnection.setFollowRedirects(false);

		// TODO get .crt file from server
		//HttpsURLConnection.setDefaultSSLSocketFactory(getSslContext(...).getSocketFactory());

		// Setup an HTTP stack that uses HttpURLConnection
		Network network = new BasicNetwork(new HurlStack());

		RequestQueue vq = new RequestQueue(cache, network);
		vq.start();
		return vq;
	}

	private ImageLoader getImageLoader(RequestQueue reqQueue) {
		return new ImageLoader(reqQueue,
				new ImageLoader.ImageCache() {
					private final LruCache<String, Bitmap>
							cache = new LruCache<String, Bitmap>(IMAGE_MEMORY_CACHE_SIZE);

					@Override
					public Bitmap getBitmap(String url) {
						return cache.get(url);
					}

					@Override
					public void putBitmap(String url, Bitmap bitmap) {
						cache.put(url, bitmap);
					}
				});
	}

	public interface ErrorListener {
		void onRedirect(int code, Map<String, String> headers, String url);

		void onError(String message, Throwable cause, int code, Map<String, String> headers);
	}

	public interface GetResourceListener<T> extends ErrorListener {
		void onResponse(int code, Map<String, String> headers, T resource);
	}

	public interface ListResourceListener<T> extends ErrorListener {
		void onResponse(int code, Map<String, String> headers, List<T> resources);
	}

	public interface PostResourceListener extends ErrorListener {
		void onResponse(int code, Map<String, String> headers);
	}

	public interface PutResourceListener extends ErrorListener {
		void onResponse(int code, Map<String, String> headers);
	}

	public interface DeleteResourceListener extends ErrorListener {
		void onResponse(int code, Map<String, String> headers);
	}

	public interface PostImageListener extends ErrorListener {
		void onResponse(int code);
	}

	private class RestErrorListener implements Response.ErrorListener {
		ErrorListener el;

		RestErrorListener(ErrorListener el) {
			this.el = el;
		}

		@Override
		public void onErrorResponse(VolleyError error) {
			String msg = error.getMessage();
			Throwable cause = error.getCause();
			NetworkResponse res = error.networkResponse;
			if (res == null) {
				Log.d(TAG, "Error: " + msg, cause);
				if (el != null) el.onError(msg, cause, 0, null);
			} else if (300 <= res.statusCode && res.statusCode < 400) {
				Log.d(TAG, "Response: " + res.statusCode + "\n" +
						Arrays.toString(res.headers.entrySet().toArray()));
				if (el != null)
					el.onRedirect(res.statusCode, res.headers, res.headers.get("Location"));
			} else {
				Log.d(TAG, "Error: " + res.statusCode, cause);
				if (el != null) el.onError(msg, cause, res.statusCode, res.headers);
			}
		}
	}
}

