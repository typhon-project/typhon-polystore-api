package com.clms.typhonapi.utils;

import com.clms.typhonapi.models.Model;
import com.clms.typhonapi.storage.ModelStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Slf4j
@Service
public class ModelWatcher {

  private ReactiveMongoTemplate reactiveMongoTemplate;

  @Autowired
  public ModelWatcher(ReactiveMongoTemplate reactiveMongoTemplate) {
    this.reactiveMongoTemplate = reactiveMongoTemplate;
  }

  /**
   * Watch for changes to the model collection
   *
   * @return a subscription to the change stream
   */
  public Flux<Model> watchForModelCollectionChanges() {
    // set changestream options to watch for any changes to the businesses collection
    ChangeStreamOptions options = ChangeStreamOptions.builder()
      .filter(Aggregation.newAggregation(Model.class,
              Aggregation.match(
                Criteria.where("operationType").is("replace")
              )
      )).returnFullDocumentOnUpdate().build();

      // return a flux that watches the changestream and returns the full document
      return reactiveMongoTemplate.changeStream("models", options, Model.class)
               .map(ChangeStreamEvent::getBody)
               .doOnError(throwable -> log.error("Error with the models changestream event: " + throwable.getMessage(), throwable));
    }
}