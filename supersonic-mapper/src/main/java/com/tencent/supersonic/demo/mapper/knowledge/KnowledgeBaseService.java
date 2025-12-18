package com.tencent.supersonic.demo.mapper.knowledge;

import com.hankcs.hanlp.collection.trie.bintrie.BinTrie;
import com.tencent.supersonic.demo.common.enums.DictWordType;
import com.tencent.supersonic.demo.common.util.EditDistanceUtils;
import com.tencent.supersonic.demo.mapper.helper.NatureHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 知识库服务
 * 提供前后缀搜索功能
 */
@Slf4j
@Service
public class KnowledgeBaseService {

    private static BinTrie<List<String>> trie = new BinTrie<>();
    private static BinTrie<List<String>> suffixTrie = new BinTrie<>();
    private static Map<Long, List<DictWord>> dimValueAlias = new HashMap<>();

    /**
     * 获取维度值别名映射
     */
    public static Map<Long, List<DictWord>> getDimValueAlias() {
        return dimValueAlias;
    }

    /**
     * 前缀搜索
     */
    public List<HanlpMapResult> prefixSearch(String key,
                                             Map<Long, List<Long>> modelIdToDataSetIds,
                                             Set<Long> detectDataSetIds) {
        return prefixSearch(key, 20, modelIdToDataSetIds, detectDataSetIds);
    }

