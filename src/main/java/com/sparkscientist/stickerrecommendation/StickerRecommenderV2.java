package com.sparkscientist.stickerrecommendation;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sparkscientist.beans.StickerData;
import com.sparkscientist.beans.StickerNScore;
import com.sparkscientist.beans.WordNCount;
import com.sparkscientist.utils.LevenshteinDistance;
import com.sparkscientist.utils.MapUtil;

import info.debatty.java.stringsimilarity.SorensenDice;

/**
 * 
 * Term 'sticker' is used interchangeably with 'image.'
 * 
 * @author Anurag 
 * @date Apr 10, 2017
 */
public class StickerRecommenderV2 {


    /**
     * Sample Data from Seed file:
     * 829~love:2,luv:2,janu:2,jaan:2,romance:2,babu:2,kiss:2,darling:2,dil:2,pyar:2,dear:2
     *    
     * For each sticker/image text, we need word and their counts
     */
    private static Map<String,List<WordNCount>> _stickerWithWordCounts;

    /**
     * For each word, we count the number of stickers in which it is present,
     * after iterating once over stickerWithWordCounts.
     * Hence, we calculate the log of the inverse doc frequency
     */
    private static Map<String,Double> _wordWithLogInverseDocFreq = null; 

    /**
     * For each word we maintain a list and each object is a stickerId and a score.
     * Eg. love ~ sticker1:0.7, sticker7:0.25
     * This is calculated using stickerWithWordCounts and wordWithLogInverseDocFreq
     */
    private static Map<String, List<StickerNScore>> _wordWithStickerScoreListMap = null; 

    @SuppressWarnings("unused")
    private static final int LEVENSHTEIN_THRESHOLD = 2;
    @SuppressWarnings("unused")
    private static final double SORENSEN_COEFFICIENT =  0.9;
    private static final double STICKER_COEFFICIENT =  0.74;
    private static SorensenDice sorensenDice = new SorensenDice(1);
    
    public static final String SEED_DATA_FILE_PATH = FilePathConfig.getPropertyValue("SEED_DATA_FILE_PATH");

    private static String[] WORDS_TO_BE_IGNORED = {};
   //   {"am","is","are","was","were","be","being","been","have","has","had","do","does","did","done","could","should","would","can","shall","will","may","might","must","i","we","me","us","my","mine","our","ours","you","your","yours","he","she","it","they","him","her","it","them","his","her","hers","its","their","theirs","who","whom","whose","which","what","this","these","that","those","another","each","either","much","neither","one","other","anybody","anyone","anything","everybody","everyone","everything","nobody","no one","nothing","somebody","someone","something","both","few","many","others","several","all","any","more","most","none","some","hai","hain","tu","mai","main","mein","kya","kidar","kidhar","kaisa","kaise","kyu","kahe","kyun","kahan","tha","thi","rha","rhi","na","haa","haan","nhi","teri","meri","tera","mera","sabka","sabki","to","so","vo","hum","acha","kisi","ki","ka","koi"
   // };

    private static Set<String> SET_OF_WORDS_TO_BE_IGNORED = new HashSet<String>(Arrays.asList(WORDS_TO_BE_IGNORED));
    /**
     * Given a word, find the words similar to the given word,
     * using a combined function of Levenshtein and Sorensen distance.
     *
     * @param word
     * @param wordWithStickerScoreListMap
     * @return list of similar words for a given word
     */
    public Set<String> _findSimilarWordsForAWord(String word, Map<String,
            List<StickerNScore>> wordWithStickerScoreListMap){
        Set<String> similarWords = new HashSet<String>();
        for(String similarWord : wordWithStickerScoreListMap.keySet()){
            //System.out.print("word");
            double levenDist = (double)LevenshteinDistance.unlimitedCompare(similarWord,word)/(Math.max(similarWord.length(), word.length()));
            double sorenDist =  1 - sorensenDice.similarity(similarWord,word);
            levenDist = levenDist/7;
            double stickerDist = 1/((1 + levenDist) * (1 + sorenDist)); //(1 + levenDist) *
            if( stickerDist > STICKER_COEFFICIENT && word.charAt(0) == similarWord.charAt(0) ){
                similarWords.add(similarWord);
            }
        }
        //Not adding the current word explicitly as it will be added in above condition itself
        return similarWords;
    }

