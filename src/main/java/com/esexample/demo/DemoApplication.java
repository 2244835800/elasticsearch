package com.esexample.demo;

import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @DeleteMapping("/delete/people/man")
    @ResponseBody
    public ResponseEntity delete(@RequestParam(name = "id", defaultValue = "") String id) {
        DeleteResponse result = this.client.prepareDelete("people", "man", id).get();
        if (id.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(result.getResult().toString(), HttpStatus.OK);
    }

    @DeleteMapping("/constraintDelete/project/item")
    @ResponseBody
    public ResponseEntity constraintDelete(@RequestParam(name = "id", required = false) String id,
                                           @RequestParam(name = "type", required = false) String type) {





        String index = "project";
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if (id != null)
            queryBuilder.must(QueryBuilders.termQuery("id", id));
        if (type != null)
            queryBuilder.must(QueryBuilders.termQuery("type", type));
        BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                        .filter(queryBuilder)
                        .source(index)
                        .get();

        long deleted = response.getDeleted();
        System.out.println(deleted);
        return new ResponseEntity(HttpStatus.OK);
    }


    @PutMapping("/update/people/man")
    @ResponseBody
    public ResponseEntity update(
            @RequestParam(name = "id") String id,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "age", required = false) String age,
            @RequestParam(name = "date", required = false)
//                    @DateTimeFormat(pattern = "yyyy-MM-dd")
                    String date,
            @RequestParam(name = "country", required = false) String country) {

        UpdateRequest update = new UpdateRequest("people", "man", id);
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject();
            if (name != null)
                builder.field("name", name);
            if (age != null)
                builder.field("age", age);
            if (date != null)
                builder.field("date", date);
            if (country != null)
                builder.field("country", country);
            builder.endObject();
            update.doc(builder);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            UpdateResponse result = this.client.update(update).get();
            return new ResponseEntity(result.getResult().toString(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    @PostMapping("query/people/man")
    @ResponseBody
    public ResponseEntity query(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "age", required = false) Integer age,
            @RequestParam(name = "country", required = false) String country
    ) {
        QueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (name != null) {
            ((BoolQueryBuilder) boolQuery).must(QueryBuilders.matchQuery("name", name));
        }
        if (country != null) {
            ((BoolQueryBuilder) boolQuery).must(QueryBuilders.matchQuery("country", country));
        }
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("age").from(age);
        if (age != null && age > 0) {
            rangeQueryBuilder.to(age);
        }
        ((BoolQueryBuilder) boolQuery).filter(rangeQueryBuilder);
        SearchRequestBuilder searchRequestBuilder = this.client.prepareSearch("people").setTypes("man").setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(boolQuery)
                .setFrom(0)
                .setSize(10);
        System.out.println(searchRequestBuilder);
        SearchResponse searchResponse = searchRequestBuilder.get();
        List<Map<String, Object>> rs = new ArrayList<Map<String, Object>>();
        for (SearchHit hit : searchResponse.getHits()) {
            rs.add(hit.getSourceAsMap());
        }
        return new ResponseEntity(rs, HttpStatus.OK);
    }


    @RequestMapping(value = "/getImage")
    @ResponseBody
    public void getImage(HttpServletResponse response) {
        response.setContentType("image/jpeg");

        String filePath = "E:/uploads/file/zhyw/EVN_ACCESSORY/1.jpg";
        File file = new File(filePath);
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            try {
                IOUtils.copy(in, response.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
