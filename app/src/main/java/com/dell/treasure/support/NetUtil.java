package com.dell.treasure.support;


import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;


/**
 * Created by hp on 2016/3/17 0017.
 */
public class NetUtil {
    //命名空间
    private final static String nameSpace = "http://jxn.com/";
    // EndPoint
    private final static String allPoint = "http://222.128.13.159:9696/find/all?wsdl";

    private final static int TimeOut = 12000;

    public static String signUp(String username,String password,String alipay) throws SoapFault {
        String response;
        // 调用的方法名称
        String methodName = "SignUp";
        String soapAction = "http://jxn.com/signUp";
        // 指定WebService的命名空间和调用的方法名
        SoapObject rpc = new SoapObject(nameSpace, methodName);

        // 设置需调用WebService接口需要传入的两个参数mobileCode、userId
        rpc.addProperty("name", username);
        rpc.addProperty("password", password);
        rpc.addProperty("alipay", alipay);

        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = true;
        // 等价于envelope.bodyOut = rpc;
        envelope.setOutputSoapObject(rpc);
        HttpTransportSE transport = new HttpTransportSE(allPoint,TimeOut);
        try {
            // 调用WebService
            transport.call(soapAction, envelope);
//        如果返回的数据类型是byte[]类型
//        SoapObject object = (SoapObject) envelope.bodyIn;
            //如果返回的数据类型是String类型
//            response=  envelope.getResponse().toString();

            // 获取返回的结果
            SoapPrimitive object = (SoapPrimitive)envelope.getResponse();
            Log.d("result","signup "+object.toString());
            response = object.toString();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "连接服务器失败";
            if(e instanceof java.net.SocketTimeoutException){
                msg = "连接服务器超时，请检查网络";
            }else if(e instanceof java.net.UnknownHostException){
                msg = "未知服务器，请检查配置";
            }
            return msg;
        }
    }

    public static String signIn(String username,String password) throws SoapFault {
        String response;

        String methodName = "SignIn";
        String soapAction = "http://jxn.com/signIn";

        SoapObject rpc = new SoapObject(nameSpace, methodName);

        rpc.addProperty("name", username);
        rpc.addProperty("password", password);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(allPoint,TimeOut);
        try {
            transport.call(soapAction, envelope);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SoapPrimitive object = (SoapPrimitive)envelope.getResponse();
        Log.d("result","signin "+object.toString());
        response = object.toString();
        return response;
    }

    public static String boundEqu(String username,String category,String MacId) throws SoapFault {
        String response;

        String methodName = "BoundEqu";
        String soapAction = "http://jxn.com/BoundEqu";

        SoapObject rpc = new SoapObject(nameSpace, methodName);

        rpc.addProperty("username", username);
        rpc.addProperty("category", category);
        rpc.addProperty("MacId", MacId);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(allPoint,TimeOut);
        try {
            transport.call(soapAction, envelope);
            SoapPrimitive object = (SoapPrimitive)envelope.getResponse();
            Log.d("result","bound "+object.toString());
            response = object.toString();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "连接服务器失败";
            if(e instanceof java.net.SocketTimeoutException){
                msg = "连接服务器超时，请检查网络";
            }else if(e instanceof java.net.UnknownHostException){
                msg = "未知服务器，请检查配置";
            }
            return msg;
        }

    }

    public static String OffBoundEqu(String username,String MacId) throws SoapFault {
        String response;

        String methodName = "OffBoundEqu";
        String soapAction = "http://jxn.com/OffBoundEqu";

        SoapObject rpc = new SoapObject(nameSpace, methodName);

//        rpc.addProperty("username", username);
        rpc.addProperty("MacId", MacId);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(allPoint,TimeOut);
        try {
            transport.call(soapAction, envelope);
            SoapPrimitive object = (SoapPrimitive)envelope.getResponse();
            Log.d("result","offBound "+object.toString());
            response = object.toString();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "连接服务器失败";
            if(e instanceof java.net.SocketTimeoutException){
                msg = "连接服务器超时，请检查网络";
            }else if(e instanceof java.net.UnknownHostException){
                msg = "未知服务器，请检查配置";
            }
            return msg;
        }

    }

    public static String allBoundEqu(String username) throws SoapFault {
        String response;

        String methodName = "allBoundEqu";
        String soapAction = "http://jxn.com/allBoundEqu";

        SoapObject rpc = new SoapObject(nameSpace, methodName);

        rpc.addProperty("username", username);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(allPoint,TimeOut);
        try {
            transport.call(soapAction, envelope);
            SoapPrimitive object = (SoapPrimitive)envelope.getResponse();
            Log.d("result","allBound "+object.toString());
            response = object.toString();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "连接服务器失败";
            if(e instanceof java.net.SocketTimeoutException){
                msg = "连接服务器超时，请检查网络";
            }else if(e instanceof java.net.UnknownHostException){
                msg = "未知服务器，请检查配置";
            }
            return msg;
        }

    }

    public static String ReleaseTask(String username,String MacId,String location,
        String lon,String lat,String losetime,String money) throws SoapFault {
        String response;

        String methodName = "ReleaseTask";
        String soapAction = "http://jxn.com/ReleaseTask";

        SoapObject rpc = new SoapObject(nameSpace, methodName);

        rpc.addProperty("username", username);
        rpc.addProperty("MacId", MacId);
        rpc.addProperty("location", location);
        rpc.addProperty("lon", lon);
        rpc.addProperty("lat", lat);
        rpc.addProperty("losetime", losetime);
        rpc.addProperty("money", money);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);
        HttpTransportSE transport = new HttpTransportSE(allPoint);
        try {
            transport.call(soapAction, envelope);
            SoapPrimitive object = (SoapPrimitive)envelope.getResponse();
            Log.d("result","ReleaseTask "+object.toString());
            response = object.toString();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("result","ReleaseTask " + e.getMessage());
            String msg = "连接服务器失败";
            if(e instanceof java.net.SocketTimeoutException){
                msg = "连接服务器超时，请检查网络";
            }else if(e instanceof java.net.UnknownHostException){
                msg = "未知服务器，请检查配置";
            }
            return msg;
        }

    }