    /**
     *  Given a list of similar words like hell, helo, hello etc,
     *  give scores to the stickers for each similar word.
     *  If two or more words give score to the same sticker,
     *  take max of the scores.
     *
     * @param similarWords
     * @return map of stickerIds with their score respectively
     */
    //TODO Parallelise this task
    private  Map<String, Double> _findMaxScoresOverSimilarWordsForAWord(
            List<String> similarWords, String inputWord){

        Map<String, Double> maxScoreMap = new HashMap<String, Double>();
        if(!similarWords.isEmpty()){
            for(String similarWord:similarWords){

                double levenDist = (double)LevenshteinDistance.unlimitedCompare(similarWord,inputWord)/(Math.max(similarWord.length(), inputWord.length()));
                double sorenDist =  1 - sorensenDice.similarity(similarWord,inputWord);
                double stickerDist = 1/((1 + levenDist) * (1 + sorenDist));
                //TODO now multiplying score with sticker distance
                List<StickerNScore> list = _wordWithStickerScoreListMap.get(similarWord);
                for(StickerNScore stickerNScore : list){
                    String stickerId = stickerNScore.getStickerId();
                    if(maxScoreMap.containsKey(stickerId)){
                        Double oldScore = maxScoreMap.get(stickerId);
                        Double newScore = stickerDist * stickerNScore.getScore();
                        //Removing lower score and adding bigger score
                        if(newScore.compareTo(oldScore) >= 1){
                            maxScoreMap.remove(stickerId);
                            maxScoreMap.put(stickerId, newScore);
                        }

                    }else{
                        maxScoreMap.put(stickerId, stickerDist * stickerNScore.getScore());
                    }
                }
            }
        }

        return maxScoreMap;
    }

    /**
     * Suppose the input text contains two words i.e " hello dear "
     * 1. Assuming that we have already found the similar words of hello and dear,
     * using above methods.
     * 2. Assuming that we have got 2 maps of max scores over similar words
     * of hello and dear respectively, using above methods.
     * Call them, M1 and M2.
     *
     * In this method, we take M1, M2 etc, and
     * sum the scores for each sticker.
     * For eg: M1 -> S1:0.3, S2:0.25
     * and M2 -> S1:0.15, S3:0.20,
     *
     * this method returns M3 -> S1:0.45, S2:0.25, S3:0.20
     *
     * @param listOfMapOfMaxScoresOverSimilarWordsForAWord
     * @return final map of stickerIds with their score
     */
    //TODO Parallelise this task
    Map<String, Double> _findSumOverMaxScoresOfSimilarWordsForGivenWords(
            List<Map<String, Double>> listOfMapOfMaxScoresOverSimilarWordsForAWord){
        Map<String, Double> finalMaxScoreMap = new HashMap<String, Double>();
        for(Map<String, Double> maxScoreMap : listOfMapOfMaxScoresOverSimilarWordsForAWord){
            for(String stickerId : maxScoreMap.keySet()){
                if(finalMaxScoreMap.containsKey(stickerId)){
                    Double newScore = finalMaxScoreMap.get(stickerId) + maxScoreMap.get(stickerId);
                    finalMaxScoreMap.put(stickerId,newScore);
                }else{
                    finalMaxScoreMap.put(stickerId,maxScoreMap.get(stickerId));
                }
            }
        }
        return finalMaxScoreMap;
    }

