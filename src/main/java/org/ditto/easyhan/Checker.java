package org.ditto.easyhan;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.easyhan.word.HanZi;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class Checker {
    public static void main(String[] args) {
//        checkFiles();
//        exerciseCodePoint();
        String[] words1 = StringUtils.splitByWholeSeparator(HanZi.LEVEL1, ",");
        String[] words2 = StringUtils.splitByWholeSeparator(HanZi.LEVEL2, ",");
        String[] words3 = StringUtils.splitByWholeSeparator(HanZi.LEVEL3, ",");
        int start1Idx = 1;
        int start2Idx = words1.length+1;
        int start3Idx = words1.length+words2.length+1;

        checkBaiduWordFiles(words1,start1Idx);
        checkBaiduWordFiles(words2,start2Idx);
        checkBaiduWordFiles(words3,start3Idx);
    }

    private static void exerciseCodePoint() {
        String aa = "\uD86D\uDCEF";

        System.out.printf("%s %x %x\n", aa, (int) aa.toCharArray()[0], (int) aa.toCharArray()[1]);

        char[] chars = aa.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            sb.append("u" + Integer.toHexString(c));
        }
        System.out.printf("%s,%x", sb.toString(), aa.codePointAt(0));

        String cp = "2B4EF";
        Integer cpi = Integer.valueOf(cp, 16);
        String cpw = codePointToString1(cpi);
        String cpw2 = codePointToString(cpi);

        int i = 0;
    }

    private static String codePointToString1(int codePoint) {
        return new String(Character.toChars(codePoint));
    }

    private static String codePointToString(int codePoint) {
        StringBuilder stringOut = new StringBuilder();
        stringOut.appendCodePoint(codePoint);
        return stringOut.toString();
    }

    private static void input() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入字符串：");
        while (true) {
            String s = scanner.next();
            if (s.equals("exit")) {
                break;
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                sb.append("\\u" + Integer.toHexString(c));
            }
            System.out.println(sb);
        }
    }


    public static void checkFiles() {
        String filename = "/Users/mellychen/hiask/easyhan-cloud/src/main/resources/words/temp.txt";

        Collection<File> files = FileUtils.listFiles(new File("/Users/mellychen/hiask/easyhan-cloud/src/main/resources/words/"), new String[]{"html"}, false);
//            List<String> lines = FileUtils.readLines(new File(filename), StandardCharsets.UTF_8);
        java.util.Iterator<File> fileIterator = files.iterator();
        for (int i = 0; i < files.size(); i++) {
            if (!fileIterator.next().getName().startsWith(String.format("%04d", i + 1))) {
                System.out.println(String.format("missing %04d", i + 1));
                break;
            }
        }
    }


    public static void checkBaiduWordFiles(String[] words,int startIdx) {

        for (int i = 0; i < words.length; i++) {
            int cp = words[i].codePointAt(0);
            String fn = String.format("/Users/mellychen/hiask/easyhan-cloud/src/main/resources/words/%x.html", cp);

            try {
                String html = FileUtils.readFileToString(new File(fn), StandardCharsets.UTF_8);
                Document doc = Jsoup.parse(html);
                Element e = doc.getElementById("results");
                if (e != null) {
                    //百度收录了这个汉字
//                    System.out.println(String.format("✔ %d 百度收录了汉字=%s fn=%s", i+startIdx, words[i],fn));
                } else {
                    System.out.println(String.format("✕ %d 百度没收录汉字=%s fn=%s", i+startIdx, words[i],fn));
                }

            } catch (FileNotFoundException fe) {
                System.out.println(String.format("✕ %d 没有保存汉字=%s fn=%s", i+startIdx, words[i],fn));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
