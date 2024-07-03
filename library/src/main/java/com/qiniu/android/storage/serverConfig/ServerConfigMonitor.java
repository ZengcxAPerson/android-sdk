package com.qiniu.android.storage.serverConfig;

import com.qiniu.android.common.AutoZone;
import com.qiniu.android.http.dns.DnsPrefetcher;
import com.qiniu.android.storage.GlobalConfiguration;
import com.qiniu.android.transaction.TransactionManager;

import java.util.List;
import java.util.Random;

/**
 * 服务控制监控
 */
public class ServerConfigMonitor {
    private static final String TransactionKey = "ServerConfig";

    private boolean enable = true;
    private final ServerConfigCache cache = new ServerConfigCache();
    private static final ServerConfigMonitor configMonitor = new ServerConfigMonitor();

    private ServerConfigMonitor() {
    }

    /**
     * 是否开启
     *
     * @param enable 是否开启
     */
    public static void setEnable(boolean enable) {
        configMonitor.enable = enable;
    }

    /**
     * 配置上传 Token，SDK 会在上传文件时自动配置
     *
     * @param token 上传 Token
     */
    public static void setToken(String token) {
        ServerConfigSynchronizer.setToken(token);
    }

    /**
     * 配置 server hosts
     *
     * @param hosts hosts
     */
    public static void setServerHosts(String[] hosts) {
        ServerConfigSynchronizer.setHosts(hosts);
    }

    /**
     * 清理配置缓存
     */
    public static void removeConfigCache() {
        configMonitor.cache.removeConfigCache();
    }

    /**
     * 开始监控
     */
    public synchronized static void startMonitor() {
        if (!configMonitor.enable) {
            return;
        }

        TransactionManager transactionManager = TransactionManager.getInstance();
        boolean isExist = transactionManager.existTransactionsForName(TransactionKey);
        if (isExist) {
            return;
        }

        Random random = new Random();
        int interval = 120 + random.nextInt(240);
        TransactionManager.Transaction transaction = new TransactionManager.Transaction(TransactionKey, 0, interval, new Runnable() {
            @Override
            public void run() {
                configMonitor.monitor();
            }
        });
        transactionManager.addTransaction(transaction);
    }

    /**
     * 停止监控
     */
    public synchronized static void endMonitor() {
        TransactionManager transactionManager = TransactionManager.getInstance();
        List<TransactionManager.Transaction> transactions = transactionManager.transactionsForName(TransactionKey);
        if (transactions != null) {
            for (TransactionManager.Transaction transaction : transactions) {
                transactionManager.removeTransaction(transaction);
            }
        }
    }

    private void monitor() {
        if (!enable) {
            return;
        }

        ServerConfig serverConfig = cache.getConfig();
        if (serverConfig == null || !serverConfig.isValid()) {
            ServerConfigSynchronizer.getServerConfigFromServer(new ServerConfigSynchronizer.ServerConfigHandler() {
                @Override
                public void handle(ServerConfig config) {
                    if (config == null) {
                        return;
                    }

                    handleServerConfig(config);
                    cache.setConfig(config);
                }
            });
        } else {
            handleServerConfig(serverConfig);
        }

        ServerUserConfig serverUserConfig = cache.getUserConfig();
        if (serverUserConfig == null || !serverUserConfig.isValid()) {
            ServerConfigSynchronizer.getServerUserConfigFromServer(new ServerConfigSynchronizer.ServerUserConfigHandler() {
                @Override
                public void handle(ServerUserConfig config) {
                    if (config == null) {
                        return;
                    }

                    handleServerUserConfig(config);
                    cache.setUserConfig(config);
                }
            });
        } else {
            handleServerUserConfig(serverUserConfig);
        }
    }