    public static String ParticipateTask(String userid,String MacId,String fromuserid,
                                     String way) throws SoapFault {
        String response;

        String methodName = "ParticipateTask";
        String soapAction = "http://jxn.com/ParticipateTask";

        SoapObject rpc = new SoapObject(nameSpace, methodName);

        rpc.addProperty("userid", userid);
        rpc.addProperty("MacId", MacId);
        rpc.addProperty("fromuserid", fromuserid);
        rpc.addProperty("way", way);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);
        HttpTransportSE transport = new HttpTransportSE(allPoint,TimeOut);
        try {
            transport.call(soapAction, envelope);
            SoapPrimitive object = (SoapPrimitive)envelope.getResponse();
            Log.d("result","ParticipateTask "+object.toString());
            response = object.toString();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "连接服务器失败";
            if(e instanceof java.net.SocketTimeoutException){
                msg = "连接服务器超时，请检查网络";
            }else if(e instanceof java.net.UnknownHostException){
                msg = "未知服务器，请检查配置";
            }
            return msg;
        }

    }

    public static String HaveFound(String username,String taskId,String MacId,String location,
                                     String lon,String lat) throws SoapFault {
        String response;

        String methodName = "HaveFound";
        String soapAction = "http://jxn.com/HaveFound";

        SoapObject rpc = new SoapObject(nameSpace, methodName);

        rpc.addProperty("username", username);
        rpc.addProperty("taskId", taskId);
        rpc.addProperty("MacId", MacId);
        rpc.addProperty("loction", location);
        rpc.addProperty("lon", lon);
        rpc.addProperty("lat", lat);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);
        HttpTransportSE transport = new HttpTransportSE(allPoint,TimeOut);
        try {
            transport.call(soapAction, envelope);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SoapPrimitive object = (SoapPrimitive)envelope.getResponse();
        Log.d("result","HaveFound "+object.toString());
        response = object.toString();
        return response;
    }

    public static String Report(String link) throws SoapFault {
        String response;
        String methodName = "report";
        String soapAction = "http://jxn.com/report";

        SoapObject rpc = new SoapObject(nameSpace, methodName);
        rpc.addProperty("link", link);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);
        HttpTransportSE transport = new HttpTransportSE(allPoint,TimeOut);
        try {
            transport.call(soapAction, envelope);
            SoapPrimitive object = (SoapPrimitive)envelope.getResponse();
            Log.d("result","report "+object.toString());
            response = object.toString();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "连接服务器失败";
            if(e instanceof java.net.SocketTimeoutException){
                msg = "连接服务器超时，请检查网络";
            }else if(e instanceof java.net.UnknownHostException){
                msg = "未知服务器，请检查配置";
            }
            return msg;
        }

    }

    public static String UserInfo(String username) throws SoapFault {
        String response;

        String methodName = "getuserInfo";
        String soapAction = "http://jxn.com/getuserInfo";

        SoapObject rpc = new SoapObject(nameSpace, methodName);

        rpc.addProperty("username", username);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(allPoint,TimeOut);
        try {
            transport.call(soapAction, envelope);

        } catch (Exception e) {
            e.printStackTrace();
        }
        SoapPrimitive object = (SoapPrimitive)envelope.getResponse();

        response = object.toString();
        return response;
    }

    public static String RecordParti(String userId,String taskId,String timeLongth,String distance,
                                      String way,String fromId) throws SoapFault {
        String response;

        String methodName = "tasks_act";
        String soapAction = "http://jxn.com/tasks_act";

        SoapObject rpc = new SoapObject(nameSpace, methodName);

        rpc.addProperty("userId", userId);
        rpc.addProperty("taskId", taskId);
        rpc.addProperty("timeLongth", timeLongth);
        rpc.addProperty("distance",distance);
        rpc.addProperty("way", way);
        rpc.addProperty("fromId", fromId);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(allPoint,TimeOut);
        try {
            transport.call(soapAction, envelope);

        } catch (Exception e) {
            e.printStackTrace();
        }
        SoapPrimitive object = (SoapPrimitive)envelope.getResponse();

        response = object.toString();
        return response;
    }

    public static String GetFinish(String taskId) throws SoapFault {
        String response;

        String methodName = "GetTaskFinishTime";
        String soapAction = "http://jxn.com/GetTaskFinishTime";

        SoapObject rpc = new SoapObject(nameSpace, methodName);

        rpc.addProperty("taskId", taskId);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(allPoint,TimeOut);
        try {
            transport.call(soapAction, envelope);

        } catch (Exception e) {
            e.printStackTrace();

        }
        SoapPrimitive object = (SoapPrimitive)envelope.getResponse();
//        SoapObject object = (SoapObject) envelope.getResponse();
        response = object.toString();
        return response;
    }

    public static String isReciveMeg(String username) throws SoapFault {
        String response;

        String methodName = "isReciveMeg";
        String soapAction = "http://jxn.com/isReciveMeg";

        SoapObject rpc = new SoapObject(nameSpace, methodName);

        rpc.addProperty("username", username);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(allPoint,TimeOut);
        try {
            transport.call(soapAction, envelope);

        } catch (Exception e) {
            e.printStackTrace();

        }
        SoapPrimitive object = (SoapPrimitive)envelope.getResponse();
//        SoapObject object = (SoapObject) envelope.getResponse();
        response = object.toString();
        return response;
    }
}
