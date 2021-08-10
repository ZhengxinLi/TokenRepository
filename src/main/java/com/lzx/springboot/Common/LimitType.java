package com.lzx.springboot.Common;
/**
 * @author lizhengxin<lizhengxin.lzx @ bytedance.com>
 * @date 08/02/2021 8:53 下午
 **/

/**
 * *****************************************************
 * Copyright (C) 2021 bytedance.com. All Rights Reserved
 * This file is part of bytedance EA project.
 * Unauthorized copy of this file, via any medium is strictly prohibited.
 * Proprietary and Confidential.
 * ****************************************************
 **/
// 限制的类型
public enum LimitType {

    /**
     * 自定义key
     */
    CUSTOMER,
    /**
     * 根据请求者IP
     */
    IP;

}
