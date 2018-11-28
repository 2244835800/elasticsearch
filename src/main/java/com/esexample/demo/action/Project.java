package com.esexample.demo.action;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @Author:hepo
 * @Version:v1.0
 * @Description:
 * @Date:2018/11/28/028
 * @Time:9:55
 */
@RestController
public class Project {
    @Autowired
    private TransportClient client;

    @PostMapping("/add/project/item")
    @ResponseBody
    public ResponseEntity addman(
            @RequestParam(name = "url", required = false) String url,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "date", required = false)
//                    @DateTimeFormat(pattern = "yyyy-MM-dd")
                    String date,
            @RequestParam(name = "region", required = false) String region) {
        try {
            XContentBuilder content = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("url", url)
                    .field("title", title)
                    .field("date", date)
                    .field("region", region)
                    .endObject();

            IndexResponse result = this.client.prepareIndex("project", "item").setSource(content).get();
            return new ResponseEntity(result.getId(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }


}
