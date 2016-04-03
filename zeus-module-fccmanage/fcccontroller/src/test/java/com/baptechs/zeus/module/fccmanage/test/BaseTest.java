package com.baptechs.zeus.module.fccmanage.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by warden on 2016/3/22.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= "classpath:spring-config-test.xml")
public class BaseTest {

    @Test
    public void baseTest() throws InterruptedException {
        Thread.sleep(1000*60*60);
    }
}
