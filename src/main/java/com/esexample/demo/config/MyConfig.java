package com.esexample.demo.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author:hepo
 * @Version:v1.0
 * @Description:
 * @Date:2018/11/16/016
 * @Time:14:49
 */
@Configuration
public class MyConfig {
    @Bean
    public TransportClient lient() throws UnknownHostException {
        TransportAddress node=new TransportAddress(InetAddress.getByName("localhost"),9300);
        Settings settings=Settings.builder().put("cluster.name","clusterNoName").build();

        TransportClient client=new PreBuiltTransportClient(settings);
        client.addTransportAddress(node);
        return client;
    }

}
