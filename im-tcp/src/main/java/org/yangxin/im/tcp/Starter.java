package org.yangxin.im.tcp;

import org.yangxin.im.tcp.server.ImServer;
import org.yangxin.im.tcp.server.ImWebSocketServer;

public class Starter {
    public static void main(String[] args) {
        new ImServer(9000);
        new ImWebSocketServer(19000);
    }
}
