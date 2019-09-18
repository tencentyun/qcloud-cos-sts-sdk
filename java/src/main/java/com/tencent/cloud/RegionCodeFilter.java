package com.tencent.cloud;

import java.util.ArrayList;
import java.util.List;

class RegionCodeFilter {

    private static List<String> blackList = new ArrayList<String>(3);

    synchronized static String convert(String region) {
        if (blackList.contains(region) || region == null || region.length() < 1) {
            return getDefaultRegion();
        }

        return region;
    }

    synchronized static boolean block(String region) {
        if (region.equals(getDefaultRegion())) {
            return false;
        }

        blackList.add(region);
        return true;
    }

    private static String getDefaultRegion() {
        return "ap-shanghai";
    }
}
