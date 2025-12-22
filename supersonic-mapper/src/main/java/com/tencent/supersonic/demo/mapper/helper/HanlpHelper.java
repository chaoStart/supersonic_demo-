package com.tencent.supersonic.demo.mapper.helper;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.tencent.supersonic.demo.common.enums.DictWordType;
import com.tencent.supersonic.demo.mapper.knowledge.DictWord;
import com.tencent.supersonic.demo.mapper.knowledge.HanlpMapResult;
import com.tencent.supersonic.demo.semantic.response.S2Term;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * HanLP分词辅助类
 */
@Slf4j
public class HanlpHelper {

    private static final Pattern PATTERN = Pattern.compile("[\u4e00-\u9fa5]");
    private static final int TERM_FREQUENCY = 1000;
    private static Segment segment;
    private static CustomDictionary dictionary;

    static {
        // 初始化分词器
        segment = HanLP.newSegment()
                .enableCustomDictionary(true)
                .enableAllNamedEntityRecognize(false);
    }

    /**
     * 获取分词结果
     */
    public static List<S2Term> getTerms(String text, Map<Long, List<Long>> modelIdToDataSetIds) {
        List<S2Term> result = new ArrayList<>();
        if (StringUtils.isBlank(text)) {
            return result;
        }

        // 使用HanLP进行分词
        List<Term> termList = segment.seg(text.toLowerCase());

        int offset = 0;
        for (Term term : termList) {
            String nature = term.nature != null ? term.nature.toString() : "";

            S2Term s2Term = S2Term.builder()
                    .word(term.word)
                    .nature(nature)
                    .frequency(TERM_FREQUENCY)
                    .offset(offset)
                    .build();
            result.add(s2Term);
            offset += term.word.length();
        }

        return result;
    }

    /**
     * 过滤分词结果，保留指定数据集相关的词
     */
    public static List<S2Term> getTerms(List<S2Term> terms, Set<Long> dataSetIds) {
        if (CollectionUtils.isEmpty(dataSetIds)) {
            return terms;
        }
        return terms.stream()
                .filter(term -> {
                    String nature = term.getNature();
                    if (!nature.startsWith(DictWordType.NATURE_SPILT)) {
                        return true;
                    }
                    Long dataSetId = NatureHelper.getDataSetId(nature);
                    return dataSetId != null && dataSetIds.contains(dataSetId);
                })
                .collect(Collectors.toList());
    }

    /**
     * 添加词到自定义词典
     */
    public static void addToCustomDictionary(DictWord dictWord) {
        if (dictWord == null || StringUtils.isBlank(dictWord.getWord())) {
            return;
        }
        try {
            String word = dictWord.getWord().toLowerCase();
            String nature = dictWord.getNatureWithFrequency();
            if (StringUtils.isNotBlank(nature)) {
                CustomDictionary.add(word, nature);
            } else {
                CustomDictionary.add(word);
            }
        } catch (Exception e) {
            log.error("Failed to add word to dictionary: {}", dictWord, e);
        }
    }

    /**
     * 从词典中移除词
     */
    public static void removeFromCustomDictionary(String word) {
        if (StringUtils.isNotBlank(word)) {
            try {
                CustomDictionary.remove(word.toLowerCase());
            } catch (Exception e) {
                log.error("Failed to remove word from dictionary: {}", word, e);
            }
        }
    }

    /**
     * 判断是否包含中文字符
     */
    public static boolean containsChinese(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        Matcher m = PATTERN.matcher(str);
        return m.find();
    }

    /**
     * 转换字母为原始形式
     */
    public static void transLetterOriginal(List<HanlpMapResult> mapResults) {
        // 简化处理，保持原样
    }
}
