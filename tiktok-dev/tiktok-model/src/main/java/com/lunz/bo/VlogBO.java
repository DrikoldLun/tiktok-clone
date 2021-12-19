package com.lunz.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VlogBO {
    private String id;
    @NotBlank
    private String vlogerId;
    @NotBlank
    @URL
    private String url;
    @NotBlank
    @URL
    private String cover;
    @NotBlank(message = "请输入标题~")
    private String title;
    private Integer width;
    private Integer height;
    private Integer likeCounts;
    private Integer commentsCounts;
}
