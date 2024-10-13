package social.nickrest.http;

import java.net.InetAddress;

public interface ClientConnectedHandle {
    void handle(InetAddress address);
}
