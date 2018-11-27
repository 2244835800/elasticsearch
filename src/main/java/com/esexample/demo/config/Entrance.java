package com.esexample.demo.config;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author:hepo
 * @Version:v1.0
 * @Description:
 * @Date:2018/11/19/019
 * @Time:15:41
 */
public class Entrance {
    private static HtmlPage getHtmlPage(String url) throws Exception {
        final WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setMaxInMemory(500);
        final HtmlPage page = webClient.getPage(url);
        System.err.println("查询中，请稍候");
//        TimeUnit.SECONDS.sleep(5);    //web请求数据需要时间，必须让主线程休眠片刻
        webClient.close();

        return page;
    }

   public static List<String> getAllUrl(String preString,Integer atomic,String lastString,Integer pageCount){
       List urlList= new ArrayList<>();
       for(int i=0;i<pageCount;i++){
           String url=preString+atomic+lastString;
           atomic++;
           urlList.add(url);
       }
       return urlList;
   }
   public static List getItemListByPage(HtmlPage page){
       List list=new ArrayList<>();
       page.getElementsByTagName("ul").get(2).getElementsByTagName("li").iterator().forEachRemaining(li -> {
           Map<String,String> map=new HashMap();
           AtomicInteger count= new AtomicInteger();
           System.out.println("——————————————————————————————————");
           li.getChildElements().iterator().forEachRemaining(str->{
               System.out.println(str.getNodeName()+" "+str.getTextContent());
               if(str.getNodeName().equals("a")){
                   map.put(String.valueOf(count.get()),((HtmlAnchor) str).getHrefAttribute());
                   count.getAndIncrement();
               }
               map.put(String.valueOf(count.get()),str.getTextContent());
               count.getAndIncrement();
           });
           list.add(map);
       });
       return list;
   }

    public static TransportClient client() throws UnknownHostException {
        TransportAddress node=new TransportAddress(InetAddress.getByName("localhost"),9300);
        Settings settings=Settings.builder().put("cluster.name","clusterNoName").build();
        TransportClient client=new PreBuiltTransportClient(settings);
        client.addTransportAddress(node);
        return client;
    }
    public static ResponseEntity addProjectItem(Map<String,String> map) {
        try {
            XContentBuilder content = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("url", map.get("0"))
                    .field("title", map.get("1"))
                    .field("type", map.get("2"))
                    .field("date",  map.get("3"))
                    .field("region",  map.get("4"))
                    .field("purchaser",  map.get("5"))
                    .endObject();

            IndexResponse result = client().prepareIndex("project", "item").setSource(content).get();
            return new ResponseEntity(result.getId(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public static void main(String[] args) throws Exception {

        String webUrl = "http://www.ccgp.gov.cn/cggg/dfgg/";
        List<String> pageList=getAllUrl("http://www.ccgp.gov.cn/cggg/dfgg/index_",1,".htm",24);
        pageList.add(0,webUrl);
        for(String url:pageList){
            HtmlPage page = getHtmlPage(url);
            List<Map<String,String>> list=getItemListByPage(page);
            for(Map<String,String> item:list){
                addProjectItem(item);
            }
            System.out.println("******************************");
        }


//        page.getElementsByTagName("ul").get(2).getElementsByTagName("li").get(0).getChildElements().iterator().forEachRemaining(str->{
//            System.out.println("ssss:"+str.getTextContent());
//        });


    }

}
