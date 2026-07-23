package com.recruitment.ai.config;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI模型配置
 */
@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class AiModelConfig {

    @Bean
    @RefreshScope
    public OkHttpClient openAiHttpClient(OpenAiProperties properties) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(properties.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                .protocols(List.of(Protocol.HTTP_1_1));
        if (StringUtils.hasText(properties.getProxyHost()) && properties.getProxyPort() > 0) {
            Proxy fallbackProxy = new Proxy(
                    Proxy.Type.HTTP,
                    new InetSocketAddress(properties.getProxyHost(), properties.getProxyPort()));
            httpClientBuilder.proxySelector(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    return List.of(Proxy.NO_PROXY, fallbackProxy);
                }

                @Override
                public void connectFailed(URI uri, SocketAddress address, IOException exception) {
                    // OkHttp continues with the next route returned by select().
                }
            });
        }
        return httpClientBuilder.build();
    }
}