    /**
     *
     * Steps involved :
     *
     * 1. Remove special characters from input
     * 2. Tokenize input text on whitespace
     *
     * Assuming that the input text is "hello dear"
     *
     * 3. We find the similar words of hello and dear.
     *
     * 4. We compute 2 maps of max scores over similar words
     * of hello and dear respectively.
     * Call them, M1 and M2.
     * For eg: M1 -> S1:0.3, S2:0.25
     * and M2 -> S1:0.15, S3:0.20,
     *
     * 5. We take M1, M2 etc, and
     * sum the scores for each sticker.
     *
     * 6. Sort the stickers
     *
     * @param inputText
     * @return list of recommended sticker/image ids 
     */
    //TODO Parallelise this task
    public List<String> getRecommendedStickers(String inputText) {
        
        inputText = inputText.toLowerCase();
        String cleanedInput = this._removeSpecialCharacters(inputText);
        List<String> tokens = this._tokenizeText(cleanedInput);
        List<Map<String, Double>> listOfMaxScoresOverSimilarWordsForAWord = new ArrayList<Map<String, Double>>();
        Boolean atLeastOneTrainingWordFoundInput = false;
        for(String token: tokens){
            if(token.length() >= 2) {
                Set<String> similarWords = _findSimilarWordsForAWord(token, _wordWithStickerScoreListMap);
                for(String similarWord : similarWords) {
                    if (_wordWithLogInverseDocFreq.containsKey(similarWord)) {
                        atLeastOneTrainingWordFoundInput = true;
                    }
                }
            }
        }
        List<String> sortedFinalStickerList = new ArrayList<String>();
        if(atLeastOneTrainingWordFoundInput) {
            for (String token : tokens) {
                if (token.length() >= 2) {
                    Set<String> similarWords = _findSimilarWordsForAWord(token, _wordWithStickerScoreListMap);
                    //System.out.println(" Similar words for " + token + " => " + similarWords);
                    Map<String, Double> maxScoresOverSimilarWordsForAWord = _findMaxScoresOverSimilarWordsForAWord(new ArrayList<String>(similarWords), token);
                    //System.out.println(" max score map " + maxScoresOverSimilarWordsForAWord.toString());
                    listOfMaxScoresOverSimilarWordsForAWord.add(maxScoresOverSimilarWordsForAWord);
                }
            }
            Map<String, Double> finalScoreSMap = _findSumOverMaxScoresOfSimilarWordsForGivenWords(listOfMaxScoresOverSimilarWordsForAWord);
            sortedFinalStickerList = _getTopStickersBasedOnScore(finalScoreSMap);
        }
            return sortedFinalStickerList;
    }

    /**
     * Hepler function
     * @author Anurag
     * @Time -  Apr 14, 2017 - 5:12:51 PM
     * @param inputText
     * @return sticker/image ids with the tags
     */
    public List<StickerData> getRecommendedStickerIdsWithText(String inputText) {
      List<String> stickerIds = getRecommendedStickers(inputText);
      List<StickerData> stickerIdsWithText = new ArrayList<>();
      for(String stickerId : stickerIds){
        List<WordNCount> wordNCounts = _stickerWithWordCounts.get(stickerId);
        StringBuilder sb = new StringBuilder();
        for(WordNCount wordNCount : wordNCounts){
          sb.append(wordNCount.getWord() );
          sb.append( " ");
        }
        stickerIdsWithText.add(new StickerData(stickerId,sb.toString()));
      }
      return stickerIdsWithText;
    }
    
    /**
     * List of sticker ids which are sorted based on their score,
     * provided in the form of a map
     *
     * @param stickerScoreMap
     * @return sorted list of sticker ids
     */
    private List<String> _getTopStickersBasedOnScore(Map<String,Double> stickerScoreMap){
      
      //Adding a random value between 0 to 0.01 to score
      for(String key : stickerScoreMap.keySet()){
        stickerScoreMap.put(key, stickerScoreMap.get(key) + Math.random()/100);
      }
        Map<String,Double> sortedStickerScoreMap = MapUtil.sortByValue(stickerScoreMap);
        List<String> sortedStickerIds = new ArrayList<String>();
        for(Map.Entry<String, Double> entry : sortedStickerScoreMap.entrySet()){
            sortedStickerIds.add(entry.getKey());
        }
        return sortedStickerIds;
    }

    /**
     * Replace special characters from input string with 'space'
     * @param input
     * @return cleaned text after replacing special characters with 'space'
     */
    private String _removeSpecialCharacters(String input){
        String cleanedInput = null;
        if(input != null && !input.isEmpty()){
            cleanedInput = input.replaceAll("[^\\w\\s]", " ");
        }
        return cleanedInput;
    }

