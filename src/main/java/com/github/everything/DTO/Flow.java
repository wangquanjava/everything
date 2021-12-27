package com.github.everything.DTO;

import lombok.Data;

/**
 * @author wangquan07
 * 2021/12/19 01:22
 */
@Data
public class Flow {
    private int id;
    private int input_id;
    private String table_name;
    private String cols;
    private String unique_cols;
}