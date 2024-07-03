package com.qiniu.android.common;

import com.qiniu.android.utils.ListUtils;
import com.qiniu.android.utils.StringUtils;
import com.qiniu.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jemy on 17/04/2017.
 */
public class ZoneInfo implements Cloneable {

    /**
     * 默认 io host
     * 只允许内部使用
     *
     * @hidden
     */
    public final static String SDKDefaultIOHost = "sdkDefaultIOHost";

    /**
     * 未知 region id
     *
     * @hidden
     */
    public final static String EmptyRegionId = "unknown";

    private static int DOMAIN_FROZEN_SECONDS = 10 * 60;

    /**
     * 有效时间
     */
    public final int ttl;

    /**
     * 是否允许 HTTP/3
     */
    public final boolean http3Enabled;

    /**
     * 是否是 IPv6
     */
    public final boolean ipv6;

    /**
     * 加速域名
     */
    public final List<String> domains;

    /**
     * 主要域名
     */
    public final List<String> accelerateDomains;

    /**
     * 备用域名
     */
    public final List<String> old_domains;

    /**
     * regionId
     */
    public final String regionId;

    /**
     * 所有域名
     */
    public List<String> allHosts;

    /**
     * detailInfo
     */
    public JSONObject detailInfo;

    private final Date buildDate;

    /**
     * 构造函数
     *
     * @param mainHosts 主要域名
     * @param regionId  区域 ID
     * @return ZoneInfo
     */
    public static ZoneInfo buildInfo(List<String> mainHosts,
                                     String regionId) {
        return buildInfo(null, mainHosts, null, regionId);
    }

    /**
     * 构造函数
     *
     * @param mainHosts 主要域名
     * @param oldHosts  备用域名
     * @param regionId  区域 ID
     * @return ZoneInfo
     */
    public static ZoneInfo buildInfo(List<String> mainHosts,
                                     List<String> oldHosts,
                                     String regionId) {
        return buildInfo(null, mainHosts, oldHosts, regionId);
    }

    /**
     * 构造函数
     *
     * @param accelerateHosts 加速域名
     * @param mainHosts         主要域名
     * @param oldHosts          备用域名
     * @param regionId          区域 ID
     * @return ZoneInfo
     */
    public static ZoneInfo buildInfo(List<String> accelerateHosts,
                                     List<String> mainHosts,
                                     List<String> oldHosts,
                                     String regionId) {
        if (accelerateHosts == null && mainHosts == null) {
            return null;
        }

        HashMap<String, Object> up = new HashMap<>();
        if (accelerateHosts != null) {
            up.put("acc_domains", new JSONArray(accelerateHosts));
        }
        if (mainHosts != null) {
            up.put("domains", new JSONArray(mainHosts));
        }
        if (oldHosts != null) {
            up.put("old", new JSONArray(oldHosts));
        }

        JSONObject upJson = new JSONObject(up);

        if (regionId == null) {
            regionId = EmptyRegionId;
        }
        HashMap<String, Object> info = new HashMap<>();
        info.put("ttl", 86400);
        info.put("region", regionId);
        info.put("up", upJson);

        JSONObject object = new JSONObject(info);

        ZoneInfo zoneInfo = null;
        try {
            zoneInfo = ZoneInfo.buildFromJson(object);
        } catch (JSONException e) {
        }
        return zoneInfo;
    }

    private ZoneInfo(int ttl,
                     boolean http3Enabled,
                     boolean ipv6,
                     String regionId,
                     List<String> accelerateDomains,
                     List<String> domains,
                     List<String> old_domains,
                     Date buildDate) {
        this.ttl = ttl;
        this.http3Enabled = http3Enabled;
        this.ipv6 = ipv6;
        this.regionId = regionId;
        this.domains = domains;
        this.accelerateDomains = accelerateDomains;
        this.old_domains = old_domains;
        this.buildDate = buildDate != null ? buildDate : new Date();
        List<String> allHosts = new ArrayList<>();
        if (accelerateDomains != null) {
            allHosts.addAll(accelerateDomains);
        }
        if (domains != null) {
            allHosts.addAll(domains);
        }
        if (old_domains != null) {
            allHosts.addAll(old_domains);
        }
        this.allHosts = allHosts;
    }

