package com.yourcompany.iptv; // 替换成你的包名

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView channelListView;
    private List<String> channelNames = new ArrayList<>();
    private List<String> channelUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        channelListView = findViewById(R.id.channel_list);

        // 从config.json读取直播源URL
        String liveSourceUrl = getDefaultLiveSourceUrl();
        if (liveSourceUrl != null) {
            new LoadChannelsTask().execute(liveSourceUrl);
        }

        // 设置列表项点击事件
        channelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String channelUrl = channelUrls.get(position);
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("CHANNEL_URL", channelUrl);
                startActivity(intent);
            }
        });
    }

    // 从assets/config.json获取默认直播源URL
    private String getDefaultLiveSourceUrl() {
        try {
            InputStream is = getAssets().open("config.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            Gson gson = new Gson();
            Config config = gson.fromJson(sb.toString(), Config.class);
            for (LiveSource source : config.live_sources) {
                if (source.is_default) {
                    return source.url;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 异步任务加载频道列表
    private class LoadChannelsTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                String currentChannelName = null;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#EXTINF:")) {
                        // 解析频道名称
                        int nameStart = line.indexOf(',') + 1;
                        if (nameStart < line.length()) {
                            currentChannelName = line.substring(nameStart);
                        }
                    } else if (currentChannelName != null && !line.startsWith("#")) {
                        // 解析频道URL
                        channelNames.add(currentChannelName);
                        channelUrls.add(line);
                        currentChannelName = null;
                    }
                }
                reader.close();
                return true;
            } catch (IOException e) {
                Log.e("MainActivity", "Error loading channels", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_list_item_1, channelNames);
                channelListView.setAdapter(adapter);
            }
        }
    }

    // 内部类，用于解析config.json
    private static class Config {
        List<LiveSource> live_sources;
    }

    private static class LiveSource {
        String name;
        String url;
        boolean is_default;
    }
}
