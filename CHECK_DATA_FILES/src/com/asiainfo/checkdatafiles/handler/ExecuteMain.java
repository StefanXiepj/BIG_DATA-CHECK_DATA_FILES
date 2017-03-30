package com.asiainfo.checkdatafiles.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import com.asiainfo.checkdatafiles.util.BaseUtil;

public class ExecuteMain {

	Logger logger = Logger.getLogger(CheckNumberHandler.class);

	public void execute(String[] filesURL) {
		try {
			String fileURL;
			int headData = 0;
			int columnCount = 0;
			int rowCount = 0;
			// 获得数据集
			for (int i = 0; i < filesURL.length; i++) {

				fileURL = filesURL[i];
				Map<String, Object> readFile = BaseUtil.readFile(fileURL);

				String encoding = (String) readFile.get("Encoding");
				// 字符编码校验
				if (encoding != "GBK") {
					logger.error(fileURL + " :字符编码不是GBK！！");
					return;
				}

				// 文件名校核
				if (!BaseUtil.isLegalFileName(fileURL)) {
					logger.error(fileURL + " :文件命名不合法！！");

				}

				// 获得数据集
				String[][] data = (String[][]) readFile.get("DATA");
				// 首行校验
				// headerHandler.handerRequest(file);
				try {
					String firstNumber = data[0][0];
					headData = Integer.parseInt(firstNumber);
					columnCount = (Integer) readFile.get("COLUMN_COUNT");
					rowCount = (Integer)  readFile.get("ROW_COUNT");
				} catch (NumberFormatException e) {
					e.printStackTrace();
					logger.error(fileURL + " :首行数据格式错误！！");
				}

				if ((columnCount - headData) != 2) {
					logger.error(fileURL + " :数据总条数不一致！！");
				}

				// 第二行校验
				// 字段字典
				String[] fields = data[1][0].split("\\|#\\|");

				HashMap<String, BaseHandler> handlerList = MappingHandler.handlerFactory();
				HashMap<String, String> fieldMapping = MappingHandler.fieldMapping;

				// 当前行
				for (int m = 2; m < rowCount; m++) {
					for (int n = 0; n < columnCount; n++) {
						// 获得检验员
						String[] propValueCurrent = null;
						if(fieldMapping.get(fields[n]) != null){
							propValueCurrent = fieldMapping.get(fields[n]).split(",");
						}else{
							propValueCurrent = fieldMapping.get("DEFAULT").split(",");
						}

						BaseHandler handler = handlerList
								.get(propValueCurrent[0]);
						handler.setParameter(propValueCurrent[1]);

						handler.handerRequest(data[m][n], m);
					}
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
