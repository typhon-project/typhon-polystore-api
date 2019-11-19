package com.clms.typhonapi.models;
import nl.cwi.swat.typhonql.client.Attribute;
import nl.cwi.swat.typhonql.client.Place;
import nl.cwi.swat.typhonql.client.Relation;

import java.util.List;
import java.util.Map;

public class MLSchema {

    public Relation[] relations;
    public Attribute[] attributes;
    public Map<Place, List<String>> places;
}
