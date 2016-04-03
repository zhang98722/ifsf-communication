package com.baptechs.zeus.module.fccmanage.test;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.baptechs.zeus.module.fccmanage.message.ApplicationMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

/**
 * Created by warden on 2016/3/21.
 */
public class DecodeTest {

    @Test
    public void test(){
        ByteBuffer result = ByteBuffer.allocate(13);

        //02.01.01.02.00.29.00.05.01.21.14.01.03.
        result.put((byte)02);
        result.put((byte)01);
        result.put((byte)01);
        result.put((byte)02);
        result.put((byte)00);
        result.put((byte)29);
        result.put((byte)00);
        result.put((byte)05);
        result.put((byte)01);
        result.put((byte)21);
        result.put((byte)14);
        result.put((byte)01);
        result.put((byte) 03);

        ApplicationMessage message=ApplicationMessage.decode(result.array());
        System.out.println(JSONObject.toJSONString(message));
    }

    public static void main(String[] args) {
        BufferedReader strin=new BufferedReader(new InputStreamReader(System.in));
        try {
            strin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