    private void handleServerConfig(ServerConfig config) {
        if (config == null) {
            return;
        }

        final ServerConfig serverConfig = cache.getConfig();
        // 清理 region 缓存
        ServerConfig.RegionConfig regionConfig = config.getRegionConfig();
        ServerConfig.RegionConfig oldRegionConfig = serverConfig != null ? serverConfig.getRegionConfig() : null;
        if (regionConfig != null) {
            if (oldRegionConfig != null && regionConfig.getClearId() > oldRegionConfig.getClearId() && regionConfig.getClearCache()) {
                AutoZone.clearCache();
            }
        }

        // dns 配置
        ServerConfig.DnsConfig dnsConfig = config.getDnsConfig();
        if (dnsConfig != null) {
            if (dnsConfig.getEnable() != null) {
                GlobalConfiguration.getInstance().isDnsOpen = dnsConfig.getEnable();
            }

            // 清理 dns 缓存
            ServerConfig.DnsConfig oldDnsConfig = serverConfig != null ? serverConfig.getDnsConfig() : null;
            if (oldDnsConfig != null && dnsConfig.getClearId() > oldDnsConfig.getClearId() && dnsConfig.getClearCache()) {
                try {
                    DnsPrefetcher.getInstance().clearDnsCache();
                } catch (Exception ignored) {
                }
            }

            // udp 配置
            ServerConfig.UdpDnsConfig udpDnsConfig = dnsConfig.getUdpDnsConfig();
            if (udpDnsConfig != null) {
                if (udpDnsConfig.getEnable() != null) {
                    GlobalConfiguration.getInstance().udpDnsEnable = udpDnsConfig.getEnable();
                }

                ServerConfig.DnsServer ipv4Servers = udpDnsConfig.getIpv4Server();
                if (ipv4Servers != null && ipv4Servers.getIsOverride()) {
                    GlobalConfiguration.DefaultUdpDnsIpv4Servers = ipv4Servers.getServers();
                }

                ServerConfig.DnsServer ipv6Servers = udpDnsConfig.getIpv6Server();
                if (ipv6Servers != null && ipv6Servers.getIsOverride()) {
                    GlobalConfiguration.DefaultUdpDnsIpv6Servers = ipv6Servers.getServers();
                }
            }

            // doh 配置
            ServerConfig.DohDnsConfig dohConfig = dnsConfig.getDohDnsConfig();
            if (dohConfig != null) {
                if (dohConfig.getEnable() != null) {
                    GlobalConfiguration.getInstance().dohEnable = dohConfig.getEnable();
                }

                ServerConfig.DnsServer ipv4Servers = dohConfig.getIpv4Server();
                if (ipv4Servers != null && ipv4Servers.getIsOverride()) {
                    GlobalConfiguration.DefaultDohIpv4Servers = ipv4Servers.getServers();
                }

                ServerConfig.DnsServer ipv6Servers = dohConfig.getIpv6Server();
                if (ipv6Servers != null && ipv6Servers.getIsOverride()) {
                    GlobalConfiguration.DefaultDohIpv6Servers = ipv6Servers.getServers();
                }
            }
        }

        // connect check 配置
        ServerConfig.ConnectCheckConfig checkConfig = config.getConnectCheckConfig();
        if (checkConfig != null) {
            if (checkConfig.getEnable() != null) {
                GlobalConfiguration.getInstance().connectCheckEnable = checkConfig.getEnable();
            }

            if (checkConfig.getTimeoutMs() != null) {
                GlobalConfiguration.getInstance().connectCheckTimeout = checkConfig.getTimeoutMs();
            }

            String[] urls = checkConfig.getUrls();
            Boolean isOverride = checkConfig.getOverride();
            if (isOverride != null && isOverride && urls != null && urls.length > 0) {
                GlobalConfiguration.DefaultConnectCheckURLStrings = urls;
            }
        }
    }

    private void handleServerUserConfig(ServerUserConfig config) {
        if (config == null) {
            return;
        }

        if (config.getNetworkCheckEnable() != null) {
            GlobalConfiguration.getInstance().connectCheckEnable = config.getNetworkCheckEnable();
        }

        if (config.getHttp3Enable() != null) {
            GlobalConfiguration.getInstance().enableHttp3 = config.getHttp3Enable();
        }
    }
}
