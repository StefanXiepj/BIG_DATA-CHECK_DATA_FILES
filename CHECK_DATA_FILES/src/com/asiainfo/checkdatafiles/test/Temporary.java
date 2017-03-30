package com.asiainfo.checkdatafiles.test;

public class Temporary {

}


package com.asiabi.report.action;

import com.asiabi.base.table.PubInfoConditionTable;
import com.asiabi.base.util.CommConditionUtil;
import com.asiabi.base.util.StringTool;
import com.asiabi.common.app.AppException;
import com.asiabi.common.app.ReflectUtil;
import com.asiabi.report.domain.RptColDictTable;
import com.asiabi.report.domain.RptFilterTable;
import com.asiabi.report.domain.RptResourceTable;
import com.asiabi.report.service.ILReportService;
import com.asiabi.report.service.ILTableService;
import com.asiabi.report.service.impl.LReportServiceImpl;
import com.asiabi.report.service.impl.LTableServiceImpl;
import com.asiabi.report.struct.ReportQryStruct;
import com.asiainfo.common.LoadProperties;
import com.asiainfo.db.SQLCommand;
import com.asiainfo.mail.action.FtpServer;
import com.asiainfo.xj.util.DBUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.net.ftp.FTPClient;

public class TermlSaleEmailData
{
  public static void main(String[] args)
  {
    String body = "aaabbbaaaddd";
    
    body = body.replaceAll("aaa", "e\n");
    System.out.println(body);
  }
  
