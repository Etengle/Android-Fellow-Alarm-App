package netdb.course.softwarestudio.service.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.Map;

public class GsonResponse<T> {
    private static Gson gson;

    static {
        gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    }

    private int code;
    private Map<String, String> headers;
    private T body;

    public GsonResponse(int code, Map<String, String> headers, T body) {
        this.code = code;
        this.headers = headers;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public T getBody() {
        return body;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(Integer.toString(code));
        sb.append("\n");
        sb.append(Arrays.toString(headers.entrySet().toArray()));
        sb.append("\n");
        if (body != null) sb.append(gson.toJson(body));
        return sb.toString();
    }
}
