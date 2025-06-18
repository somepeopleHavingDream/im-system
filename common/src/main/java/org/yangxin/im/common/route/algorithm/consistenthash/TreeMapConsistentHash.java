package org.yangxin.im.common.route.algorithm.consistenthash;

import org.yangxin.im.common.enums.UserErrorCode;
import org.yangxin.im.common.exception.ApplicationException;

import java.util.SortedMap;
import java.util.TreeMap;

public class TreeMapConsistentHash extends AbstractConsistentHash {
    private static final int NODE_SIZE = 2;
    private final TreeMap<Long, String> treeMap = new TreeMap<>();

    @Override
    protected void add(long key, String value) {
        for (int i = 0; i < NODE_SIZE; i++) {
            treeMap.put(super.hash("node" + key + i), value);
        }
        treeMap.put(key, value);
    }

    @Override
    protected String getFirstNodeValue(String value) {
        Long hash = super.hash(value);
        SortedMap<Long, String> last = treeMap.tailMap(hash);
        if (!last.isEmpty()) {
            return last.get(last.firstKey());
        }
        if (treeMap.isEmpty()) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        return treeMap.firstEntry().getValue();
    }

    @Override
    protected void processBefore() {

    }
}
