package com.totato.karaoke.me.autocompletecomponent;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class AutocompleteFinder {

    private AutocompleteFinder() {
    }

    private IAutocompleteFinder callBack = null;
    static AutocompleteFinder singleton;
    private String language = "en";

    public static AutocompleteFinder getInstance(IAutocompleteFinder callBack, String language) {
        if (singleton == null && callBack != null && language != null && language.trim().length() > 0) {
            singleton = new AutocompleteFinder();
            singleton.callBack = callBack;
            singleton.language = language.trim();
        }
        return singleton;
    }

    private String createUrl(String search) {
        String url = "http://suggestqueries.google.com/complete/search?ds=yt&client=firefox&ie=utf-8&oe=utf-8&hl=" + language;
        try {
            url += "&q=" + "karaoke%20" + Uri.encode(search);
            Log.e("TRUNG", url);
        } catch (Exception e) {
        }
        return url;
    }

    public void start(String search) {
        destroy();
        task = new AutocompleteTask(search);
        task.execute(new Object[]{});
    }

    private AutocompleteTask task = null;

    public void destroy() {
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    class AutocompleteTask extends AsyncTask<Object, Object, Object> {

        private String search = null;

        AutocompleteTask(String search) {
            this.search = search;
        }

        private List<String> parse(String json) {
            List<String> list = new ArrayList<>();
            try {
                json = BypassSSLCerficicate.decodeArray(json)[1];
                String[] array = BypassSSLCerficicate.decodeArray(json);
                if (array != null && array.length >= 2) {
                    for (int i = 0; i < array.length; i++) {
                        if (array[i] != null && array[i].length() > 0) list.add(array[i].replace("karaoke ",""));
                    }
                }
            } catch (Exception e) {
            }
            return list;
        }

        @Override
        protected Object doInBackground(Object... params) {
            Object result = null;
            try {
                String urlService = createUrl(search);
                String json = BypassSSLCerficicate.getInstance().sendGET(urlService);
                if (json != null && json.length() > 0) result = parse(json);
            } catch (Exception e) {
                result = null;
            }
            return result;
        }

        protected void onPostExecute(Object result) {
            if (callBack != null) {
                if (result != null && result instanceof List) {
                    callBack.success(search, (List<String>) result);
                } else {
                    callBack.error("Unknown data");
                }
            }
        }

    }

}
