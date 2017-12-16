package org.ditto.easyhan.data;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.ditto.easyhan.model.Pinyin;
import org.easyhan.word.HanZi;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class BaiduHanziCheckAndDownload {
    public static void main(String[] args) {
//        checkFiles();
//        exerciseCodePoint();
//        checkAllBaiduFiles();

//        downloadPinyinsMp3(HanZi.LEVEL1);
//        downloadPinyinsMp3(HanZi.LEVEL2);
//        downloadPinyinsMp3(HanZi.LEVEL3);

        downloadBishun(HanZi.LEVEL1);
        downloadBishun(HanZi.LEVEL2);
        downloadBishun(HanZi.LEVEL3);
    }

    private static void checkAllBaiduFiles() {
        int start1Idx = 1;
        int start2Idx = HanZi.LEVEL1.length + 1;
        int start3Idx = HanZi.LEVEL1.length + HanZi.LEVEL2.length + 1;

        checkBaiduWordFiles(HanZi.LEVEL1, start1Idx);
        checkBaiduWordFiles(HanZi.LEVEL2, start2Idx);
        checkBaiduWordFiles(HanZi.LEVEL3, start3Idx);
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


    public static void checkBaiduWordFiles(String[] words, int startIdx) {

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
                    String fn_ = String.format("/Users/mellychen/hiask/easyhan-cloud/src/main/resources/words_/%x.html",
                            cp);
                    try {
                        String html_ = FileUtils.readFileToString(new File(fn_), StandardCharsets.UTF_8);
                        System.out.println(String.format("✕ %d 百度没收录汉字=%s manually defined fn_=%s", i + startIdx,
                                words[i],
                                fn_));
                    } catch (FileNotFoundException fe_) {
                        try {
                            FileUtils.writeStringToFile(new File(fn_), html, StandardCharsets.UTF_8);
                        } catch (IOException e_) {
                            e_.printStackTrace();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

            } catch (FileNotFoundException fe) {
                System.out.println(String.format("✕ %d 没有保存汉字=%s fn=%s", i + startIdx, words[i], fn));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void downloadPinyinsMp3(String[] words) {
        Tika tika = new Tika();
        //Get file from resources folder
        for (int i = 0; i < words.length; i++) {
            File file = new File(getWordFileName(words[i]));
            try {
                String html = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                Document doc = Jsoup.parse(html);
                Element e = doc.getElementById("results");
                if (e != null) {
                    List<Pinyin> pinyins = BaiduHanZiUtil.tone_py(doc);
                    if (pinyins != null) {
                        for (int j = 0; j < pinyins.size(); j++) {
                            Pinyin pinyin = pinyins.get(j);
                            if (StringUtils.isNotEmpty(pinyin.mp3)) {
                                String fn = String.format
                                        ("/Users/mellychen/hiask/easyhan-cloud/src/main/resources/pinyin/%s",
                                                StringUtils.substring(pinyin.mp3, pinyin.mp3.lastIndexOf('/') + 1));
                                File mp3File = new File(fn);
                                if (!mp3File.exists()) {
                                    try {
                                        FileUtils.copyURLToFile(new URL(pinyin.mp3), mp3File, 20000, 30000);
                                        System.out.println(String.format("ok download mp3 %04d: %s as %s", i, pinyin
                                                .mp3, fn));
                                    } catch (IOException e1) {
                                        System.out.println(String.format("fail download mp3 %04d: codepoint=%x %s %s",
                                                i, words[i].codePointAt(0), words[i], pinyin.mp3));
                                    }
                                } else {
                                    String type = tika.detect(mp3File);
                                    if (!"audio/mpeg".equals(type)) {
                                        try {
                                            FileUtils.copyURLToFile(new URL(pinyin.mp3), mp3File, 20000, 30000);
                                            System.out.println(String.format("ok redownload mp3 %04d:%s  %s as %s",
                                                    i, type, pinyin.mp3, fn));
                                        } catch (IOException e1) {
                                            System.out.println(String.format("fail redownload mp3 %04d: codepoint=%x %s %s  %s",
                                                    i, words[i].codePointAt(0), words[i], type, pinyin.mp3));
                                        }
                                    }
                                }
                            } else {
                                System.out.println(String.format("not defined pinyins %04d: codepoint=%x %s", i,
                                        words[i].codePointAt(0), words[i]));
                            }
                        }
                    } else {
                        System.out.println(String.format("not defined pinyins %04d: codepoint=%x %s", i, words[i].codePointAt(0), words[i]));
                    }
                }
            } catch (IOException e) {
                System.out.println(String.format("not defined pinyins %04d: codepoint=%x %s ",
                        i, words[i].codePointAt(0), words[i]));

            }
        }
    }

    public static void downloadBishun(String[] words) {
        Tika tika = new Tika();
        //Get file from resources folder
        for (int i = 0; i < words.length; i++) {
            File file = new File(getWordFileName(words[i]));
            try {
                String html = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                Document doc = Jsoup.parse(html);
                Element e = doc.getElementById("results");
                if (e != null) {
                    String word_bishun = BaiduHanZiUtil.word_bishun(doc);
                    if (StringUtils.isNotEmpty(word_bishun)) {
                        String fn = String.format
                                ("/Users/mellychen/hiask/easyhan-cloud/src/main/resources/bishun/%x_%s",
                                        words[i].codePointAt(0),
                                        StringUtils.substring(word_bishun, word_bishun.lastIndexOf('/') + 1));
                        File bishunFile = new File(fn);
                        if (!bishunFile.exists()) {

                            try {
                                FileUtils.copyURLToFile(new URL(word_bishun), bishunFile, 20000, 30000);
                                System.out.println(String.format("ok download word_bishun %04d: %s as %s",
                                        i, word_bishun, fn));
                            } catch (IOException e1) {
                                System.out.println(String.format("fail download word_bishun %04d: codepoint=%x %s %s",
                                        i, words[i].codePointAt(0), words[i], word_bishun));
                            }
                        } else {
                            String type = tika.detect(bishunFile);
                            if (!"image/gif".equals(type)) {
                                try {
                                    FileUtils.copyURLToFile(new URL(word_bishun), bishunFile, 20000, 30000);
                                    System.out.println(String.format("ok redownload word_bishun %04d: %s as %s",
                                            i, word_bishun, fn));
                                } catch (IOException e1) {
                                    System.out.println(String.format("fail redownload word_bishun %04d: codepoint=%x %s %s",
                                            i, words[i].codePointAt(0), words[i],
                                            word_bishun));
                                }
                            }
                        }
                    } else {
                        System.out.println(String.format("not defined word_bishun %04d: %s codepoint=%x", i,
                                words[i], words[i].codePointAt(0)));
                    }
                } else {
                    System.out.println(String.format("not defined word_bishun %04d: %s codepoint=%x", i,
                            words[i], words[i].codePointAt(0)));
                }
            } catch (IOException e) {
                System.out.println(String.format("not defined word_bishun %04d: %s codepoint=%x", i,
                        words[i], words[i].codePointAt(0)));
            }
        }
    }

    private static String getWordFileName(String word) {
        return
                String.format("/Users/mellychen/hiask/easyhan-cloud/src/main/resources/words/%x.html",
                        word.codePointAt(0));
    }

}
