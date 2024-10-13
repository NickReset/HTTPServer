package social.nickrest.http.request.type;

import social.nickrest.http.method.HTTPMethod;

public abstract class AdvancedHTTPRequest extends BasicHTTPRequest{

    public AdvancedHTTPRequest(String path, HTTPMethod method) {
        super(path, method);
    }

}
