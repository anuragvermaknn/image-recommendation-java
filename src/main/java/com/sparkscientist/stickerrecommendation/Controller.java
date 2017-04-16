/**
 * @author Anurag
 * @description 
 */
package com.sparkscientist.stickerrecommendation;

import static spark.Spark.*;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import com.sparkscientist.beans.StickerData;

import spark.ModelAndView;
import spark.Response;
import spark.Route;
import spark.template.velocity.VelocityTemplateEngine;
/**
 * @author Anurag
 * @description 
 */
public class Controller {

  public static Map<String, String> StickerIdsAndUrl = new HashMap<String, String>();

  private static String STICKER_AND_URL_FILE_PATH = FilePathConfig.getPropertyValue("STICKER_AND_URL_FILE_PATH");
  
  public static void loadStickerIdsAndUrl(){

    BufferedReader br = null;
    try {
        br = new BufferedReader(new FileReader(STICKER_AND_URL_FILE_PATH));
        String sCurrentLine;

        while ((sCurrentLine = br.readLine()) != null) {
            String[] keyValue =  sCurrentLine.split(",");
            StickerIdsAndUrl.put(keyValue[0], keyValue[1]);
        }
    }catch(IOException e) {
      //TODO logging exception
      e.printStackTrace();

    } 
  }
  public static void main(String[] args) {
    
    Controller.loadStickerIdsAndUrl();
    StickerRecommenderV2 stickerRecommenderV2_1 = new StickerRecommenderV2();
    stickerRecommenderV2_1.setup(StickerRecommenderV2.SEED_DATA_FILE_PATH);
    port(9090);
    get("/input",(req, res) -> { 
      Map<String, Object> model = new HashMap<>();
      return new ModelAndView(model, "test.vm");
    }, new VelocityTemplateEngine());

    
    post("/suggestion", (req, res) -> {
      //Integer suggestionNumber = Integer.parseInt(req.params(":no"));
      //System.out.println("suggestionNumber "+ suggestionNumber+ " "+ req.params(":no"));
      //String inputText = req.params(":input");
      String inputText = req.queryParams("inputText");// attribute("inputText");
      System.out.println("inputText "+ inputText);
      Map<String, Object> model = new HashMap<>();
      if(inputText!=null){
      List<StickerData> stickerDatas = stickerRecommenderV2_1.getRecommendedStickerIdsWithText(inputText);
      int j = 1;
      for(StickerData stickerData : stickerDatas){
        String stickerId = stickerData.getStickerId();
        System.out.println("Sticker Id :" + stickerId);
        stickerData.setUrl(StickerIdsAndUrl.get(stickerId));
        //imageUrls.add(StickerIdsAndUrl.get(stickerId));
        j++;
      }
      
      model.put("images",stickerDatas);
      }
      //return "No suggestion";
      return new ModelAndView(model, "suggestion.vm");
    }, new VelocityTemplateEngine());
}
  
  
 /*
        for(String stickerId : results1){
        System.out.println("Sticker Id :" + stickerId);
        if(j == suggestionNumber.intValue()){
          int a = 0;
          if(StickerIdsAndUrl.containsKey(stickerId)){
            HttpServletResponse response = res.raw();
            imageReadAndOutput(response,StickerIdsAndUrl.get(stickerId));
            return response;
          } else {
            return "Sticker url not present for Sticker id : " + stickerId;
          }
          //return  stickerId;
        }
        j++;
      }
 
  */
}
