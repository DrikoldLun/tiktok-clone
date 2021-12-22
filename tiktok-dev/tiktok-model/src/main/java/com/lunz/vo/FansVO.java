package com.lunz.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
// 传给前端粉丝列表
public class FansVO {
    private String fanId;
    private String nickname;
    private String face;
    private boolean isFriend = false; // 互粉用
}
