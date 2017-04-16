/**
 * @author Anurag
 * @description 
 */
package com.sparkscientist.stickerrecommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.sparkscientist.utils.LevenshteinDistance;

import info.debatty.java.stringsimilarity.SorensenDice;

/**
 * @author Anurag
 * @description 
 */
public class TestDistance {

  private static final double STICKER_COEFFICIENT =  0.74;
  private static SorensenDice sorensenDice = new SorensenDice(1);

  public static List<String> oneLevenDistWords(String word){
    List<String> nearWords = new ArrayList<String>();
    
    //d time -> time0
    nearWords.add(word + '0');
    
    //e time -> tim0
    nearWords.add(word.substring(0, word.length() - 1) + '0');
    
    //a time -> tim 
    nearWords.add(word.substring(0, word.length() - 1));
    
    //ed time -> tim00
    nearWords.add(word.substring(0, word.length() - 1) + "00");
    
    //da time -> 0tim
    nearWords.add('0' + word.substring(0, word.length() - 1));
    
    //dd time -> time00
    nearWords.add(word + "00");
    
    return nearWords;
  }
  
  public static void checkSimilarity(String nearWord, String inputWord){
    double levenDist = (double)LevenshteinDistance.unlimitedCompare(nearWord,inputWord)/(Math.max(nearWord.length(), inputWord.length()));
    levenDist = levenDist/6;
    double sorenDist =  1 - sorensenDice.similarity(nearWord,inputWord);

    double bobbleDist = 1/((1 + levenDist) * (1 + sorenDist)); //(1 + levenDist) *
    System.out.println("\n Near Word " + nearWord);
    System.out.print("sorenDist "+ sorenDist);
    System.out.print(" levenDist "+ levenDist);
    System.out.print(" bobbleDist " +bobbleDist+"\n");
    
    if( bobbleDist > STICKER_COEFFICIENT){
      System.out.println("                Matched   "); 
    }else{
      System.out.println("                                Un-Matched   "); 
    }

  }
  
  @SuppressWarnings("resource")
  public static void main(String[] args){


    StickerRecommenderV2 stickerRecommenderV2_1 = new StickerRecommenderV2();
    stickerRecommenderV2_1.setup("src/main/resources/SeedData1.csv");

    int i = 0;
    while(i < 2){
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("Enter Word: ");
        String inputWord = reader.nextLine().toLowerCase();
        List<String> nearWords = TestDistance.oneLevenDistWords(inputWord);
 
        for(String nearWord : nearWords){
          TestDistance.checkSimilarity(nearWord, inputWord);
        }
        System.out.println("\n\n\n");
    }

  }
  
}
