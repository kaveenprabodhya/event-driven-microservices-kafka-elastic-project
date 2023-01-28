package com.microservices.demo.elastic.query.service.api;

import com.microservices.demo.elastic.query.service.business.ElasticQueryService;
import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceRequestModel;
import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceResponseModel;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/documents", produces = "application/vnd.api.v1+json")
public class ElasticDocumentController {

    private final ElasticQueryService elasticQueryService;

    private static final Logger logger = LoggerFactory.getLogger(ElasticDocumentController.class);

    public ElasticDocumentController(ElasticQueryService elasticQueryService) {
        this.elasticQueryService = elasticQueryService;
    }

    @GetMapping
    public @ResponseBody ResponseEntity<List<ElasticQueryServiceResponseModel>> getAllDocuments() {
        List<ElasticQueryServiceResponseModel> response = new ArrayList<>();
        logger.info("Elasticsearch returned {} of documents", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public @ResponseBody ResponseEntity<ElasticQueryServiceResponseModel> getDocumentById(@PathVariable String id) {
        ElasticQueryServiceResponseModel responseModel = ElasticQueryServiceResponseModel.builder()
                .id(id)
                .build();
        logger.debug("Elasticsearch returned document with id {}", id);
        return ResponseEntity.ok(responseModel);
    }

    @PostMapping("/get-document-by-text")
    public @ResponseBody
    ResponseEntity<List<ElasticQueryServiceResponseModel>>
    getDocumentByText(@RequestBody @Valid ElasticQueryServiceRequestModel elasticQueryServiceRequestModel) {
        List<ElasticQueryServiceResponseModel> response =
                elasticQueryService.getDocumentByText(elasticQueryServiceRequestModel.getText());
        logger.info("Elasticsearch returned {} of documents", response.size());
        return ResponseEntity.ok(response);
    }
}
