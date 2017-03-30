/*  1:   */package com.asiainfo.checkdatafiles.handler;
/*  2:   */
/*  3:   */import java.util.logging.Logger;
/*  4:   */
/* 16:   */public abstract class BaseHandler
/* 17:   */{
/* 18:   */  private String handerName;
/* 19:   */  private String parameter;
/* 20:   */  private BaseHandler successor;
/* 21:21 */  Logger logger = Logger.getLogger(getClass().getName());
/* 22:   */  
/* 23:   */  public String getHanderName() {
/* 24:24 */    return this.handerName;
/* 25:   */  }
/* 26:   */  
/* 27:   */  public void setHanderName(String handerName) {
/* 28:28 */    this.handerName = handerName;
/* 29:   */  }
/* 30:   */  
/* 31:   */  public String getParameter() {
/* 32:32 */    return this.parameter;
/* 33:   */  }
/* 34:   */  
/* 35:   */  public void setParameter(String parameter) {
/* 36:36 */    this.parameter = parameter;
/* 37:   */  }
/* 38:   */  
/* 39:   */  public BaseHandler getSuccessor() {
/* 40:40 */    return this.successor;
/* 41:   */  }
/* 42:   */  
/* 43:   */  public void setSuccessor(BaseHandler successor) {
/* 44:44 */    this.successor = successor;
/* 45:   */  }
/* 46:   */  
/* 47:   */  public abstract void handerRequest(Object paramObject);
/* 48:   */}


/* Location:           D:\git\CHECK_DATA_FILES\bin\
 * Qualified Name:     com.asiainfo.checkdatafiles.handler.BaseHandler
 * JD-Core Version:    0.7.0-SNAPSHOT-20130630
 */