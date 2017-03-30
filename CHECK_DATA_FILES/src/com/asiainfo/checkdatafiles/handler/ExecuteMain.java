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
			// ������ݼ�
			for (int i = 0; i < filesURL.length; i++) {

				fileURL = filesURL[i];
				Map<String, Object> readFile = BaseUtil.readFile(fileURL);

				String encoding = (String) readFile.get("Encoding");
				// �ַ�����У��
				if (encoding != "GBK") {
					logger.error(fileURL + " :�ַ����벻��GBK����");
					return;
				}

				// �ļ���У��
				if (!BaseUtil.isLegalFileName(fileURL)) {
					logger.error(fileURL + " :�ļ��������Ϸ�����");

				}

				// ������ݼ�
				String[][] data = (String[][]) readFile.get("DATA");
				// ����У��
				// headerHandler.handerRequest(file);
				try {
					String firstNumber = data[0][0];
					headData = Integer.parseInt(firstNumber);
					columnCount = (Integer) readFile.get("COLUMN_COUNT");
					rowCount = (Integer)  readFile.get("ROW_COUNT");
				} catch (NumberFormatException e) {
					e.printStackTrace();
					logger.error(fileURL + " :�������ݸ�ʽ���󣡣�");
				}

				if ((columnCount - headData) != 2) {
					logger.error(fileURL + " :������������һ�£���");
				}

				// �ڶ���У��
				// �ֶ��ֵ�
				String[] fields = data[1][0].split("\\|#\\|");

				HashMap<String, BaseHandler> handlerList = MappingHandler.handlerFactory();
				HashMap<String, String> fieldMapping = MappingHandler.fieldMapping;

				// ��ǰ��
				for (int m = 2; m < rowCount; m++) {
					for (int n = 0; n < columnCount; n++) {
						// ��ü���Ա
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
