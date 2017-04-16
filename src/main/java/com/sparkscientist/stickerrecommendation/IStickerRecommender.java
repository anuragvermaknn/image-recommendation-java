package com.sparkscientist.stickerrecommendation;

import java.util.List;

/**
 * @author Anurag
 * @description 
 */
public interface IStickerRecommender {

    /**
     * Using a score based on term frequency and inverse document frequency,
     * computing that score for each sticker and returning the top
     * relevant stickers
     *
     * @param inputText
     * @return list of recommended sticker ids for the given input text
     */
    public List<String> getRecommendedStickers(String inputText);

    /**
     * Load seed file,
     * build probabilities and weights,
     * store in maps
     * 
     * @author Anurag
     * @Time -  Apr 14, 2017 - 4:40:46 PM
     * @param filePath
     */
    public void setup(String filePath);
    
    public boolean reloadStickerFiles(String stickerWordsCountFilePath, String countAWordPresentInDifferentStickerFilePath);
}