  public String[] t(String date, String rpt_id)
    throws AppException
  {
    ILReportService rptService = new LReportServiceImpl();
    
    ILTableService tableService = new LTableServiceImpl();
    
    RptResourceTable rptTable = null;
    
    RptFilterTable[] rptFilterTables = (RptFilterTable[])null;
    
    String[] filtersValue = (String[])null;
    
    PubInfoConditionTable[] cdnTables = (PubInfoConditionTable[])null;
    
    cdnTables = CommConditionUtil.genCondition(rpt_id, 
      "0");
    
    String processHTML = "";
    
    boolean clearSession = true;
    
    boolean clearFiltersValue = false;
    
    String preview = "N";
    if ((rptTable == null) || 
      ((rptTable != null) && (!rptTable.rpt_id.equals(rpt_id))) || 
      ("Y".equals(preview)))
    {
      rptTable = null;
      rptFilterTables = (RptFilterTable[])null;
      filtersValue = (String[])null;
      
      clearSession = true;
    }
    if (clearSession) {
      rptTable = (RptResourceTable)rptService.getReport(rpt_id);
      List listRptFilter = rptService.getReportFilter(rpt_id);
      if ((listRptFilter != null) && (listRptFilter.size() >= 0)) {
        rptFilterTables = 
          (RptFilterTable[])listRptFilter.toArray(new RptFilterTable[listRptFilter.size()]);
      }
    }
    
    ReportQryStruct qryStruct = new ReportQryStruct();
    
    String p_condition = "N";
    
    if ("Y".equals(preview)) {
      rptTable.preview_data = "Y";
    }
    else {
      rptTable.preview_data = "N";
    }
    rptTable.data_where_sql = (" " + rptTable.data_where);
    
    qryStruct.date_s = date;
    qryStruct.date_e = date;
    
    if (StringTool.checkEmptyString(qryStruct.divcity_flag)) {
      qryStruct.divcity_flag = rptTable.divcity_flag;
    }
    rptTable.divcity_flag = qryStruct.divcity_flag;
    
    if (StringTool.checkEmptyString(qryStruct.row_flag)) {
      qryStruct.row_flag = rptTable.row_flag;
    }
    
    String expandcol = "";
    if (!StringTool.checkEmptyString(expandcol)) {
      qryStruct.expandcol = expandcol;
    }
    else if ((!StringTool.checkEmptyString(rptTable.startcol)) && 
      (!"N".equals(rptTable.startcol))) {
      qryStruct.expandcol = rptTable.startcol;
    }
    
    qryStruct.svc_knd = "";
    
    if (rptTable.data_where_sql.toUpperCase().indexOf(" WHERE ") >= 0) {
      RptResourceTable tmp383_381 = rptTable;
      tmp383_381.data_where_sql = (tmp383_381.data_where_sql + CommConditionUtil.getRptWhere(cdnTables, qryStruct));
    } else {
      rptTable.data_where_sql = 
        ("WHERE 1=1 " + CommConditionUtil.getRptWhere(cdnTables, qryStruct));
    }
    System.out.println("rptTable.data_where=" + rptTable.data_where_sql);
    qryStruct.visible_data = "Y";
    List listRptCol = null;
    listRptCol = rptService.getReportCol(rptTable.rpt_id, 
      qryStruct.expandcol);
    
    List tmpRptCol = new ArrayList();
    boolean isReplace = false;
    for (int i = 0; i < listRptCol.size(); i++) {
      RptColDictTable dict = (RptColDictTable)listRptCol.get(i);
      if (dict.field_dim_code.toUpperCase().indexOf("DATA_INT;") >= 0) {
        System.out.println("5555555555");
        isReplace = true;
        String tmpCode = dict.field_dim_code;
        String[] code = tmpCode.split(";");
        if ((code == null) || (code.length == 0)) {
          break;
        }
        
        String value = ReflectUtil.getStringFromObj(qryStruct, 
          cdnTables[i].qry_code).trim();
        if ((value.length() == 2) && (code.length >= 2)) {
          dict.field_dim_code = (code[0] + code[1]);
        } else if ((value.length() == 4) && (code.length >= 3)) {
          dict.field_dim_code = (code[0] + code[2]);
        } else if ((value.length() == 6) && (code.length >= 4)) {
          dict.field_dim_code = (code[0] + code[3]);
        } else if ((value.length() == 8) && (code.length >= 5)) {
          dict.field_dim_code = (code[0] + code[4]);
        } else if (code.length >= 5)
        {
          dict.field_dim_code = (code[0] + "5");
        }
      }
      if (dict.field_code.toUpperCase().indexOf("DATA_CHAR;") >= 0) {
        System.out.println("66666666666");
        isReplace = true;
        String tmpCode = dict.field_code;
        String[] code = tmpCode.split(";");
        if ((code == null) || (code.length == 0)) {
          break;
        }
        
        String value = ReflectUtil.getStringFromObj(qryStruct, 
          cdnTables[i].qry_code).trim();
        if ((value.length() == 2) && (code.length >= 2)) {
          dict.field_code = (code[0] + code[1]);
        } else if ((value.length() == 4) && (code.length >= 3)) {
          dict.field_code = (code[0] + code[2]);
        } else if ((value.length() == 6) && (code.length >= 4)) {
          dict.field_code = (code[0] + code[3]);
        } else if ((value.length() == 8) && (code.length >= 5)) {
          dict.field_code = (code[0] + code[4]);
        } else if (code.length >= 5)
        {
          dict.field_code = (code[0] + "5");
        }
      }
      tmpRptCol.add(dict);
    }
    if (isReplace) {
      listRptCol = tmpRptCol;
    }
    String[] body = tableService.getReportBody(rptTable, listRptCol, 
      qryStruct, null);
    StringBuffer bodyStr = new StringBuffer();
    if ((body != null) && (body.length >= 0)) {
      for (int i = 0; i < body.length; i++) {
        bodyStr.append(body[i]);
      }
      
      String bodyString = bodyStr.toString();
      bodyString = bodyString.replaceAll("class=\"tab-title\"", "");
      bodyString = bodyString.replaceAll("title=\"\"", "");
      
      bodyString = bodyString.replaceAll("\n", " ");
      DBUtil dbUtil = new DBUtil();
      try {
        String sqlEmpty = "insert into bi_ui.ui_terml_sale_email_data(data_date,rpt_id,body) values(" + 
          date + ",'" + rpt_id + "',empty_clob()) ";
        SQLCommand.execUpdate(sqlEmpty);
        String sql1 = "select body from bi_ui.ui_terml_sale_email_data where data_date=" + 
          date + " and rpt_id = '" + rpt_id + "' for update";
        SQLCommand.updateClob(sql1, "body", bodyString);
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return body;
  }
  
  public String[] getHtmlExcel(String date, String rpt_id)
    throws AppException
  {
    ILReportService rptService = new LReportServiceImpl();
    
    ILTableService tableService = new LTableServiceImpl();
    
    RptResourceTable rptTable = null;
    
    PubInfoConditionTable[] cdnTables = (PubInfoConditionTable[])null;
    cdnTables = CommConditionUtil.genCondition(rpt_id, 
      "0");
    
    boolean clearSession = true;
    String preview = "N";
    if ((rptTable == null) || 
      ((rptTable != null) && (!rptTable.rpt_id.equals(rpt_id))) || 
      ("Y".equals(preview)))
    {
      rptTable = null;
      clearSession = true;
    }
    if (clearSession) {
      rptTable = (RptResourceTable)rptService.getReport(rpt_id);
      List<?> listRptFilter = rptService.getReportFilter(rpt_id);
      if (listRptFilter != null) { listRptFilter.size();
      }
    }
    
    ReportQryStruct qryStruct = new ReportQryStruct();
    
    if ("Y".equals(preview)) {
      rptTable.preview_data = "Y";
    }
    else {
      rptTable.preview_data = "N";
    }
    rptTable.data_where_sql = (" " + rptTable.data_where);
    qryStruct.date_s = date;
    qryStruct.date_e = date;
    
    if (StringTool.checkEmptyString(qryStruct.divcity_flag)) {
      qryStruct.divcity_flag = rptTable.divcity_flag;
    }
    rptTable.divcity_flag = qryStruct.divcity_flag;
    
    if (StringTool.checkEmptyString(qryStruct.row_flag)) {
      qryStruct.row_flag = rptTable.row_flag;
    }
    
    String expandcol = "";
    if (!StringTool.checkEmptyString(expandcol)) {
      qryStruct.expandcol = expandcol;
    }
    else if ((!StringTool.checkEmptyString(rptTable.startcol)) && 
      (!"N".equals(rptTable.startcol))) {
      qryStruct.expandcol = rptTable.startcol;
    }
    
    qryStruct.svc_knd = "";
    
    if (rptTable.data_where_sql.toUpperCase().indexOf(" WHERE ") >= 0) {
      RptResourceTable tmp324_322 = rptTable;
      tmp324_322.data_where_sql = (tmp324_322.data_where_sql + CommConditionUtil.getRptWhere(cdnTables, qryStruct));
    } else {
      rptTable.data_where_sql = 
        ("WHERE 1=1 " + CommConditionUtil.getRptWhere(cdnTables, qryStruct));
    }
    qryStruct.visible_data = "Y";
    List<RptColDictTable> listRptCol = null;
    listRptCol = rptService.getReportCol(rptTable.rpt_id, 
      qryStruct.expandcol);
    
    List<RptColDictTable> tmpRptCol = new ArrayList();
    boolean isReplace = false;
    for (int i = 0; i < listRptCol.size(); i++) {
      RptColDictTable dict = (RptColDictTable)listRptCol.get(i);
      if (dict.field_dim_code.toUpperCase().indexOf("DATA_INT;") >= 0) {
        isReplace = true;
        String tmpCode = dict.field_dim_code;
        String[] code = tmpCode.split(";");
        if ((code == null) || (code.length == 0)) {
          break;
        }
        String value = ReflectUtil.getStringFromObj(qryStruct, 
          cdnTables[i].qry_code).trim();
        if ((value.length() == 2) && (code.length >= 2)) {
          dict.field_dim_code = (code[0] + code[1]);
        } else if ((value.length() == 4) && (code.length >= 3)) {
          dict.field_dim_code = (code[0] + code[2]);
        } else if ((value.length() == 6) && (code.length >= 4)) {
          dict.field_dim_code = (code[0] + code[3]);
        } else if ((value.length() == 8) && (code.length >= 5)) {
          dict.field_dim_code = (code[0] + code[4]);
        } else if (code.length >= 5)
        {
          dict.field_dim_code = (code[0] + "5");
        }
      }
      if (dict.field_code.toUpperCase().indexOf("DATA_CHAR;") >= 0) {
        isReplace = true;
        String tmpCode = dict.field_code;
        String[] code = tmpCode.split(";");
        if ((code == null) || (code.length == 0)) {
          break;
        }
        String value = ReflectUtil.getStringFromObj(qryStruct, 
          cdnTables[i].qry_code).trim();
        if ((value.length() == 2) && (code.length >= 2)) {
          dict.field_code = (code[0] + code[1]);
        } else if ((value.length() == 4) && (code.length >= 3)) {
          dict.field_code = (code[0] + code[2]);
        } else if ((value.length() == 6) && (code.length >= 4)) {
          dict.field_code = (code[0] + code[3]);
        } else if ((value.length() == 8) && (code.length >= 5)) {
          dict.field_code = (code[0] + code[4]);
        } else if (code.length >= 5)
        {
          dict.field_code = (code[0] + "5");
        }
      }
      tmpRptCol.add(dict);
    }
    if (isReplace) {
      listRptCol = tmpRptCol;
    }
    String[] body = tableService.getReportBody(rptTable, listRptCol, 
      qryStruct, null);
    StringBuffer bodyStr = new StringBuffer();
    if ((body != null) && (body.length >= 0)) {
      for (int i = 0; i < body.length; i++) {
        bodyStr.append(body[i]);
      }
      String bodyString = bodyStr.toString();
      bodyString = bodyString.replaceAll("class=\"tab-title\"", "");
      bodyString = bodyString.replaceAll("title=\"\"", "");
      bodyString = bodyString.replaceAll("\n", " ");
    }
    return body;
  }
  
  public String[] getFullStr(String rpt_id, String data_date)
    throws UnsupportedEncodingException, AppException
  {
    String fileName = rpt_id + "_" + data_date;
    String restr = "";
    
    ILReportService rptService = new LReportServiceImpl();
    
    RptResourceTable rptTable = (RptResourceTable)rptService
      .getReport(rpt_id);
    long longTime = new Date().getTime();
    if ((rptTable == null) || (!rptTable.rpt_id.equals(rpt_id))) {
      restr = restr + "<center>";
      restr = restr + "<br><br>此导出报表信息有误，可能你的操作信息丢失，请重新查询确定你需要导出的报表信息！<br>";
      restr = restr + "<input type=\"reset\" name=\"close_win\" class=\"button\" onMouseOver=\"switchClass(this)\" onMouseOut=\"switchClass(this)\" value=\"关闭\" onClick=\"javascript:window.close();\"> ";
      restr = restr + "</center>";
      return new String[] { fileName, restr };
    }
    rptTable.pagecount = "-1";
    restr = restr + "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
    restr = restr + "<HTML>";
    restr = restr + "<meta http-equiv='Content-Type' content='text/html; charset=utf8'>";
    restr = restr + "<body>";
    if ((rptTable.name != null) || (rptTable.name.trim().length() != 0))
    {
      restr = restr + "<font>" + rptTable.name + "</font><br/>";
    }
    restr = restr + "<font>统计日期：" + data_date + "</font><br/>";
    restr = restr + "<table width=\"100%\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse\" bordercolor=\"#999999\">";
    try {
      String[] body = getHtmlExcel(data_date, rpt_id);
      for (int i = 0; i < body.length; i++) {
        restr = restr + body[i];
      }
    } catch (AppException e) {
      e.printStackTrace();
    }
    
    restr = restr + "</table>";
    restr = restr + "</body>";
    restr = restr + "</HTML>";
    return new String[] { fileName, restr };
  }
  
  public void storeFile(String rpt_id, String data_date) throws Exception {
    String[] file = getFullStr(rpt_id, data_date);
    if (file[0] == null) {
      System.err.println("Exception: file not found!");
      return;
    }
    String fileName = new String(file[0].getBytes("GBK"));
    String fileStr = file[1];
    InputStream is = new ByteArrayInputStream(fileStr.getBytes("UTF8"));
    System.out.println("333");
    String ftpIp = LoadProperties.getProperty("mail_ftpIp");
    System.out.println("ftpIp:::" + ftpIp);
    String ftpPath = LoadProperties.getProperty("mail_ftpPath");
    System.out.println("ftpPath:::" + ftpPath);
    int ftpPort = Integer.parseInt(
      LoadProperties.getProperty("mail_ftpPort"));
    System.out.println("ftpPort:::" + ftpPort);
    String ftpName = LoadProperties.getProperty("mail_ftpUserName");
    System.out.println("ftpName:::" + ftpName);
    String ftpPasswd = LoadProperties.getProperty("mail_ftpUserPasswd");
    System.out.println("ftpPasswd:::" + ftpPasswd);
    FtpServer fs = new FtpServer();
    fs.connect(ftpPath, ftpIp, ftpPort, ftpName, ftpPasswd);
    System.out.println("ftp connected!");
    String newName = fileName + ".xls";
    System.out.println("newName:::" + newName);
    fs.ftp.storeFile(newName, is);
    is.close();
    System.out.println("file upload ok!");
  }
  
  public void sendMail(String rpt_id, String data_date, String email) throws Exception
  {
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String now = formatter.format(new Date());
    ILReportService rptService = new LReportServiceImpl();
    RptResourceTable rptTable = (RptResourceTable)rptService
      .getReport(rpt_id);
    String rpt_name = rptTable.name;
    
    String fileName = rpt_id + "_" + data_date;
    rpt_name = fileName;
    String sql = "insert into newaio.email_message (EMAIL_ID, TITLE, MSG, FROM_ADDRESS, SEND_DATE, HISTORY, TO_ADDRESS, CONTENT_TYPE, ATTACH_FILE_PATHS, PRIORITY, SEND_TIME, BRAND_NAME, RPT_ID, RPT_NAME, SCHEDULE_TIME, EMAIL_NAME, NAME)values (newaio.EMAIL_MESSAGE_SEQUENCE.nextval, '" + 
    
      rpt_name + 
      "', 'test', 'tybi@ty.chinatelecom.cn', '" + 
      now + 
      "', 0, '" + 
      email + 
      "', 1, '" + 
      fileName + ".xls" + 
      "', '2', '" + 
      now + 
      "', null, null, null, '" + 
      now + 
      "', '" + 
      rpt_name + 
      ".xls', null);";
    SQLCommand.execute(sql);
  }
}

