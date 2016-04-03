package com.baptechs.zeus.module.fccmanage.ifsf;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by warden on 2016/4/1.
 */
public class Session {

    @Getter @Setter             int                 id;

    @Getter @Setter             int                 type;
    @Getter @Setter             Date                created;
    @Getter @Setter             Date                updated;
    @Getter @Setter             int                 status;
}
