package com.demo.common;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketUtils {
    private SocketUtils() {
        // nothing to initialize
    }

    public static int findAvailablePort() {
        // private port range
        int start = 8080;
        int end = 65535;
        int port = start;
        while (port <= end) {
            if (isPortAvailable(port)) {
                return port;
            }
            port++;
        }

        throw new IllegalStateException("Can't find available port number");
    }

    public static boolean isPortAvailable(int port) {
        try {
            new ServerSocket(port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
