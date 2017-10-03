package com.uftorrent.app.setup.env;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.FileReader;
import java.io.BufferedReader;

public class CommonVars {
    private Map<String, String> envVars = new HashMap<>();
    public CommonVars() {
        String fileName = "Common.cfg";
        loadEnvVars(fileName);
    }

    private void loadEnvVars(String fileName) {
        String line;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                String[] envKeyValue = line.split("=");
                this.envVars.put(envKeyValue[0], envKeyValue[1]);
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        }
    }

    public void print() {
        for (String envName : this.envVars.keySet()) {
            System.out.format("Key: %s, Value: %s%n", envName, this.envVars.get(envName));
        }
    }
    public String get(String key) {
        return this.envVars.get(key);
    }
}
