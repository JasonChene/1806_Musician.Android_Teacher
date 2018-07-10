package com.example.macbookpro.musictrainerteacher.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LocalStorage extends AppCompatActivity {
    private final static String FILENAME = "data";
    private final static String FILERECIVEDATAINFO = "reciveInfo";

   public static void  saveObject(Context context, String key, JSONObject data){
       SharedPreferences SP = context.getSharedPreferences(FILENAME, Context.MODE_MULTI_PROCESS);
       SharedPreferences.Editor editor = SP.edit();
       String dataInfo = data.toString();
       editor.putString(key, dataInfo);
       editor.commit();
   }
    public static void  saveString(Context context, String key, String data){
        SharedPreferences SP = context.getSharedPreferences(FILENAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = SP.edit();
        String dataInfo = data.toString();
        editor.putString(key, dataInfo);
        editor.commit();
    }

    public  static void removeKey(Context context, String key){
        SharedPreferences SP = context.getSharedPreferences(FILENAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = SP.edit();
        editor.remove(key);
        editor.apply();
    }


   public static JSONObject getObject(Context context, String key){
       SharedPreferences SP = context.getSharedPreferences(FILENAME, Context.MODE_MULTI_PROCESS);
       String result = SP.getString(key, "");
       JSONObject data;
       try{
           data =  new JSONObject(result);

       }catch (JSONException e){
           data  =  new JSONObject();
       }
       return data;
   }


    public static String getString(Context context, String key){
        SharedPreferences SP = context.getSharedPreferences(FILENAME, Context.MODE_MULTI_PROCESS);
        String result = SP.getString(key, "");
        return result;
    }

    public static void saveByte(Context context, String key, String data)
    {
        SharedPreferences SP = context.getSharedPreferences(FILENAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = SP.edit();
        String dataInfo = data;
        editor.putString(key, dataInfo);
        editor.commit();
    }

    public static String getByteString(Context context, String key){
        SharedPreferences SP = context.getSharedPreferences(FILENAME, Context.MODE_MULTI_PROCESS);
        String result = SP.getString(key, "");
        return result;
    }

    public static void saveStringEnalbleArray(Context context, String[] strArray, String key) {
        SharedPreferences prefs = context.getSharedPreferences(FILENAME, Context.MODE_MULTI_PROCESS);
        JSONArray jsonArray = new JSONArray();
        for (String b : strArray) {
            jsonArray.put(b);
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,jsonArray.toString());
        editor.commit();
    }
    public static String[] getStringEnableArray(Context context, String key)
    {
        SharedPreferences prefs = context.getSharedPreferences(FILENAME, Context.MODE_MULTI_PROCESS);
        String[] resArray = new String[0];
        Arrays.fill(resArray, true);
        try {
            JSONArray jsonArray = new JSONArray(prefs.getString(key, "[]"));
            resArray=new String[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++) {
                resArray[i] = jsonArray.getString(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resArray;
    }
    public static String getUserXtm(Context context){
        String yhxtm = null;
        JSONObject user =  getObject(context, "User");
        try {
            yhxtm =  user.getString("yhxtm");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return yhxtm;
    }
}

