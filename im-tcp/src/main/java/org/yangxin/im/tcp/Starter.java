package org.yangxin.im.tcp;

import org.yangxin.im.tcp.server.ImServer;

public class Starter {
    public static void main(String[] args) {
        new ImServer(9000);
    }
}