    /**
     * Tokenise input on space character
     * @param cleanedInput
     * @return list of tokens obtained after splitting on 'space'
     */
    private List<String> _tokenizeText(String cleanedInput){
        List<String> tokens = new ArrayList<String>();
        if(cleanedInput !=null){
            tokens = Arrays.asList(cleanedInput.split(" "));
        }
        return tokens;
    }
    
    /**
     * Process seed file,
     * compute and store,
     * required probabilities
     * 
     * @author Anurag
     * @Time -  Apr 14, 2017 - 4:55:21 PM
     * @param filePath
     */
    public void setup(String filePath){
      _makeStickerWithWordCountsFromFile(filePath);
      _makeWordWithLogInverseDocFreqMap();
      _makeWordWithStickerScoreListMap();
    }
    
    
    
    public static void main(String[] args){


        StickerRecommenderV2 stickerRecommenderV2_1 = new StickerRecommenderV2();
        // Structure of SeedData1.csv
        //829~love:2,luv:2,janu:2,jaan:2,romance:2,babu:2,kiss:2,darling:2,dil:2,pyar:2,dear:2
        
        stickerRecommenderV2_1.setup(SEED_DATA_FILE_PATH);
        stickerRecommenderV2_1.makeNewerSeedDataFile();
    }
    

    /*************************************** DATA PROCESSING WORK ********************************************/
    /**
     * It comprises of 3 steps.
     * 
     * 1. Using seed file, we load the mapping,
     * from stickers to tags, into map.
     * 
     * 2. For each tag present in the seed file,
     * compute its weight as negative logarithm,
     * of inverse count of images( it is present in).   
     * 
     * 3. We create a mapping from each tag to stickers (in which it is present).
     * We also compute and store probabilistic score along with sticker id. 
     *  
     * 
     */
    
