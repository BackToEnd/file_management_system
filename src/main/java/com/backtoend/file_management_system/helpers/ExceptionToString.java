package com.backtoend.file_management_system.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExceptionToString {
    private ExceptionToString() { }

    public static ArrayList<String> getStringStackTrace(Exception e) {
        ArrayList<String> result = new ArrayList<>();
        List<StackTraceElement> list = Arrays.stream(e.getStackTrace()).toList();
        list.forEach(element -> {
            String className = element.getClassName();
            String effectiveClassName = (className.contains(".")) ?
                    className.substring(className.lastIndexOf('.') + 1)
                    : className;
            String methodName = element.getMethodName();
            int lineNumber = element.getLineNumber();
            String fileName = element.getFileName();
            String format = '[' + fileName + "] " +
                    effectiveClassName + " > " +
                    methodName + " (line " +
                    lineNumber + ")";
            result.add(format);
        });
        return result;
    }
}
