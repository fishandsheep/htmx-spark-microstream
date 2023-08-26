package org.fisheep.mysqllogscan.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

    private String name;

    private String nickName;

    private String mobile;

    private String email;

}
