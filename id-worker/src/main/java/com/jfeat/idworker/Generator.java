package com.jfeat.idworker;

import com.baomidou.mybatisplus.toolkit.IdWorker;

/**
 * Created by jackyhuang on 16/10/26.
 */
public class Generator {
    public static void main(String[] args) throws Exception {
        Long id = IdWorker.getId();
        System.out.print(id);
    }
}
