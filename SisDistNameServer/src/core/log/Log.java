/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author renee
 */
public class Log {

    private FileWriter writer;
    private PrintWriter out;
    private final ExecutorService logExecutor;
    private final DateFormat dateFormat;
    private final Calendar cal;

    public static enum Types {

        INIT, CLOSE, ERROR, UPDATE, MSG
    };

    private Log() {

        logExecutor = Executors.newSingleThreadExecutor();

        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        cal = Calendar.getInstance();

        logExecutor.execute(() -> {
            try {
                initLog();
            } catch (IOException ex) {
                Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

    }

    public static Log getInstance() {
        return LogHolder.INSTANCE;
    }

    private static class LogHolder {

        private static final Log INSTANCE = new Log();
    }

    private void initLog() throws IOException {
        //continua no final do arquivo
        writer = new FileWriter("log.txt", true);
        out = new PrintWriter(writer, true);
    }

    public void addLog(Types type, String msg) {
        logExecutor.execute(() -> {
            out.println(dateFormat.format(cal.getTime()) + " - " + type + " - " + msg);
        });
    }

    public void close() {
        logExecutor.execute(() -> {
            try {
                out.close();
                writer.close();
                logExecutor.shutdown();
            } catch (IOException ex) {
                Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    /**
     * Retorna os tipos de notificacoes disponiveis
     * @return 
     */
    public Types[] getTypes(){
        return Types.values();
    }
}
