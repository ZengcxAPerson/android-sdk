package com.qiniu.android.storage;

import android.content.Context;

import com.qiniu.android.http.dns.Dns;
import com.qiniu.android.utils.UrlSafeBase64;
import com.qiniu.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 上传全局配置
 */
public class GlobalConfiguration {

    /**
     * APP Context
     */
    public static Context appContext;

    /**
     * 是否开启dns预解析 默认开启
     */
    public boolean isDnsOpen = true;

    /**
     * dns 预取失败后 会进行重新预取  dnsRepreHostNum为最多尝试次数
     */
    public int dnsRepreHostNum = 2;

    /**
     * 在 dns 预取时的超时时间
     * 单位：秒
     */
    public int dnsResolveTimeout = 3;

    /**
     * dns 预取, ip 默认有效时间  单位：秒 默认：120
     * 只有在 dns 预取未返回 ttl 时使用
     */
    public int dnsCacheTime = 120;

    /**
     * dns预取缓存最大有效时间  单位：秒 默认 600
     * 当 dns 缓存 ip 过期并未刷新时，只要在 dnsCacheMaxTTL 时间内仍有效。
     */
    public int dnsCacheMaxTTL = 600;

    /**
     * 自定义DNS解析客户端host
     */
    public Dns dns = null;

    /**
     * 自定义DNS解析客户端host
     */
    public String dnsCacheDir = Utils.sdkDirectory() + "/dnsCache/";

    /**
     * 是否使用 udp 方式进行 Dns 预取，默认开启
     */
    public boolean udpDnsEnable = true;

    /**
     * 设置 udp ipv4 server，请直接设置 {@link GlobalConfiguration#udpDnsIpv4Servers}
     * 此值不可修改
     */
    public static String[] DefaultUdpDnsIpv4Servers = parseBase64Array("WyIyMjMuNS41LjUiLCAiMTE0LjExNC4xMTQuMTE0IiwgIjEuMS4xLjEiLCAiOC44LjguOCJd");

    /**
     * 使用 udp 进行 Dns 预取时的 server ipv4 数组；当对某个 Host 使用 udp 进行 Dns 预取时，会使用 server 数组进行并发预取
     * 当 udpDnsEnable 开启时，使用 udp 进行 Dns 预取方式才会生效
     * 默认 {@link GlobalConfiguration#DefaultUdpDnsIpv4Servers}
     */
    public String[] udpDnsIpv4Servers = null;

    /**
     * 设置 udp ipv6 server，请直接设置 {@link GlobalConfiguration#udpDnsIpv6Servers}
     * 此值不可修改
     */
    public static String[] DefaultUdpDnsIpv6Servers = null;

    /**
     * 使用 udp 进行 Dns 预取时的 server ipv6 数组；当对某个 Host 使用 udp 进行 Dns 预取时，会使用 server 数组进行并发预取
     * 当 udpDnsEnable 开启时，使用 udp 进行 Dns 预取方式才会生效
     * 默认 {@link GlobalConfiguration#DefaultUdpDnsIpv6Servers}
     */
    public String[] udpDnsIpv6Servers = null;

    /**
     * 是否使用 doh 预取，默认开启
     */
    public boolean dohEnable = true;

    /**
     * 设置 doh ipv4 server，请直接设置 {@link GlobalConfiguration#dohIpv4Servers}
     * 此值不可修改
     */
    public static String[] DefaultDohIpv4Servers = parseBase64Array("WyJodHRwczovLzIyMy42LjYuNi9kbnMtcXVlcnkiLCAiaHR0cHM6Ly84LjguOC44L2Rucy1xdWVyeSJd");

    /**
     * 使用 doh 预取时的 server 数组；当对某个 Host 使用 Doh 预取时，会使用 server 数组进行并发预取
     * 当 dohEnable 开启时，doh 预取才会生效
     * 默认 {@link GlobalConfiguration#DefaultDohIpv4Servers}
     * 注意：如果使用 ip，需保证服务证书与 IP 绑定，避免 sni 问题
     */
    public String[] dohIpv4Servers = null;

    /**
     * 设置 doh ipv6 server，请直接设置 {@link GlobalConfiguration#dohIpv6Servers}
     * 此值不可修改
     */
    public static String[] DefaultDohIpv6Servers = null;

    /**
     * 使用 doh 预取时的 server 数组；当对某个 Host 使用 Doh 预取时，会使用 server 数组进行并发预取
     * 当 dohEnable 开启时，doh 预取才会生效
     * 默认 {@link GlobalConfiguration#DefaultDohIpv6Servers}
     * 注意：如果使用 ip，需保证服务证书与 IP 绑定，避免 sni 问题
     */
    public String[] dohIpv6Servers = null;

