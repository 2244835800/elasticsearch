package com.esexample.demo;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoApplication {
    @Autowired
    private TransportClient client;

    @GetMapping("/")
    public String index(){
        return "index";
    }

    @GetMapping("/get/people/man")
    @ResponseBody
    public ResponseEntity getman(@RequestParam(name="id",defaultValue = "") String id){
        GetResponse result=this.client.prepareGet( "people","man",id).get();
        if(id.isEmpty()||!result.isExists()){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(result.getSource(),HttpStatus.OK);
    }
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
