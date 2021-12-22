package com.lunz.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
// 传给前端关注列表
public class VlogerVO {
    private String vlogerId;
    private String nickname;
    private String face;
    private boolean isFollowed = true;
}
