package org.ditto.easyhan.grpc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.ditto.easyhan.model.Pinyin;

import java.util.List;

/**
 * Created by mellychen on 2017/11/19.
 */

@JsonPropertyOrder(value = {"word", "pinyin", "radical", "wuxing",
        "traditional", "wubi", "strokes", "strokenames", "strokes_count", "basemean", "detailmean", "terms", "riddles", "fanyi",
        "bishun","html"})
public class WordBaidu {
    public String word;
    public List<Pinyin> pinyins;
    public String radical;
    public String wuxing ;
    public String traditional ;
    public String wubi ;
    public List<String> strokes;
    public List<String> strokenames;
    public Integer strokes_count;
    public String basemean ;
    public String detailmean ;
    public List<String> terms;
    public List<String> riddles;
    public String fanyi;
    public String bishun;
    public String html;



}
