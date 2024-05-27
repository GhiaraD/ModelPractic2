package ro.pub.cs.systems.eim.lab03.modelpractic2;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;


public class CommunicationThread extends Thread {

    private final ServerThread serverThread;
    private final Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);

            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");
            String currency = bufferedReader.readLine();
            currency = currency.isEmpty() ? "RON" : currency;
            String informationType = bufferedReader.readLine();
            informationType = informationType.isEmpty() ? Constants.ALL : informationType;
            if (currency == null || currency.isEmpty() || informationType == null || informationType.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }

            HashMap<String, BPIinfo> data = serverThread.getData();
            BPIinfo weatherForecastInformation;
            if (data.containsKey(currency)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                weatherForecastInformation = data.get(currency);
            } else {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                HttpClient httpClient = new DefaultHttpClient();
                String pageSourceCode = "";
                HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + "/" + currency);
                HttpResponse httpGetResponse = httpClient.execute(httpGet);
                HttpEntity httpGetEntity = httpGetResponse.getEntity();
                if (httpGetEntity != null) {
                    pageSourceCode = EntityUtils.toString(httpGetEntity);
                }

                JSONObject content = new JSONObject(pageSourceCode);
                JSONObject bpi = content.getJSONObject(Constants.BPI);

//                JSONObject weatherArray = bpi.getJSONObject(currency);

//                JSONObject weather;
//                StringBuilder condition = new StringBuilder();
//                for (int i = 0; i < weatherArray.length(); i++) {
//                    weather = weatherArray.getJSONObject(i);
//                    condition.append(weather.getString(currency)).append(" : ").append(weather.getString(Constants.DESC));
//
//                    if (i < weatherArray.length() - 1) {
//                        condition.append(";");
//                    }
//                }

                JSONObject main = bpi.getJSONObject(currency);
                String code = main.getString(Constants.CODE);
                String rate = main.getString(Constants.RATE);
                String desc = main.getString(Constants.DESC);
                String ratef = main.getString(Constants.RATEF);

                weatherForecastInformation = new BPIinfo(
                        code, rate, desc, ratef
                );
                serverThread.setData(currency, weatherForecastInformation);
            }
            if (weatherForecastInformation == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Weather Forecast Information is null!");
                return;
            }
            String result;
            switch(informationType) {
                case Constants.ALL:
                    result = weatherForecastInformation.toString();
                    break;
                case Constants.CODE:
                    result = weatherForecastInformation.getCode();
                    break;
                case Constants.RATE:
                    result = weatherForecastInformation.getRate();
                    break;
                case Constants.DESC:
                    result = weatherForecastInformation.getDesc();
                    break;
                case Constants.RATEF:
                    result = weatherForecastInformation.getRatef();
                    break;
                default:
                    result = "[COMMUNICATION THREAD] Wrong information type (all / temperature / wind_speed / condition / humidity / pressure)!";
            }
            printWriter.println(result);
            printWriter.flush();
        } catch (IOException | JSONException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            }
        }
    }

}
