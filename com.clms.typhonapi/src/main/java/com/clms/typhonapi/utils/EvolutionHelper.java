package com.clms.typhonapi.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.clms.typhonapi.models.Model;

@Component
public class EvolutionHelper {

    @Autowired
    private QueryRunner queryRunner;
    
    public void evolve(Model mlModel) {
    	//TODO: if initial version tell queryRunner to initialize databases, else forward call to evolution
    	if (!mlModel.isInitializedDatabases()) {
    		queryRunner.initDatabases();
    	}
    	
    	//re-init query engine with latest ml model
    	queryRunner.init(mlModel);
    }
}
