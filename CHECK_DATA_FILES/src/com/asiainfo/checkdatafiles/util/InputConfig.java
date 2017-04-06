package com.asiainfo.checkdatafiles.util;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class InputConfig {
    String listen_port;
    String hdfs;

    private static InputConfig _instance;

    static {
            Gson gson = new Gson();
            FileInputStream configIn = null;
            try {
                    configIn = new FileInputStream("hdfs.conf.json");
                    _instance = gson.fromJson(IOUtils.toString(configIn), InputConfig.class);
            } catch (JsonSyntaxException e) {
                    e.printStackTrace();
            } catch (IOException e) {
                    e.printStackTrace();
            } finally {
                    IOUtils.closeQuietly(configIn);
            }
    }

    public static InputConfig getInstance() {
            return _instance;
    }
}