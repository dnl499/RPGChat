package com.example.daniel.directchat;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Daniel on 26/10/2017.
 *
 * It contains the OnClick and OnLongClick for the tab buttons. The OnClick makes the clicked Button disabled and loads it's contents on the frame.
 * The OnLongClick closes a tab and remove the data from the closed tab from the memory.
 */

class ButtonClickHandler implements View.OnClickListener, View.OnLongClickListener {
    @Override
    public void onClick(View view) {
        MainActivity.btns.get(MainActivity.atual).setEnabled(true);
        MainActivity.atual = Integer.parseInt(view.getContentDescription().toString());
        MainActivity.ln.removeAllViews();
        for (int c = 0; c < MainActivity.nMsgs.get(MainActivity.atual); c++) {
            MainActivity.ln.addView(MainActivity.tvs.get(MainActivity.atual).get(c));
        }
        MainActivity.btns.get(MainActivity.atual).setEnabled(false);
        MainActivity.btns.get(MainActivity.atual).setBackgroundColor(MainActivity.btns.get(0).getSolidColor());
        if (MainActivity.isRoom.get(MainActivity.atual)) {
            MainActivity.tools.setVisibility(View.VISIBLE);
            MainActivity.part.setVisibility(View.VISIBLE);
            MainActivity.swi.setVisibility(View.VISIBLE);
            MainActivity.fav.setVisibility(View.VISIBLE);
        } else {
            MainActivity.tools.setVisibility(View.INVISIBLE);
            MainActivity.part.setVisibility(View.INVISIBLE);
            MainActivity.swi.setVisibility(View.INVISIBLE);
            MainActivity.fav.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        MainActivity.btns.get(MainActivity.atual).setEnabled(true);
        MainActivity.ln1.removeView(view);
        int trgt = Integer.parseInt(view.getContentDescription().toString());
        MainActivity.btns.remove(trgt);
        MainActivity.tvs.remove(trgt);
        MainActivity.oppened.clear();
        MainActivity.isRoom.remove(trgt);
        MainActivity.roomMsgs.remove(trgt);
        MainActivity.nMsgs.remove(trgt);
        for (int i = 0; i < MainActivity.btns.size(); i++) {
            MainActivity.btns.get(i).setContentDescription(String.valueOf(i));
            if (i > 0) MainActivity.oppened.add(MainActivity.btns.get(i).getText().toString());
        }

        MainActivity.atual = 0;
        MainActivity.ln.removeAllViews();
        for (int c = 0; c < MainActivity.nMsgs.get(MainActivity.atual); c++) {
            MainActivity.ln.addView(MainActivity.tvs.get(MainActivity.atual).get(c));
        }
        MainActivity.btns.get(MainActivity.atual).setEnabled(false);
        Button bt = (Button)view;
        exitRoom(bt.getText().toString());
        return true;
    }


    private void exitRoom(String nome) {
        String pw = MainActivity.pass.getText().toString();
        String lg = MainActivity.login.getText().toString();
        URL url = null;
        try {
            url = new URL("http://chaos-workbench.net16.net/RoomExit.php");
        } catch (MalformedURLException e) {
            MainActivity.mensagem("Erro: URL malformado", e.getMessage());
        }

        byte[] biv = new byte[12];
        Random rnd = new Random();
        rnd.nextBytes(biv);
        String rniv = Base64.encodeToString(biv, Base64.DEFAULT);

        String out = MainActivity.encrypt(pw, pw, rniv);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("LOGIN", lg)
                .add("PASS", out)
                .add("IV", rniv)
                .add("SALA", nome)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("content-type", "application/json; charset=utf-8")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                MainActivity.mensagem("Erro de conexão: ", "Talvez você tenha sido desconectado ou o servidor esteja offline.");

            }

            @SuppressWarnings("ConstantConditions")
            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                final String res = response.body().string();
                Looper.prepare();
                if (res.contains("uuser")) {
                    MainActivity.mensagem("App", "Como você logou? :o");
                } else if (res.contains("lgus")) {
                    MainActivity.mensagem("App", "Como você logou? :o");
                } else {
                    MainActivity.mensagem("App", res);
                }
            }
        });
    }
}