    /**
     * 前缀搜索 - 搜索查询文本中包含的词
     */
    public List<HanlpMapResult> prefixSearch(String key, int limit,
                                             Map<Long, List<Long>> modelIdToDataSetIds,
                                             Set<Long> detectDataSetIds) {
        if (StringUtils.isBlank(key)) {
            return new ArrayList<>();
        }

        Set<Long> modelIdOrDataSetIds = findModelIdOrDataSetIds(modelIdToDataSetIds, detectDataSetIds);
        List<HanlpMapResult> results = new ArrayList<>();
        Set<String> foundWords = new HashSet<>();

        // 在trie中搜索 - 遍历查询文本的每个位置
        String lowerKey = key.toLowerCase();
        for (int i = 0; i < lowerKey.length(); i++) {
            String subKey = lowerKey.substring(i);
            trie.prefixSearch(subKey).forEach(entry -> {
                String name = entry.getKey();
                List<String> natures = entry.getValue();
                // 确保找到的词是查询文本的子串，且从当前位置开始
                if (natures != null && !natures.isEmpty() && subKey.startsWith(name) && !foundWords.contains(name)) {
                    foundWords.add(name);
                    double similarity = 1.0;  // 完全匹配
                    results.add(new HanlpMapResult(name, new ArrayList<>(natures), name, similarity));
                }
            });
        }

        // 按长度和相似度排序
        return results.stream()
                .sorted((a, b) -> b.getName().length() - a.getName().length())
                .peek(result -> {
                    List<String> filteredNatures = result.getNatures().stream()
                            .map(nature -> NatureHelper.changeModel2DataSet(nature, modelIdToDataSetIds))
                            .flatMap(Collection::stream)
                            .filter(nature -> {
                                if (CollectionUtils.isEmpty(detectDataSetIds)) {
                                    return true;
                                }
                                Long dataSetId = NatureHelper.getDataSetId(nature);
                                return dataSetId != null && detectDataSetIds.contains(dataSetId);
                            })
                            .collect(Collectors.toList());
                    result.setNatures(filteredNatures);
                })
                .filter(result -> !CollectionUtils.isEmpty(result.getNatures()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 后缀搜索
     */
    public List<HanlpMapResult> suffixSearch(String key, int limit,
                                             Map<Long, List<Long>> modelIdToDataSetIds,
                                             Set<Long> detectDataSetIds) {
        if (StringUtils.isBlank(key)) {
            return new ArrayList<>();
        }

        String reverseKey = StringUtils.reverse(key.toLowerCase());
        List<HanlpMapResult> results = new ArrayList<>();

        suffixTrie.prefixSearch(reverseKey).forEach(entry -> {
            String reverseName = entry.getKey();
            List<String> natures = entry.getValue();
            if (natures != null && !natures.isEmpty()) {
                String name = StringUtils.reverse(reverseName);
                List<String> cleanNatures = natures.stream()
                        .map(n -> n.replaceAll(DictWordType.SUFFIX.getType(), ""))
                        .collect(Collectors.toList());
                double similarity = EditDistanceUtils.getSimilarity(name, key);
                results.add(new HanlpMapResult(name, cleanNatures, key, similarity));
            }
        });

        return results.stream()
                .sorted((a, b) -> b.getName().length() - a.getName().length())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 添加词到词典
     */
    public void put(String word, String natureWithFrequency) {
        if (StringUtils.isBlank(word) || StringUtils.isBlank(natureWithFrequency)) {
            return;
        }
        String lowerWord = word.toLowerCase();
        // Parse nature from natureWithFrequency (format: "nature frequency")
        String nature = parseNature(natureWithFrequency);
        if (StringUtils.isBlank(nature)) {
            log.warn("Failed to parse nature for word: {}, natureWithFrequency: {}", word, natureWithFrequency);
            return;
        }

        List<String> existing = trie.get(lowerWord);
        if (existing == null) {
            existing = new ArrayList<>();
        }
        existing.add(nature);
        trie.put(lowerWord, existing);
    }

    /**
     * 添加后缀词到词典
     */
    public void putSuffix(String word, String natureWithFrequency) {
        if (StringUtils.isBlank(word) || StringUtils.isBlank(natureWithFrequency)) {
            return;
        }
        String reversedWord = StringUtils.reverse(word.toLowerCase());
        // Parse nature from natureWithFrequency (format: "nature frequency")
        String nature = parseNature(natureWithFrequency);
        if (StringUtils.isBlank(nature)) {
            log.warn("Failed to parse suffix nature for word: {}, natureWithFrequency: {}", word, natureWithFrequency);
            return;
        }
        // Add suffix marker
        String suffixNature = nature + DictWordType.SUFFIX.getType();

        List<String> existing = suffixTrie.get(reversedWord);
        if (existing == null) {
            existing = new ArrayList<>();
        }
        existing.add(suffixNature);
        suffixTrie.put(reversedWord, existing);
    }

    /**
     * Parse nature from natureWithFrequency string (format: "nature frequency")
     */
    private String parseNature(String natureWithFrequency) {
        if (StringUtils.isBlank(natureWithFrequency)) {
            return null;
        }
        // Format is "nature frequency", split by space
        String[] parts = natureWithFrequency.split("\\s+");
        if (parts.length > 0) {
            return parts[0];
        }
        return null;
    }

    /**
     * 添加维度值别名
     */
    public void addDimValueAlias(Long dimId, DictWord dictWord) {
        dimValueAlias.computeIfAbsent(dimId, k -> new ArrayList<>()).add(dictWord);
    }

    /**
     * 清空词典
     */
    public void clear() {
        log.info("Clearing knowledge base...");
        trie = new BinTrie<>();
        suffixTrie = new BinTrie<>();
        dimValueAlias = new HashMap<>();
    }

    /**
     * 查找模型ID或数据集ID
     */
    private static Set<Long> findModelIdOrDataSetIds(Map<Long, List<Long>> modelIdToDataSetIds,
                                                      Set<Long> detectDataSetIds) {
        if (CollectionUtils.isEmpty(detectDataSetIds)) {
            return new HashSet<>();
        }
        if (MapUtils.isEmpty(modelIdToDataSetIds)) {
            return new HashSet<>(detectDataSetIds);
        }
        Set<Long> result = modelIdToDataSetIds.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(detectDataSetIds::contains))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        result.addAll(detectDataSetIds);
        return result;
    }

    /**
     * 更新语义知识库
     */
    public void updateKnowledge(List<DictWord> dictWords) {
        if (CollectionUtils.isEmpty(dictWords)) {
            return;
        }
        for (DictWord dictWord : dictWords) {
            if (StringUtils.isNotBlank(dictWord.getNatureWithFrequency())) {
                put(dictWord.getWord(), dictWord.getNatureWithFrequency());
                // 如果是维度或指标，也添加后缀
                if (dictWord.getNature().contains(DictWordType.DIMENSION.getType())
                        || dictWord.getNature().contains(DictWordType.METRIC.getType())) {
                    String suffixNature = dictWord.getNatureWithFrequency() + DictWordType.SUFFIX.getType();
                    putSuffix(dictWord.getWord(), suffixNature);
                }
            }
        }
        log.info("Updated knowledge base with {} words", dictWords.size());
    }
}
