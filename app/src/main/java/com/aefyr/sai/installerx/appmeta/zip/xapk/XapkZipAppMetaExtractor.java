package com.aefyr.sai.installerx.appmeta.zip.xapk;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.aefyr.sai.installerx.appmeta.AppMeta;
import com.aefyr.sai.installerx.appmeta.zip.ZipAppMetaExtractor;
import com.aefyr.sai.utils.IOUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.ZipEntry;

//TODO handle manifest.json versions
public class XapkZipAppMetaExtractor implements ZipAppMetaExtractor {
    private static final String TAG = "XapkMetaExtractor";

    private static final String META_FILE = "manifest.json";
    private static final String ICON_FILE = "icon.png";

    private Context mContext;
    private AppMeta mAppMeta;

    public XapkZipAppMetaExtractor(Context context) {
        mContext = context.getApplicationContext();
        mAppMeta = new AppMeta();
    }

    @Override
    public boolean wantEntry(ZipEntry entry) {
        return entry.getName().equals(META_FILE) || entry.getName().equals(ICON_FILE);
    }

    @Override
    public void consumeEntry(ZipEntry entry, InputStream entryInputStream) {
        if (entry.getName().equals(META_FILE)) {
            try {
                JSONObject metaJson = new JSONObject(IOUtils.readStream(entryInputStream, StandardCharsets.UTF_8));
                mAppMeta.packageName = metaJson.optString("package_name");
                mAppMeta.appName = metaJson.optString("name");
                mAppMeta.versionName = metaJson.optString("version_name");
                mAppMeta.versionCode = metaJson.optLong("version_code");
            } catch (Exception e) {
                Log.w(TAG, "Unable to extract meta", e);
            }
        }

        if (entry.getName().equals(ICON_FILE)) {
            File iconFile = new File(getCacheDir(), UUID.randomUUID().toString() + ".png");
            try (InputStream in = entryInputStream; OutputStream out = new FileOutputStream(iconFile)) {
                IOUtils.copyStream(in, out);
                mAppMeta.iconUri = Uri.fromFile(iconFile);
            } catch (IOException e) {
                Log.w(TAG, "Unable to extract icon", e);
            }
        }
    }

    private File getCacheDir() {
        File cacheDir = new File(mContext.getCacheDir(), "XapkZipAppMetaExtractor");
        if (!cacheDir.exists())
            cacheDir.mkdir();

        return cacheDir;
    }

    @Override
    public AppMeta buildMeta() {
        return mAppMeta;
    }

}