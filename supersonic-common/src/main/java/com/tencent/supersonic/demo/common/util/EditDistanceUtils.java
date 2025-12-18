package com.tencent.supersonic.demo.common.util;

import com.hankcs.hanlp.algorithm.EditDistance;

/**
 * 编辑距离工具类
 * 用于计算两个字符串之间的相似度
 */
public class EditDistanceUtils {

    /**
     * 计算两个字符串的相似度
     *
     * @param detectSegment 检测文本片段
     * @param matchName 待匹配名称
     * @return 相似度(0.0-1.0)
     */
    public static double getSimilarity(String detectSegment, String matchName) {
        if (detectSegment == null || matchName == null) {
            return 0.0;
        }
        String detectSegmentLower = detectSegment.toLowerCase();
        String matchNameLower = matchName.toLowerCase();
        int maxLength = Math.max(matchName.length(), detectSegment.length());
        if (maxLength == 0) {
            return 1.0;
        }
        return 1 - (double) EditDistance.compute(detectSegmentLower, matchNameLower) / maxLength;
    }
}
