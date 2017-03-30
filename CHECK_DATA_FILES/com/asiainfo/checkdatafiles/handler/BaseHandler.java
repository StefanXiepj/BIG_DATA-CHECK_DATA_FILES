package com.asiainfo.checkdatafiles.handler;

import java.util.logging.Logger;

public abstract class BaseHandler
{
  private String handerName;
  private String parameter;
  private BaseHandler successor;
  Logger logger = Logger.getLogger(getClass().getName());
  
  public String getHanderName() {
    return this.handerName;
  }
  
  public void setHanderName(String handerName) {
    this.handerName = handerName;
  }
  
  public String getParameter() {
    return this.parameter;
  }
  
  public void setParameter(String parameter) {
    this.parameter = parameter;
  }
  
  public BaseHandler getSuccessor() {
    return this.successor;
  }
  
  public void setSuccessor(BaseHandler successor) {
    this.successor = successor;
  }
  
  public abstract void handerRequest(Object paramObject);
}
