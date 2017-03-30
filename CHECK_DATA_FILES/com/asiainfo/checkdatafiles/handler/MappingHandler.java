/*   1:    */package com.asiainfo.checkdatafiles.handler;
/*   2:    */
/*   3:    */import java.io.BufferedInputStream;
/*   4:    */import java.io.FileInputStream;
/*   5:    */import java.io.InputStream;
/*   6:    */import java.util.HashMap;
/*   7:    */import java.util.Iterator;
/*   8:    */import java.util.LinkedHashMap;
/*   9:    */import java.util.Map;
/*  10:    */import java.util.Properties;
/*  11:    */import java.util.Set;
/*  12:    */import org.apache.log4j.Logger;
/*  13:    */
/*  16:    */public class MappingHandler
/*  17:    */{
/*  18: 18 */  Logger logger = Logger.getLogger(CheckNumberHandler.class);
/*  19:    */  
/*  20: 20 */  static HashMap<String, String> fieldMapping = new LinkedHashMap();
/*  21: 21 */  static HashMap<String, BaseHandler> handlerMapping = new LinkedHashMap();
/*  22:    */  
/*  24: 24 */  Map<String, BaseHandler> handlerMap = new LinkedHashMap();
/*  25:    */  
/*  26:    */  static {
/*  27: 27 */    Properties prop = new Properties();
/*  28:    */    
/*  29:    */    try
/*  30:    */    {
/*  31: 31 */      InputStream in = new BufferedInputStream(
/*  32: 32 */        new FileInputStream("D:\\workspaces\\CHECK_DATA_FILES\\conf\\field_mapping.properties"));
/*  33:    */      
/*  34: 34 */      prop.load(in);
/*  35: 35 */      Iterator<String> it = prop.stringPropertyNames().iterator();
/*  36: 36 */      HashMap<String, String> properties = new HashMap();
/*  37:    */      
/*  38: 38 */      while (it.hasNext()) {
/*  39: 39 */        String key = (String)it.next();
/*  40: 40 */        properties.put(key, prop.getProperty(key));
/*  41:    */      }
/*  42: 42 */      fieldMapping = properties;
/*  43: 43 */      in.close();
/*  44:    */    }
/*  45:    */    catch (Exception e) {
/*  46: 46 */      e.printStackTrace();
/*  47:    */    }
/*  48:    */  }
/*  49:    */  
/*  50:    */  public static HashMap<String, BaseHandler> handlerFactory()
/*  51:    */  {
/*  52:    */    try {
/*  53: 53 */      String handlerNames = (String)fieldMapping.get("HANDLER2");
/*  54: 54 */      String[] handlerList = handlerNames.split(",");
/*  55: 55 */      for (int i = 0; i < handlerList.length; i++) {
/*  56: 56 */        BaseHandler hander = (BaseHandler)Class.forName("com.asiainfo.checkdatafiles.handler." + handlerList[i]).newInstance();
/*  57: 57 */        handlerMapping.put(handlerList[i], hander);
/*  58:    */      }
/*  59:    */      
/*  60: 60 */      return handlerMapping;
/*  61:    */    } catch (InstantiationException e) {
/*  62: 62 */      e.printStackTrace();
/*  63:    */    } catch (IllegalAccessException e) {
/*  64: 64 */      e.printStackTrace();
/*  65:    */    } catch (ClassNotFoundException e) {
/*  66: 66 */      e.printStackTrace();
/*  67:    */    }
/*  68:    */    
/*  69: 69 */    return null;
/*  70:    */  }
/*  71:    */  
/*  74:    */  public BaseHandler createValidateQueue(String[] fields)
/*  75:    */  {
/*  76: 76 */    String functionName = null;
/*  77: 77 */    String funParameter = null;
/*  78: 78 */    String key = null;
/*  79: 79 */    String value = null;
/*  80: 80 */    String[] splitValues = null;
/*  81: 81 */    BaseHandler handler = null;
/*  82:    */    
/*  83:    */    try
/*  84:    */    {
/*  85: 85 */      for (int i = 0; i < fields.length; i++)
/*  86:    */      {
/*  87: 87 */        key = fields[i];
/*  88: 88 */        value = (String)fieldMapping.get(key);
/*  89:    */        
/*  91: 91 */        if (value == null) {
/*  92: 92 */          value = (String)fieldMapping.get("DEFAULT");
/*  93:    */        }
/*  94:    */        
/*  96: 96 */        splitValues = value.split(",");
/*  97: 97 */        functionName = splitValues[0];
/*  98: 98 */        funParameter = splitValues[1];
/*  99:    */        
/* 101:101 */        handler = (BaseHandler)Class.forName(functionName).newInstance();
/* 102:102 */        handler.setParameter(funParameter);
/* 103:103 */        handler.setHanderName(key);
/* 104:104 */        this.handlerMap.put(fields[i], handler);
/* 105:    */      }
/* 106:    */      
/* 108:108 */      for (int i = 1; i < fields.length; i++) {
/* 109:109 */        ((BaseHandler)this.handlerMap.get(fields[(i - 1)])).setSuccessor((BaseHandler)this.handlerMap.get(fields[i]));
/* 110:    */      }
/* 111:    */      
/* 112:112 */      return (BaseHandler)this.handlerMap.get(fields[0]);
/* 113:    */    }
/* 114:    */    catch (InstantiationException e) {
/* 115:115 */      e.printStackTrace();
/* 116:116 */      this.logger.error("InstantiationException");
/* 117:    */    } catch (IllegalAccessException e) {
/* 118:118 */      e.printStackTrace();
/* 119:119 */      this.logger.error("IllegalAccessException");
/* 120:    */    } catch (ClassNotFoundException e) {
/* 121:121 */      e.printStackTrace();
/* 122:122 */      this.logger.error("ClassNotFoundException");
/* 123:    */    }
/* 124:124 */    return null;
/* 125:    */  }
/* 126:    */}


/* Location:           D:\git\CHECK_DATA_FILES\bin\
 * Qualified Name:     com.asiainfo.checkdatafiles.handler.MappingHandler
 * JD-Core Version:    0.7.0-SNAPSHOT-20130630
 */