package com.tesco.pma.api;

import java.time.Instant;

import lombok.Data;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 13.10.2021 Time: 22:58
 */
@Data
public class StatusHistoryRecord<K, S extends DictionaryItem<Integer>> {
    private K id;
    private S status;
    private Instant updateTime;
}
