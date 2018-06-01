package com.ssig.smartcap.mobile.utils;

import android.os.Environment;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Created by flabe on 30/5/2018.
 */

public class ParserJSON {

    public static class ReadJsonFile{

        private File storage_file;
        public ReadJsonFile(){
            storage_file = null;
        }

        public HashMap<String, String> ReadFile (String filename){
            HashMap<String, String> parsedData = new HashMap<>();
            try {
                File file = new File((Environment.getExternalStorageDirectory()+"/SmartCap"), filename);
                setFile(file);
                FileInputStream stream = new FileInputStream(file);
                String jsonStr = null;
                try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                    jsonStr = Charset.defaultCharset().decode(bb).toString();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                finally {
                    stream.close();
                }
                JSONObject jsonObj = new JSONObject(jsonStr);
                Log.wtf("TESTE", String.valueOf(jsonObj));
                // Getting data JSON Array nodes
                //JSONArray data  = jsonObj.getJSONArray("data");



                // looping through All nodes
                    String nome = jsonObj.getString("nome");
                    String duracao = jsonObj.getString("duracao");
                    String data_captura = jsonObj.getString("data_capture");


                    Log.wtf("Nome", nome);
                    Log.wtf("Duracao", duracao);
                    Log.wtf("Data_captura", data_captura);
                    //use >  int id = c.getInt("duration"); if you want get an int

                    // tmp hashmap for single node


                    // adding each child node to HashMap key => value
                    parsedData.put("name", nome);
                    parsedData.put("duration", duracao);
                    parsedData.put("date", data_captura);
                    //parsedData.put("Acc", )
                    // do what do you want on your interface

            } catch (Exception e) {
                e.printStackTrace();
            }
            return parsedData;
        }

        private void setFile(File f) {
            this.storage_file = f;
        }

        public File getFile(){
            return this.storage_file;
        }
    }
}
