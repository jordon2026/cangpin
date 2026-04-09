package com.jujing.museum.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码生成工具 - 算术验证码
 */
public class CaptchaUtil {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final Random RANDOM = new Random();

    /**
     * 生成算术验证码
     *
     * @return [算术表达式文本, 答案, base64图片]
     */
    public static String[] generateCaptcha() {
        // 生成随机数
        int num1 = RANDOM.nextInt(20) + 1;
        int num2 = RANDOM.nextInt(20) + 1;
        // 随机选择运算符
        String[] operators = {"+", "-", "*"};
        String operator = operators[RANDOM.nextInt(operators.length)];

        int answer;
        String expression;
        switch (operator) {
            case "+":
                answer = num1 + num2;
                break;
            case "-":
                // 确保结果非负
                if (num1 < num2) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                }
                answer = num1 - num2;
                break;
            case "*":
                // 乘法限制范围
                num1 = RANDOM.nextInt(9) + 1;
                num2 = RANDOM.nextInt(9) + 1;
                answer = num1 * num2;
                break;
            default:
                answer = num1 + num2;
        }

        expression = num1 + " " + operator + " " + num2 + " = ?";
        String base64Image = drawCaptcha(expression);

        return new String[]{expression, String.valueOf(answer), base64Image};
    }

    /**
     * 绘制验证码图片并返回base64
     */
    private static String drawCaptcha(String text) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 边框
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        // 绘制干扰线
        for (int i = 0; i < 4; i++) {
            g.setColor(getRandomColor(150, 200));
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制干扰点
        for (int i = 0; i < 30; i++) {
            g.setColor(getRandomColor(150, 200));
            int x = RANDOM.nextInt(WIDTH);
            int y = RANDOM.nextInt(HEIGHT);
            g.fillOval(x, y, 2, 2);
        }

        // 绘制验证码文字
        g.setFont(new Font("Arial", Font.BOLD, 22));
        char[] chars = text.toCharArray();
        int totalWidth = chars.length * 18;
        int startX = (WIDTH - totalWidth) / 2;
        for (int i = 0; i < chars.length; i++) {
            g.setColor(getRandomColor(20, 130));
            // 随机旋转
            double theta = (RANDOM.nextDouble() - 0.5) * 0.4;
            g.rotate(theta, startX + i * 18 + 8, HEIGHT / 2.0 + 4);
            g.drawString(String.valueOf(chars[i]), startX + i * 18, HEIGHT / 2 + 8);
            g.rotate(-theta, startX + i * 18 + 8, HEIGHT / 2.0 + 4);
        }

        g.dispose();

        // 转换为 base64
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    /**
     * 获取随机颜色
     */
    private static Color getRandomColor(int min, int max) {
        int r = RANDOM.nextInt(max - min) + min;
        int g = RANDOM.nextInt(max - min) + min;
        int b = RANDOM.nextInt(max - min) + min;
        return new Color(r, g, b);
    }
}
