package cn.edu.xidian.ictt.yk.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by heart_sunny on 2018/10/20
 */
public class InputUtil {

    private static final BufferedReader KEYBOARD_INPUT = new BufferedReader(new InputStreamReader(System.in));

    public static String getString(String prompt) {
        String returnData = null;
        boolean flag = true;

        while (flag) {
            System.out.println(prompt);
            try {
                returnData = KEYBOARD_INPUT.readLine();
                if (returnData == null || "".equals(returnData)) {
                    System.out.println("输入的数据不允许为空");
                } else {
                    flag = false;
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("数据输入错误");
            }
        }
        return returnData;
    }
}