    /**
     * 构造函数
     *
     * @param obj Not allowed to be null
     * @return ZoneInfo
     * @throws JSONException 异常
     */
    public static ZoneInfo buildFromJson(JSONObject obj) throws JSONException {
        if (obj == null) {
            return null;
        }

        int ttl = obj.optInt("ttl");
        long timestamp = obj.optInt("timestamp");
        if (timestamp < 100) {
            timestamp = Utils.currentSecondTimestamp();
            obj.put("timestamp", timestamp);
        }

        boolean http3Enabled = false;
        boolean ipv6Enabled = false;
        try {
            JSONObject features = obj.getJSONObject("features");
            JSONObject http3 = features.optJSONObject("http3");
            if (http3 != null) {
                http3Enabled = http3.optBoolean("enabled");
            }

            JSONObject ipv6 = features.optJSONObject("ipv6");
            if (ipv6 != null) {
                ipv6Enabled = ipv6.optBoolean("enabled");
            }
        } catch (Exception ignored) {
        }

        String regionId = obj.optString("region", EmptyRegionId);
        JSONObject up = obj.optJSONObject("up");
        if (up == null) {
            return null;
        }

        List<String> domains = new ArrayList<>();
        JSONArray domainsJson = up.optJSONArray("domains");
        if (domainsJson != null && domainsJson.length() > 0) {
            for (int i = 0; i < domainsJson.length(); i++) {
                String domain = domainsJson.optString(i);
                if (!StringUtils.isNullOrEmpty(domain)) {
                    domains.add(domain);
                }
            }
        }

        List<String> accelerateDomains = new ArrayList<>();
        JSONArray accelerateDomainsJson = up.optJSONArray("acc_domains");
        if (accelerateDomainsJson != null && accelerateDomainsJson.length() > 0) {
            for (int i = 0; i < accelerateDomainsJson.length(); i++) {
                String domain = accelerateDomainsJson.optString(i);
                if (!StringUtils.isNullOrEmpty(domain)) {
                    accelerateDomains.add(domain);
                }
            }
        }

        List<String> old_domains = new ArrayList<>();
        JSONArray old_domainsJson = up.optJSONArray("old");
        if (old_domainsJson != null && old_domainsJson.length() > 0) {
            for (int i = 0; i < old_domainsJson.length(); i++) {
                String domain = old_domainsJson.optString(i);
                if (!StringUtils.isNullOrEmpty(domain)) {
                    old_domains.add(domain);
                }
            }
        }

        if (ListUtils.isEmpty(accelerateDomains) && ListUtils.isEmpty(domains)) {
            return null;
        }

        Date buildDate = new Date(timestamp * 1000);
        ZoneInfo zoneInfo = new ZoneInfo(ttl, http3Enabled, ipv6Enabled, regionId,
                accelerateDomains, domains, old_domains, buildDate);
        zoneInfo.detailInfo = obj;

        return zoneInfo;
    }

    /**
     * 获取区域 ID
     *
     * @return 区域 ID
     */
    public String getRegionId() {
        return regionId;
    }

    /**
     * 转字符串
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("ttl", this.ttl);
        m.put("allHost", this.allHosts);
        return new JSONObject(m).toString();
    }

    /**
     * 是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        if (buildDate == null) {
            return false;
        }

        int currentTimestamp = (int) (Utils.currentSecondTimestamp());
        int buildTimestamp = (int) (buildDate.getTime() * 0.001);
        return ttl > (currentTimestamp - buildTimestamp);
    }

    /**
     * clone
     *
     * @return Object
     * @throws CloneNotSupportedException 异常
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        ZoneInfo info = new ZoneInfo(ttl, http3Enabled, ipv6, regionId,
                accelerateDomains, domains, old_domains, buildDate);
        info.detailInfo = detailInfo;
        return info;
    }

    /**
     * server group
     */
    @Deprecated
    public static class UploadServerGroup {

        /**
         * 备注信息
         */
        public final String info;

        /**
         * 主要域名
         */
        public final ArrayList<String> main;

        /**
         * 备用域名
         */
        public final ArrayList<String> backup;

        /**
         * 所有域名
         */
        public final ArrayList<String> allHosts;

        /**
         * 构造函数
         *
         * @param jsonObject json
         * @return UploadServerGroup
         */
        public static UploadServerGroup buildInfoFromJson(JSONObject jsonObject) {
            if (jsonObject == null) {
                return null;
            }

            String info = null;
            ArrayList<String> main = new ArrayList<String>();
            ArrayList<String> backup = new ArrayList<String>();
            ArrayList<String> allHosts = new ArrayList<String>();

            try {
                info = jsonObject.getString("info");
            } catch (JSONException e) {
            }

            try {
                JSONArray mainArray = jsonObject.getJSONArray("main");
                for (int i = 0; i < mainArray.length(); i++) {
                    String item = mainArray.getString(i);
                    main.add(item);
                    allHosts.add(item);
                }
            } catch (JSONException e) {
            }

            try {
                JSONArray mainArray = jsonObject.getJSONArray("backup");
                for (int i = 0; i < mainArray.length(); i++) {
                    String item = mainArray.getString(i);
                    main.add(item);
                    allHosts.add(item);
                }
            } catch (JSONException e) {
            }

            return new UploadServerGroup(info, main, backup, allHosts);
        }

        /**
         * 构造函数
         *
         * @param info     备注信息
         * @param main     主要域名
         * @param backup   备用域名
         * @param allHosts 所有域名
         */
        public UploadServerGroup(String info,
                                 ArrayList<String> main,
                                 ArrayList<String> backup,
                                 ArrayList<String> allHosts) {
            this.info = info;
            this.main = main;
            this.backup = backup;
            this.allHosts = allHosts;
        }
    }
}
