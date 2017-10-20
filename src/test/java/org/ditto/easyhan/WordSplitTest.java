package org.ditto.easyhan;

import org.ditto.easyhan.data.WordDataImportRunner;

public class WordSplitTest {

    public static void main(String[] args) {
        String[] words = WordDataImportRunner.hanzi_level2.split("\\)");
        for (int i = 0; i < words.length; i++) {
            String[] wps = words[i].split("\\(");
            System.out.println(String.format("%04d,%s,%s",i+01,wps[0],wps[1]));
        }

    }

}
