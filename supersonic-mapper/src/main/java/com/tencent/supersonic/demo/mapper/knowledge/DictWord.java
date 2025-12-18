package com.tencent.supersonic.demo.mapper.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;

/**
 * 词典词
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictWord {

    private String word;                // 词
    private String nature;              // 词性
    private String natureWithFrequency; // 带频率的词性
    private String alias;               // 别名

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DictWord that = (DictWord) o;
        return Objects.equals(word, that.word)
                && Objects.equals(natureWithFrequency, that.natureWithFrequency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, natureWithFrequency);
    }
}
