package social.nickrest.http.request.type;

import social.nickrest.http.method.HTTPMethod;

public abstract class AdvancedHTTPResponse extends BasicHTTPResponse {

    public AdvancedHTTPResponse(String path, HTTPMethod method) {
        super(path, method);
    }

}
