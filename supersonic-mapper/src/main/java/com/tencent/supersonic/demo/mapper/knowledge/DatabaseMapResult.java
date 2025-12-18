package com.tencent.supersonic.demo.mapper.knowledge;

import com.tencent.supersonic.demo.semantic.pojo.SchemaElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 数据库匹配结果
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DatabaseMapResult extends MapResult {

    /**
     * 匹配到的Schema元素
     */
    private SchemaElement schemaElement;

    public DatabaseMapResult(String name, String detectWord, SchemaElement schemaElement, double similarity) {
        this.name = name;
        this.detectWord = detectWord;
        this.schemaElement = schemaElement;
        this.similarity = similarity;
    }

    @Override
    public String getMapKey() {
        if (schemaElement != null) {
            return name + schemaElement.getId() + schemaElement.getName();
        }
        return name;
    }
}
