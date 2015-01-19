package netdb.course.softwarestudio.service.rest;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class GsonRequest<T> extends JsonRequest<GsonResponse<T>> {
    private static Gson gson, putGson;

    //Map<String, String> params;
    Map<String, String> headers;
    private Class<T> bodyCls;

    static {
        gson = new GsonBuilder().create();
        putGson = new GsonBuilder().setExclusionStrategies(new PutExclusionStrategy())
                .serializeNulls().create();
    }

    /**
     * For GET.
     *
     * @param url
     * @param params
     * @param headers
     * @param bodyCls
     * @param listener
     * @param errorListener
     */
    public GsonRequest(String url, Map<String, String> params, Map<String, String> headers, Class<T> bodyCls,
                       Response.Listener<GsonResponse<T>> listener,
                       Response.ErrorListener errorListener) throws UnsupportedEncodingException {
        super(Method.GET, getUrlWithQuery(url, params), null, listener, errorListener);
        // Volley does not call {@link #getParams()} in GET. Need to translate params to query
        // string manually.
        // this.params = params;
        this.headers = headers;
        this.bodyCls = bodyCls;
    }

    /**
     * For POST, PUT, and DELETE.
     *
     * @param method
     * @param url
     * @param headers
     * @param body
     * @param listener
     * @param errorListener
     */
    public GsonRequest(int method, String url, Map<String, String> headers, T body,
                       Class<T> bodyCls, Response.Listener<GsonResponse<T>> listener,
                       Response.ErrorListener errorListener) {
        super(method, url, method == Method.PUT ? putGson.toJson(body) : gson.toJson(body),
               listener, errorListener);
	    this.bodyCls = bodyCls;
        this.headers = headers;
    }

    private static String getUrlWithQuery(String url, Map<String, String> params) throws UnsupportedEncodingException {
        if (params == null) return url;

        StringBuilder sb = new StringBuilder(url);
        sb.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            sb.append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    //@Override
    //public Map<String, String> getParams() {
    //    return params;
    //}

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Response<GsonResponse<T>> parseNetworkResponse(NetworkResponse response) {
        try {
            T resource = null;
            if (response.data != null && response.data.length > 0) {
                String json = new String(response.data, HttpHeaderParser.parseCharset(
                        response.headers));
                resource = gson.fromJson(json, bodyCls);
            }
            GsonResponse<T> res = new GsonResponse<T>(response.statusCode, response.headers,
                    resource);
            return Response.success(res, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}