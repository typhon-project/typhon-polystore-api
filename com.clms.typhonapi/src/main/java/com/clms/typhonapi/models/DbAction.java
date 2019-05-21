package com.clms.typhonapi.models;

import org.springframework.data.annotation.Id;
import org.joda.time.DateTime;

public class DbAction {

    public String get_Id() {
        return _Id;
    }

    public void set_Id(String _Id) {
        this._Id = _Id;
    }

    public DateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(DateTime actionDate) {
        this.actionDate = actionDate;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDb_name() {
        return db_name;
    }

    public void setDb_name(String db_name) {
        this.db_name = db_name;
    }

    public String getPolystore() {
        return polystore;
    }

    public void setPolystore(String polystore) {
        this.polystore = polystore;
    }

    @Id
    private String _Id;
    private DateTime actionDate;
    private String action;
    private String filename;
    private String db_name;
    private String polystore;

    public String backupMariaDB(String polystore,String filename,String database){
        return "ok";
    }
}
