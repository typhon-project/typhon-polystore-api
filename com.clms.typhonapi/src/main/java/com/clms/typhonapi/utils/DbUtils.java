package com.clms.typhonapi.utils;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

public class DbUtils {


    public static File MariaBackupProcess(String host, String port, String user, String password, String database_name, String backup_name){
        ProcessBuilder pb = new ProcessBuilder();
        File backupFile;
        File f = new File("/backups/");
        if(f.exists() && f.isDirectory()) {
             backupFile = new File("/backups/"+backup_name+"maria_"+database_name+"_"+DateTime.now().toString("ddMMyyyy")+".sql");
        }
        else{
            boolean direcot = new File("/backups/").mkdir();
            backupFile=new File("/backups/"+backup_name+"maria_"+database_name+"_"+DateTime.now().toString("ddMMyyyy")+".sql");
        }


        pb.command("mysqldump","--host="+host,"-p "+port,"--user="+user,"--password="+password,database_name);
        pb.directory(new File(System.getProperty("user.home")));
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.to(backupFile));

        Process process;
        try {
             process = pb.start();
             process.waitFor();
        } catch (IOException e) {
            backupFile.delete();
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            backupFile.delete();
            e.printStackTrace();
        }
        
        return backupFile;
    }


    public static String MariaRestore(String host, String port, String user, String password, String database_name, String backup_name){
        ProcessBuilder pb = new ProcessBuilder();
        File f = new File("/backups/"+backup_name);
        if(!f.exists() || f.isDirectory()) {
            return "Backup does not exist! ";
        }
        pb.command("mysql","--host="+host,"--port="+port,"--user="+user,"--password="+password, database_name,"--execute=source "+"/backups/"+backup_name);
        System.out.println("" + pb.command());
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process;
        try {
            process = pb.start();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
            return "nope";
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "OK";
    }
}