    /**
     * Host全局冻结时间  单位：秒   默认：30  推荐范围：[10 ~ 60]
     * 当某个Host的上传失败后并且可能短时间无法恢复，会冻结该Host，globalHostFrozenTime为全局冻结时间
     * Host全局冻结时间  单位：秒   默认：10  推荐范围：[5 ~ 30]
     * 当某个Host的上传失败后并且可能短时间无法恢复，会冻结该Host
     */
    public int globalHostFrozenTime = 10;

    /**
     * Host局部冻结时间，只会影响当前上传操作  单位：秒   默认：5*60  推荐范围：[60 ~ 10*60]
     * 当某个Host的上传失败后并且短时间可能会恢复，会局部冻结该Host
     */
    public int partialHostFrozenTime = 5 * 60;


    /**
     * 设置 connect check urls，请直接设置 {@link GlobalConfiguration#connectCheckURLStrings}
     * 此值不可修改
     */
    public static String[] DefaultConnectCheckURLStrings = parseBase64Array("WyJodHRwczovL3d3dy5xaW5pdS5jb20iLCAiaHR0cHM6Ly93d3cuYmFpZHUuY29tIiwgImh0dHBzOi8vd3d3Lmdvb2dsZS5jb20iXQ==");


    /**
     * 网络连接状态检测使用的connectCheckURLStrings，网络链接状态检测可能会影响重试机制，启动网络连接状态检测有助于提高上传可用性。
     * 当请求的 Response 为网络异常时，并发对 connectCheckURLStrings 中 URLString 进行 HEAD 请求，以此检测当前网络状态的链接状态，其中任意一个 URLString 链接成功则认为当前网络状态链接良好；
     * 当 connectCheckURLStrings 为 nil 或者 空数组时则弃用检测功能。
     */
    public String[] connectCheckURLStrings = null;

    /**
     * 网络连接状态检测HEAD请求超时，默认：2s
     */
    public int connectCheckTimeout = 2;

    /**
     * 是否开启网络连接状态检测，默认：开启
     */
    public boolean connectCheckEnable = true;

    /**
     * 是否允许 http3
     * 默认上传 client 不支持 http3, 如果 enable 需要定制支持 http3 的 client;
     * client 指定方法见 {@link Configuration#requestClient}
     */
    public boolean enableHttp3 = false;

    private static final GlobalConfiguration configuration = new GlobalConfiguration();

    private GlobalConfiguration() {
    }

    /**
     * 获取全局配置单例
     *
     * @return 单例
     */
    public static GlobalConfiguration getInstance() {
        return configuration;
    }

    /**
     * 获取 UDP dns ipv4 server 列表
     *
     * @return server 列表
     */
    public String[] getUdpDnsIpv4Servers() {
        if (udpDnsIpv4Servers != null) {
            return udpDnsIpv4Servers;
        } else {
            return DefaultUdpDnsIpv4Servers;
        }
    }

    /**
     * 获取 UDP dns ipv6 server 列表
     *
     * @return server 列表
     */
    public String[] getUdpDnsIpv6Servers() {
        if (udpDnsIpv6Servers != null) {
            return udpDnsIpv6Servers;
        } else {
            return DefaultUdpDnsIpv6Servers;
        }
    }

    /**
     * 获取 Doh ipv4 server 列表
     *
     * @return server 列表
     */
    public String[] getDohIpv4Servers() {
        if (dohIpv4Servers != null) {
            return dohIpv4Servers;
        } else {
            return DefaultDohIpv4Servers;
        }
    }

    /**
     * 获取 Doh 方式进行 Dns 预解析使用的 IPv6 Server
     *
     * @return Server 列表
     */
    public String[] getDohIpv6Servers() {
        if (dohIpv6Servers != null) {
            return dohIpv6Servers;
        } else {
            return DefaultDohIpv6Servers;
        }
    }

    /**
     * 获取网络状态检测使用的 Url
     *
     * @return Url 列表
     */
    public String[] getConnectCheckUrls() {
        if (connectCheckURLStrings != null) {
            return connectCheckURLStrings;
        } else {
            return DefaultConnectCheckURLStrings;
        }
    }

    /**
     * 解析 Base64 数据为字符串数组
     *
     * @param data base64 data
     * @return 字符串数组
     */
    public static String[] parseBase64Array(String data) {
        try {
            byte[] jsonBytes = UrlSafeBase64.decode(data);
            JSONArray jsonArray = new JSONArray(new String(jsonBytes));
            int count = jsonArray.length();
            String[] items = new String[count];
            for (int i = 0; i < count; i++) {
                items[i] = jsonArray.getString(i);
            }
            return items;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
