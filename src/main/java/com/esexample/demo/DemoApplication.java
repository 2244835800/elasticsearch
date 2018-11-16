package com.esexample.demo;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;

@SpringBootApplication
@RestController
public class DemoApplication {
    @Autowired
    private TransportClient client;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/get/people/man")
    @ResponseBody
    public ResponseEntity getman(@RequestParam(name = "id", defaultValue = "") String id) {
        GetResponse result = this.client.prepareGet("people", "man", id).get();
        if (id.isEmpty() || !result.isExists()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(result.getSource(), HttpStatus.OK);
    }

    @PostMapping("/add/people/man")
    @ResponseBody
    public ResponseEntity addman(
            @RequestParam(name = "name", defaultValue = "未知") String name,
            @RequestParam(name = "age", defaultValue = "") String age,
            @RequestParam(name = "date", defaultValue = "")
//                    @DateTimeFormat(pattern = "yyyy-MM-dd")
                    String date,
            @RequestParam(name = "country", defaultValue = "") String country) {
        try {
            XContentBuilder content = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("name", name)
                    .field("age", age)
                    .field("date", date)
                    .field("country", country)
                    .endObject();

            IndexResponse result = this.client.prepareIndex("people", "man").setSource(content).get();
            return new ResponseEntity(result.getId(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
