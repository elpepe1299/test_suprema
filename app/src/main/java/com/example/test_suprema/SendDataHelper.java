package com.example.test_suprema;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;

public class SendDataHelper {

    public interface SendDataCallback {
        void onSuccess(String response);

        void onError(String error);
    }

    public static void sendData(Context context, byte[] capturedData, final SendDataCallback callback) {
        try {
            // URL del servicio web
            String url = "http://192.168.1.53:3010/tiempo_posteo";

            // Crear un objeto JSON con el campo "huella"
            String base64Data = Base64.getEncoder().encodeToString(capturedData);
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("huella", base64Data);

            // Inicializar la cola de solicitudes Volley
            RequestQueue requestQueue = Volley.newRequestQueue(context);

            // Crear una solicitud JSON POST
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonRequest,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                // Manejar la respuesta del servidor
                                String result = response.getString("resultado");
                                callback.onSuccess(result);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                callback.onError("Error al analizar la respuesta JSON");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Manejar errores de la solicitud
                            callback.onError("Error en la solicitud: " + error.getMessage());
                        }
                    }
            );

            // Agregar la solicitud a la cola
            requestQueue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
            callback.onError("Error al crear la solicitud JSON");
        }
    }
}
