package com.inspur.handler;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.Reflection;
import com.google.protobuf.*;
import com.inspur.protos.SensorOuterClass;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by yuanxiaolong on 2018/9/30.
 */
public class SensorHandler {

    @Deprecated
    public static byte[] readIs(){
        InputStream is = null;
        try {
            byte[] bytes = new byte[4096];
            is = new FileInputStream("sensor.dat");
            List<Byte> list = new ArrayList<>();
            int len = is.read(bytes);
            while (len > 0){
                for (int i = 0; i < len; i++) {
                    list.add(bytes[i]);
                }
                len = is.read(bytes);
            }

            byte[] result = new byte[list.size()];
            for (int i = 0; i < list.size(); i++) {
                result[i] = list.get(i).byteValue();
            }
            return result;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                is.close();
            }   catch (IOException e){
                e.printStackTrace();
            }
        }

        return null;
    }

    @Deprecated
    public static void newMessageUseReflection(){
        try {
            Set<String> fields = Sets.newHashSet("state","client","timestamp");
            System.out.println(SensorOuterClass.Sensor.class);
            Class clazz = Class.forName("com.inspur.protos.SensorOuterClass$Sensor");
            Method newBuilder = clazz.getMethod("newBuilder");
            Object obj = newBuilder.invoke(clazz);

            System.out.println(obj.getClass());
            Class builder = Class.forName("com.inspur.protos.SensorOuterClass$Sensor$Builder");
            Method[] methods = builder.getMethods();
            for (Method method : methods){
                String name = method.getName();
                if(name.startsWith("set")){
                    String field = name.substring(3);
                    if(fields.contains(field.toLowerCase())){
                        Class[] classes = method.getParameterTypes();
                        System.out.println(classes.length);
//                        method.invoke(obj, )
                    }
                }
            }
        }catch (ClassNotFoundException|NoSuchMethodException
                |IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @return
     */
    public static Descriptors.Descriptor codeProto(){
        DescriptorProtos.FieldDescriptorProto.Builder temperatureBuilder = DescriptorProtos.FieldDescriptorProto.newBuilder()
                .setNumber(1)
                .setName("temperature")
                .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE);

        DescriptorProtos.FieldDescriptorProto.Builder humidityBuilder = DescriptorProtos.FieldDescriptorProto.newBuilder()
                .setNumber(2)
                .setName("humidity")
                .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE);

        DescriptorProtos.DescriptorProto.Builder reportedBuilder = DescriptorProtos.DescriptorProto.newBuilder()
                .setName("Reported");

        reportedBuilder.addField(temperatureBuilder)
                .addField(humidityBuilder);

        DescriptorProtos.FieldDescriptorProto.Builder reportedFieldBuilder = DescriptorProtos.FieldDescriptorProto.newBuilder()
                .setNumber(1)
                .setName("reported")
                .setTypeName("Reported");

        DescriptorProtos.DescriptorProto.Builder stateBuilder = DescriptorProtos.DescriptorProto.newBuilder()
                .setName("State")
                .addField(reportedFieldBuilder);

        DescriptorProtos.DescriptorProto.Builder _builder = DescriptorProtos.DescriptorProto.newBuilder()
                .setName("Sensor")
                .addField(DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setNumber(1)
                        .setName("state")
                        .setTypeName("State")
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED))
                .addField(DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setNumber(2)
                        .setName("client")
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING))
                .addField(DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setNumber(3)
                        .setName("timestamp")
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64));

        DescriptorProtos.FileDescriptorProto.Builder builder = DescriptorProtos.FileDescriptorProto.newBuilder().setPackage("com.inspur.protos")
                .setName("sensor.proto")
                .setSyntax("proto3")
                .addMessageType(_builder)
                .addMessageType(reportedBuilder)
                .addMessageType(stateBuilder);

        DescriptorProtos.FileDescriptorProto fileDescriptorProto = builder.build();

//        System.out.println(fileDescriptorProto.toString());
        
        System.out.println(_builder.build().getDescriptorForType().toProto());
//        PluginProtos.CodeGeneratorRequest.Builder requestBuilder = PluginProtos.CodeGeneratorRequest.newBuilder()
//                .addProtoFile(fileDescriptorProto);
//        requestBuilder.build();

        return _builder.getDescriptorForType();
//        System.out.println(requestBuilder.build());
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        String fileName = "dyn-msg.dat";

        try {
            DynamicMessage readMsg = DynamicMessage.parseFrom(SensorOuterClass.Sensor.getDescriptor(), new FileInputStream(fileName));
            System.out.println(readMsg);
        }catch (IOException e){
            e.printStackTrace();
        }

        codeProto();
    }
}
