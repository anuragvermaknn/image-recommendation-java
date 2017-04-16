/**
 * @author Anurag
 * @description 
 */
package com.sparkscientist.stickerrecommendation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Anurag
 * @description 
 */
public class FilePathConfig {
 
    private static Properties prop;
    private static final String configFilePath = "config.properties"; 
    static{
        InputStream is = null;
        try {
            prop = new Properties();
            is = FilePathConfig.class.getClassLoader().getResourceAsStream(configFilePath);
            prop.load(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     
    public static String getPropertyValue(String key){
        return prop.getProperty(key);
    }
     
    public static void main(String a[]){
         
        System.out.println("SEED_DATA_FILE_PATH: "+getPropertyValue("SEED_DATA_FILE_PATH"));
        System.out.println("STICKER_AND_URL_FILE_PATH: "+getPropertyValue("STICKER_AND_URL_FILE_PATH"));
    }
}