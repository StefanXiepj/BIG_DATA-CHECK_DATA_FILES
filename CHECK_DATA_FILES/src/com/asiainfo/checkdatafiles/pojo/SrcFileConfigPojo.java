package com.asiainfo.checkdatafiles.pojo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSONArray;
import com.google.gson.JsonSyntaxException;

public class SrcFileConfigPojo {
	
	private FilePojo[] files;


	private static SrcFileConfigPojo _instance;

    static {
            FileInputStream configIn = null;
            try {
                configIn = new FileInputStream("conf\\src_file_config2.json");
                byte[] buf = new byte[1024];
                String strConfig = "";
                int length = 0;
                while((length = configIn.read(buf)) != -1){
                	strConfig += new String(buf,0,length);
                	
                }
                
                List<FilePojo> parseArray = JSONArray.parseArray(strConfig, FilePojo.class);
                for (Iterator<FilePojo> iterator = parseArray.iterator(); iterator.hasNext();) {
					FilePojo filePojo = (FilePojo) iterator.next();
					System.out.println(filePojo.getInterfaceName());
				}
                //_instance = gson.fromJson(IOUtils.toString(configIn, new InputStreamReader(configIn).getEncoding()), SrcFileConfigPojo.class);
            } catch (JsonSyntaxException e) {
                    e.printStackTrace();
            } catch (IOException e) {
                    e.printStackTrace();
            } finally {
                    IOUtils.closeQuietly(configIn);
            }
    }

    
    
public static SrcFileConfigPojo get_instance() {
		return _instance;
	}



public static void main(String[] args) {
	FilePojo[] instance = SrcFileConfigPojo.get_instance().getFiles();
	System.out.println(instance.length);
	for (int i = 0; i < instance.length; i++) {
		System.out.println(instance[i].getInterfaceName());
		
	}
	
}



public FilePojo[] getFiles() {
	return files;
}



public void setFiles(FilePojo[] files) {
	this.files = files;
}

}