    /**
     * Step 1 :
     * 
     * Load the seed file, which contains
     * the mapping between the image/sticker ids,
     * and their relevant tags, 
     * into a map. 
     * 
     * @author Anurag
     * @Time -  Apr 10, 2017 - 12:19:12 AM
     * @param stickerWordsCountFilePath
     * @return
     */
    public boolean _makeStickerWithWordCountsFromFile(String stickerWordsCountFilePath){

        //Map<String, Map<String, Integer>> stickerWordCounts = new HashMap<String, Map<String, Integer>>();
         _stickerWithWordCounts =  new HashMap<String,List<WordNCount>>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(stickerWordsCountFilePath));
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                String[] keyValue =  sCurrentLine.split("~");
                String stickerKey = keyValue[0];
                List<WordNCount> wordNCountList = _getWordCountMapForSticker(keyValue[1]);
                _stickerWithWordCounts.put(stickerKey,wordNCountList);
            }
            br.close();
            return true;
        } catch(IOException e) {
            //TODO logging exception
            e.printStackTrace();
            return false;
        }

    }
    
    
    /**
     * Step 2 
     * 
     * Taking probabilities (in the range 0 to 1) 
     * & converting into negative logarithms,
     * and finally normalize. 
     * 
     * @author Anurag
     * @Time -  Apr 10, 2017 - 12:17:35 AM
     */
    private void _makeWordWithLogInverseDocFreqMap(){
        if(!_stickerWithWordCounts.isEmpty()){
             _wordWithLogInverseDocFreq = new TreeMap<String,Double>();
            Integer totalStickers = _stickerWithWordCounts.size();
            for(String stickerId :_stickerWithWordCounts.keySet()){
                List<WordNCount> wordNCounts = _stickerWithWordCounts.get(stickerId);
                for(WordNCount wordNCount : wordNCounts){
                    String word = wordNCount.getWord();
                    if(!_wordWithLogInverseDocFreq.containsKey(word)){
                        _wordWithLogInverseDocFreq.put(word,0.0);
                    }

                    double newCount = _wordWithLogInverseDocFreq.get(word) + 1;
                    _wordWithLogInverseDocFreq.put(word, newCount);

                }
            }
            // now dividing by total sticker count and taking log
            for(String word :_wordWithLogInverseDocFreq.keySet()){
                //TODO replaced numerator and denominator
                double logInvFreq = -1 * Math.log( _wordWithLogInverseDocFreq.get(word)/totalStickers );
                //System.out.println(" word "+ word +  "  and idf "+logInvFreq);
                _wordWithLogInverseDocFreq.put(word,logInvFreq);
            }

        }

    }

    /**
     * Step 3
     * 
     * We create a mapping from each tag to stickers (in which it is present).
     * We also compute and store probabilistic score along with sticker id. 
     * @author Anurag
     * @Time -  Apr 10, 2017 - 12:16:23 AM
     */
    private void _makeWordWithStickerScoreListMap(){

        if(!_stickerWithWordCounts.isEmpty() && !_wordWithLogInverseDocFreq.isEmpty()){
             _wordWithStickerScoreListMap = new TreeMap<String, List<StickerNScore>>();
            for(String stickerId :_stickerWithWordCounts.keySet()){

                List<WordNCount> wordNCounts = _stickerWithWordCounts.get(stickerId);
                @SuppressWarnings("unused")
                int totalWordsInSticker = 0;
                for(WordNCount wordNCount : wordNCounts){
                    totalWordsInSticker += wordNCount.getCount();
                }

                for(WordNCount wordNCount : wordNCounts){
                    String word = wordNCount.getWord();
                    
                    double termFreq = (double)wordNCount.getCount();//TODO/totalWordsInSticker;

                    if(_wordWithLogInverseDocFreq.containsKey(word)) {
                        //TODO now taking square of log of idf
                        Double score = termFreq * Math.pow(_wordWithLogInverseDocFreq.get(word), 1);
                        //System.out.println("stickerId "+ stickerId + ", score" + score);
                        if (_wordWithStickerScoreListMap.containsKey(word)) {
                          
                          
                            _wordWithStickerScoreListMap.get(word).add(new StickerNScore(stickerId, score));
                        } else {
                          if(word.equals("party")){
                            System.out.println(" " + word);
                          }
                            List<StickerNScore> list = new ArrayList<StickerNScore>();
                            list.add(new StickerNScore(stickerId, score));
                            _wordWithStickerScoreListMap.put(word, list);
                        }
                    }
                }
            }
        }

    }
    
    /**
     * Helper function
     * @author Anurag
     * @Time -  Apr 14, 2017 - 4:52:42 PM
     * @param line
     * @return List of WordNCounts for each sticker/image
     */
    private static List<WordNCount> _getWordCountMapForSticker(String line){
       // Map<String,Integer> wordCountMap = new HashMap<String,Integer>();
        List<WordNCount> wordNCountList = new ArrayList<WordNCount>();
        if(line !=null && !line.isEmpty()){
            String[] keyValuePairs = line.split(",");
            for(String keyValue : keyValuePairs){
                if(!keyValue.isEmpty()) {
                    String[] splitArray = keyValue.split(":");
                    if(!SET_OF_WORDS_TO_BE_IGNORED.contains(splitArray[0].toLowerCase()) && splitArray[0].length() >=2 ) {
                        wordNCountList.add(new WordNCount(splitArray[0], Integer.valueOf(splitArray[1])));
                    }
                }
            }
        }
        return wordNCountList;
    }

    /**
     * Helper Function :
     * 
     * Creating a single seed file after data processing,
     * which can be used for deployment
     * @author Anurag
     * @Time -  Apr 10, 2017 - 12:20:15 AM
     */
    @Deprecated
    private void makeNewerSeedDataFile(){
      String filePath = "src/main/resources/WordsToIdfToStickersSeedData_10.csv";
      File file = new File(filePath);
      try {
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        
        for(String word : _wordWithLogInverseDocFreq.keySet()){
          Double idf = _wordWithLogInverseDocFreq.get(word);
          writer.write(word+"~"+idf+"~");
          boolean firstStickerCrossed = false;
          if(word.equals("party")){
            System.out.println(" " + word + " "+ _wordWithStickerScoreListMap.get(word).size());
          }
          for(StickerNScore stickerNScore : _wordWithStickerScoreListMap.get(word)){
            if(firstStickerCrossed){
              writer.write(",");
            }
            writer.write(stickerNScore.getStickerId()+":"+stickerNScore.getScore());
            firstStickerCrossed =true;
          }
          writer.write("\n");
        }
        writer.flush();
        writer.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
}
