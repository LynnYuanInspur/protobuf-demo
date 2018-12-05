package com.inspur.handler;

import com.google.common.collect.Maps;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.inspur.protos.SensorOuterClass;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by yuanxiaolong on 2018/10/1.
 */
public class DynamicMsgDemo {

    /**
     *
     * @param descriptor
     * @param fvs
     * @return
     */
    public static DynamicMessage.Builder newMessageBuilder(Descriptors.Descriptor descriptor, Map<String, Object> fvs){
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
        List<Descriptors.FieldDescriptor> fields = builder.getDescriptorForType().getFields();
        for (Descriptors.FieldDescriptor field: fields){
            String fieldName = field.getName().toLowerCase();
            if(fvs.containsKey(fieldName)){
                if(fvs.get(fieldName) instanceof Map){
                    Map<String, Object> _fvs = (Map<String, Object>)fvs.get(fieldName);
                    builder.setField(field, newMessageBuilder(field.getMessageType(), _fvs).build());
                }else {
                    builder.setField(field, fvs.get(fieldName));
                }
            }
        }
        return builder;
    }

    /**
     *
     * @param message
     * @param fileName
     */
    public static void writeMessage2File(DynamicMessage message, String fileName){
        OutputStream os = null;
        try {
            os = new FileOutputStream(fileName);
            message.writeTo(os);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                os.close();
            }   catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param className  如：com.inspur.protos.SensorOuterClass$Sensor
     * @return
     */
    public Descriptors.Descriptor getDescriptorUseReflection(String className){
        try {
            System.out.println(SensorOuterClass.Sensor.class);
            Class clazz = Class.forName(className);
            Method method = clazz.getMethod("getDescriptor");
            Object obj = method.invoke(clazz);
            if(obj instanceof Descriptors.Descriptor){
             return (Descriptors.Descriptor)obj;
            }
        }catch (ClassNotFoundException|NoSuchMethodException
                |IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        String fileName = "dyn-msg.dat";

        System.out.println("1. initialize field-values map...");
        Map<String, Object> fvs = Maps.newHashMap();

        fvs.put("client", "client-40");
        fvs.put("timestamp", 1538294941336L);

        Map<String, Object> reported = Maps.newHashMap();
        reported.put("temperature", 97.83d);

        Map<String, Object> state = Maps.newHashMap();
        state.put("reported", reported);
        fvs.put("state", state);

        System.out.println("2. new Message...");

        DynamicMessage.Builder builder = newMessageBuilder(SensorOuterClass.Sensor.getDescriptor(), fvs);
        DynamicMessage message = builder.build();

        System.out.println(message);

        System.out.println("3. Write the message to " + fileName);
        writeMessage2File(message, fileName);

        try {
            System.out.println("4. Read the message from " + fileName);
            DynamicMessage readMsg = DynamicMessage.parseFrom(SensorOuterClass.Sensor.getDescriptor(), new FileInputStream(fileName));
            System.out.println(readMsg);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
