package org.ditto.easyhan.data;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ignite.Ignite;
import org.ditto.easyhan.grpc.WordBaidu;
import org.ditto.easyhan.model.Pinyin;
import org.ditto.easyhan.model.Word;
import org.ditto.easyhan.repository.WordRepository;
import org.easyhan.common.grpc.HanziLevel;
import org.easyhan.word.HanZi;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


public class BaiduHanZiUtil {
    private static final Logger logger = Logger.getLogger(BaiduHanZiUtil.class.getName());
    private final static Gson gson = new Gson();


    public static List<Pinyin> tone_py(Document doc) {
        List<Pinyin> result = new ArrayList<>();
        try {
            Element e = doc.getElementById("pinyin");
            Elements spans = e.getElementsByTag("span");
            for (int i = 0; spans != null && i < spans.size(); i++) {
                try {
                    String pinyin = spans.get(i).getElementsByTag("b").get(0).childNode(0).outerHtml();
                    String mp3 = spans.get(i).getElementsByTag("a").get(0).attr("url");
                    result.add(new Pinyin(pinyin, mp3));
                } catch (Exception e1) {
                    e1.printStackTrace();
                } finally {

                }
            }
//            logger.info(String.format("pinyin=%s", gson.toJson(result)));
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {

        }

        return result;
    }

    public static  String radical(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("radical").child(1).html();
//            logger.info(String.format("radical=%s", result));
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {

        }
        return result;
    }


    public static  String wuxing(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("wuxing").child(1).html();
//            logger.info(String.format("wuxing=%s", result));
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {

        }
        return result;
    }

    public static  String traditional(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("traditional").child(1).html();
//            logger.info(String.format("traditional=%s", result));
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {

        }
        return result;
    }


    public static  String wubi(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("wubi").child(1).html();
//            logger.info(String.format("wubi=%s", result));
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {

        }
        return result;
    }


    public static  List<String> strokes(Document doc) {
        List<String> result = new ArrayList<>();
        try {
            String strokes = doc.getElementsMatchingOwnText("笔顺 ：").get(0).parent().childNode(1).outerHtml();
//            String strokes = doc.getElementById("strokenames").childNode(1).outerHtml();
            String[] strokeArr = StringUtils.splitByWholeSeparator(StringUtils.remove(strokes, "&nbsp;").trim(), " ");
            for (int j = 0; j < strokeArr.length; j++) {
                if (StringUtils.isNotEmpty(strokeArr[j])) {
                    result.add(StringUtils.trim(strokeArr[j]));
                }
            }
//            logger.info(String.format("strokes=%s", gson.toJson(result)));
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {

        }

        return result;
    }


    public static  List<String> strokenames(Document doc) {
        List<String> result = new ArrayList<>();
        try {
            String strokes = doc.getElementById("stroke").childNode(1).outerHtml();
            String[] strokenameArr = StringUtils.splitByWholeSeparator(StringUtils.remove(strokes, "\u00a0 ").trim(), "、");
            for (int j = 0; j < strokenameArr.length; j++) {
                String s = StringUtils.trim(strokenameArr[j]);
                if (StringUtils.isNotEmpty(s)) {
                    result.add(s);
                    if (!isValidStrokeName(s)) {
                        logger.warning(String.format("unknown strokename=%s", s));
                    }
                }
            }
//            logger.info(String.format("strokenames=%s", gson.toJson(result)));
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {

        }

        return result;
    }

    public static  boolean isValidStrokeName(String s) {
        boolean valid = false;
        if (StringUtils.isNotEmpty(s)) {
            String[] names = StringUtils.splitByWholeSeparator(s, "/");

            for (int i = 0; i < names.length; i++) {
                String[] definition = HanZi.STROKE_NAMES.get(names[i]);
                if (definition != null) {
                    valid = true;
                    break;
                }
            }
        }
        return valid;
    }


    public static  String basemean(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("base-mean").html();
//            logger.info(String.format("basemean=%s", result));
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {

        }
        return result;
    }


    public static  String detailmean(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("detail-mean").html();
//            logger.info(String.format("detailmean=%s", result));
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {

        }
        return result;
    }


    public static  List<String> term(Document doc) {
        List<String> result = new ArrayList<>();
        try {
            Elements elements = doc.getElementById("term").getElementsByTag("a");
            for (int i = 0; elements != null && i < elements.size(); i++) {
                result.add(elements.get(i).html());
            }
//            logger.info(String.format("term=%s", gson.toJson(result)));
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {

        }
        return result;
    }


    public static  List<String> riddle(Document doc) {
        List<String> result = new ArrayList<>();
        try {
            Elements elements = doc.getElementById("riddle-wrapper").getElementsByTag("p");
            for (int i = 0; elements != null && i < elements.size(); i++) {
                result.add(elements.get(i).html());
            }
//            logger.info(String.format("riddle=%s", gson.toJson(result)));
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {

        }

        return result;
    }

    public static  String fanyi(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("fanyi-wrapper").getElementsByTag("p").get(0).html();
//            logger.info(String.format("fanyi=%s", result));
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {

        }

        return result;
    }


    public static  String word_bishun(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("word_bishun").attr("data-src");
//            logger.info(String.format("word_bishun=%s", result));
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {

        }

        return result;
    }
}
