package com.example.macbookpro.musictrainerteacher.manager;

/**
 * 数据管理类
 */
public class DataManager {

    private String dataReceived;
    public DataItem dataIteml = new DataItem();


    public DataItem dataDecode(String data){

        dataReceived = data;

        int index1 = dataReceived.indexOf(",");
        int index2 = dataReceived.indexOf(",",index1 + 1);

        dataIteml.setTimeStamp(dataReceived.substring(0,10));

        dataIteml.setX(dataReceived.substring(11,index2));
        dataIteml.setY(dataReceived.substring(index2+1,dataReceived.length()-1));
        dataIteml.setStyle(dataReceived.substring(dataReceived.length()-1));

        return dataIteml;
    }

}
