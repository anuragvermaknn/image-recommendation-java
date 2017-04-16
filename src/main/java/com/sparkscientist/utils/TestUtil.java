package com.sparkscientist.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sparkscientist.beans.WordNCount;

import java.util.List;

/**
 * Created by anurag on 6/6/16.
 */
public class TestUtil {

  
  
  
  
    public static void main(String[] args) {



        Map<String,List<WordNCount>> stickerWithWordCounts =  new HashMap<String,List<WordNCount>>();


        String filePath = "/Users/anurag/Downloads/Recommendations-2.csv";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                String[] tokens = sCurrentLine.split(",");

                if(tokens.length > 1 && !tokens[0].isEmpty()){
                    String word = tokens[0];
                    System.out.println(tokens[1]);
                    Integer weight = Integer.parseInt(tokens[1]);
                    for(int i = 2; i < tokens.length; i++){
                        String stickerId = tokens[i];
                        if(!stickerId.isEmpty()) {
                            if (stickerWithWordCounts.containsKey(stickerId)) {
                                List<WordNCount> wordNCounts = stickerWithWordCounts.get(stickerId);
                                //System.out.println(" list of word counts before update -> stickerId : "+stickerId + "  wordNCounts : " + stickerWithWordCounts.get(stickerId));
                                Boolean matchFound = false;
                                for (WordNCount wordNCount : wordNCounts) {
                                    if (wordNCount.getWord().equalsIgnoreCase(word)) {
                                        matchFound = true;
                                        wordNCount.setCount(wordNCount.getCount() + weight);
                                        //System.out.println(" list of word counts after update -> stickerId : "+stickerId + "  wordNCounts : " + stickerWithWordCounts.get(stickerId));
                                    }

                                }
                                if (!matchFound) {
                                    stickerWithWordCounts.get(stickerId).add(new WordNCount(word, weight));
                                }
                            } else {
                                List<WordNCount> wordNCounts = new ArrayList<WordNCount>();
                                wordNCounts.add(new WordNCount(word, weight));
                                stickerWithWordCounts.put(stickerId, wordNCounts);
                            }
                        }
                    }
                }
            }
            File file = new File("/Users/anurag/Downloads/SeedData_10.csv");// creates the file
            file.createNewFile();
            // creates a FileWriter Object

            FileWriter writer = new FileWriter(file);

            for(String stickerId: stickerWithWordCounts.keySet()){
                writer.write(stickerId+"~");
                List<WordNCount> wordNCounts = stickerWithWordCounts.get(stickerId);
                boolean firstWordCrossed = false;
                for(WordNCount wordNCount : wordNCounts){
                    if(firstWordCrossed){
                        writer.write(",");
                    }
                    writer.write(wordNCount.getWord() + ":" + wordNCount.getCount());
                    firstWordCrossed = true;
                }
                writer.write("\n");
                System.out.println(" list of word counts -> stickerId : "+stickerId + "  wordNCounts : " + stickerWithWordCounts.get(stickerId));
            }

            writer.flush();
            writer.close();

            //_stickerWordCounts = stickerWordCounts;
           // return true;
        } catch(IOException e) {
            //TODO logging exception
            e.printStackTrace();
           // return false;
        }



    }
}
