package cn.bingod.antipyretic;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * @author bin
 * @since 2017/5/23
 */
final class Log {

    private Messager messager;
    private static Log log;

    private Log(Messager messager) {
        this.messager = messager;
    }

    static void init(Messager messager) {
        if (log == null) {
            log = new Log(messager);
        }
    }

    static void print(CharSequence msg) {
        if (log != null && log.messager != null)
            log.messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    static void println(CharSequence msg) {
        print(msg + "\n");
    }
}
