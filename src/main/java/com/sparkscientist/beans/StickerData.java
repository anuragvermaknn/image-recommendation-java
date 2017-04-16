/**
 * @author Anurag
 * @description 
 */
package com.sparkscientist.beans;

/**
 * @author Anurag
 * @description 
 */
public class StickerData {

  private String stickerId;
  private String stickerText;
  private String url;
  
  /**
   * @param stickerId
   * @param stickerText
   */
  public StickerData(String stickerId, String stickerText) {
    super();
    this.stickerId = stickerId;
    this.stickerText = stickerText;
  }
  /**
   * @author Anurag
   * @return the stickerId
   */
  public String getStickerId() {
    return stickerId;
  }
  /**
   * @author Anurag
   * @param stickerId the stickerId to set
   */
  public void setStickerId(String stickerId) {
    this.stickerId = stickerId;
  }
  /**
   * @author Anurag
   * @return the stickerText
   */
  public String getStickerText() {
    return stickerText;
  }
  /**
   * @author Anurag
   * @param stickerText the stickerText to set
   */
  public void setStickerText(String stickerText) {
    this.stickerText = stickerText;
  }
  /**
   * @author Anurag
   * @return the url
   */
  public String getUrl() {
    return url;
  }
  /**
   * @author Anurag
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "StickerData [stickerId=" + stickerId + ", stickerText=" + stickerText + ", url=" + url
        + "]";
  }

  
  
}
